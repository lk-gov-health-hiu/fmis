/*
 * The MIT License
 *
 * Copyright 2026 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.BillAcceptanceStatus;

/**
 * Audit trail entry recorded for every CPC acceptance-workflow transition on
 * a {@link Bill} - Accept, Request Resubmit, Cancel Acceptance, and the
 * submitting side's Resubmit. Independent of {@link BillHistory}, which only
 * tracks total-quantity drift found on reprint.
 *
 * @author Dr M H B Ariyaratne
 */
@Entity
@XmlRootElement
public class BillAcceptanceHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Enumerated(EnumType.STRING)
    private BillAcceptanceStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private BillAcceptanceStatus toStatus;

    @Lob
    private String comments;

    @ManyToOne
    private WebUser changedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date changedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BillAcceptanceHistory)) {
            return false;
        }
        BillAcceptanceHistory other = (BillAcceptanceHistory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.BillAcceptanceHistory[ id=" + id + " ]";
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public BillAcceptanceStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(BillAcceptanceStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public BillAcceptanceStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(BillAcceptanceStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public WebUser getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(WebUser changedBy) {
        this.changedBy = changedBy;
    }

    public Date getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Date changedAt) {
        this.changedAt = changedAt;
    }

}
