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

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lk.gov.health.phsp.entity.Driver;

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Stateless
public class DriverFacade extends AbstractFacade<Driver> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DriverFacade() {
        super(Driver.class);
    }

    /**
     * Find a driver by NIC, optionally excluding a specific driver by ID
     * @param nic The NIC to search for
     * @param excludeId The driver ID to exclude from the search (null if creating new)
     * @return Driver with the matching NIC, or null if not found
     */
    public Driver findDriverByNic(String nic, Long excludeId) {
        if (nic == null || nic.trim().isEmpty()) {
            return null;
        }

        String jpql;
        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("nic", nic.trim());

        if (excludeId != null) {
            // When editing, exclude the current driver from the search
            jpql = "SELECT d FROM Driver d WHERE LOWER(d.nic) = LOWER(:nic) AND d.id != :excludeId AND d.retired = false";
            parameters.put("excludeId", excludeId);
        } else {
            // When creating, just check if NIC exists
            jpql = "SELECT d FROM Driver d WHERE LOWER(d.nic) = LOWER(:nic) AND d.retired = false";
        }

        return findFirstByJpql(jpql, parameters);
    }

}
