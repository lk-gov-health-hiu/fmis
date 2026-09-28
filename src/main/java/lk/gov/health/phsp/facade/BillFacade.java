/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.facade;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import lk.gov.health.phsp.entity.Bill;
import lk.gov.health.phsp.entity.BillNumberCounter;
import lk.gov.health.phsp.entity.DemoAccount;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.BillAcceptanceStatus;
import lk.gov.health.phsp.enums.SriLankaProvince;

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Stateless
public class BillFacade extends AbstractFacade<Bill> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BillFacade() {
        super(Bill.class);
    }

    /**
     * Marks the bill accepted and gives it the next acceptance number for the
     * province, in one transaction: if saving the bill fails (e.g. someone else
     * changed it meanwhile) the counter increment is rolled back too, so no
     * number is used up.
     *
     * @return the saved (managed) bill
     */
    public Bill acceptWithNumber(Bill bill, SriLankaProvince province, WebUser acceptedBy, Date acceptedAt) {
        Bill managed = em.merge(bill);
        managed.setAcceptanceStatus(BillAcceptanceStatus.ACCEPTED);
        managed.setAcceptedBy(acceptedBy);
        managed.setAcceptedAt(acceptedAt);
        managed.setAcceptanceNumber(nextAcceptanceNumber(province));
        em.flush();
        return managed;
    }

    /**
     * One-time numbering of bills accepted before acceptance numbers existed,
     * in the order they were accepted. Safe to run repeatedly - only accepted
     * bills without a number are touched.
     *
     * @return how many bills were numbered
     */
    public int backfillAcceptanceNumbers() {
        List<Bill> bills = em.createQuery("select b from Bill b "
                + "where b.retired = false "
                + "and b.acceptanceStatus = :accepted "
                + "and b.acceptanceNumber is null "
                + "order by b.acceptedAt, b.id", Bill.class)
                .setParameter("accepted", BillAcceptanceStatus.ACCEPTED)
                .getResultList();
        int numbered = 0;
        for (Bill b : bills) {
            SriLankaProvince province = b.getToInstitution() == null ? null : b.getToInstitution().getSriLankaProvince();
            if (province == null) {
                Logger.getLogger(BillFacade.class.getName()).log(Level.WARNING,
                        "Accepted bill {0} not given an acceptance number - province of its fuel station is unknown", b.getId());
                continue;
            }
            b.setAcceptanceNumber(nextAcceptanceNumber(province));
            numbered++;
        }
        return numbered;
    }

    // Locks the province's counter row until the calling transaction ends, so
    // concurrent acceptances in the same province wait for each other and can
    // never get the same number.
    private String nextAcceptanceNumber(SriLankaProvince province) {
        String key = "CPC_BILL_ACCEPTANCE_" + province.getCode();
        BillNumberCounter counter = em.find(BillNumberCounter.class, key, LockModeType.PESSIMISTIC_WRITE);
        if (counter == null) {
            counter = new BillNumberCounter(key);
            em.persist(counter);
            em.flush();
        }
        counter.setLastNumber(counter.getLastNumber() + 1);
        // Write the increment now: a second locked find in the same transaction
        // (e.g. the backfill) re-reads the row and would otherwise lose it
        em.flush();
        return String.format("%s/%06d", province.getCode(), counter.getLastNumber());
    }

}
