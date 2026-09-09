/*
 * The MIT License
 *
 * Copyright 2024 buddh.
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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lk.gov.health.phsp.enums.BillAcceptanceStatus;

/**
 *
 * @author buddh
 */
@Entity
@Table(name = "BILL", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fromInstitution_id", "toInstitution_id", "billNo"})
})
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fromInstitution_id", nullable = false)
    private Institution fromInstitution;

    @ManyToOne
    @JoinColumn(name = "toInstitution_id", nullable = false)
    private Institution toInstitution;

    @Column(name = "billNo", nullable = false, updatable = false)
    private String billNo;

    @Column(name = "billType")
    private String billType;

    @Temporal(TemporalType.DATE)
    @Column(name = "billDate")
    private Date billDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "billTime")
    private Date billTime;

    @Column(name = "retired")
    private boolean retired;

    @ManyToOne
    @JoinColumn(name = "retiredBy_id")
    private WebUser retiredBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "retiredAt")
    private Date retiredAt;
    
    private Double totalQty;
    /**
     * The fuel price/liter in effect when this bill was created (see
     * FuelPrice) - a snapshot, so it stays fixed even if the price schedule
     * is later edited. Null for bills created before fuel prices existed.
     */
    private Double pricePerLiter;
    private Double totalValue;

    @ManyToOne
    private WebUser billUser;

    /**
     * Optimistic lock - guards against two concurrent requests (e.g. two
     * accidental double-clicks on Accept) both succeeding on the same bill.
     */
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private BillAcceptanceStatus acceptanceStatus;

    @ManyToOne
    private WebUser acceptedBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date acceptedAt;

    @ManyToOne
    private WebUser resubmitRequestedBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date resubmitRequestedAt;
    @Lob
    private String resubmitComments;

    @ManyToOne
    private WebUser acceptanceCancelledBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date acceptanceCancelledAt;
    @Lob
    private String acceptanceCancelledComments;

    /**
     * Set when the submitting (admin/hospital) side has fixed the flagged
     * transactions and explicitly resubmits the bill for another CPC
     * decision - moves {@link #acceptanceStatus} back to
     * {@link BillAcceptanceStatus#PENDING}.
     */
    @ManyToOne
    private WebUser resubmittedBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date resubmittedAt;

    @PrePersist
    private void generateBillNo() {
        // Generate the bill number based on the institutions
        this.billNo = fromInstitution.getCode() + "-" + toInstitution.getCode() + "-" + System.currentTimeMillis();
    }

    
    
    
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Bill)) {
            return false;
        }
        Bill other = (Bill) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.Bill[ id=" + id + " ]";
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public Date getBillTime() {
        return billTime;
    }

    public void setBillTime(Date billTime) {
        this.billTime = billTime;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public Double getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Double totalQty) {
        this.totalQty = totalQty;
    }

    public Double getPricePerLiter() {
        return pricePerLiter;
    }

    public void setPricePerLiter(Double pricePerLiter) {
        this.pricePerLiter = pricePerLiter;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public WebUser getBillUser() {
        return billUser;
    }

    public void setBillUser(WebUser billUser) {
        this.billUser = billUser;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Legacy bills created before this field existed have no stored value -
     * they are treated as {@code PENDING}, exactly as they behaved before
     * the acceptance workflow existed.
     */
    public BillAcceptanceStatus getAcceptanceStatus() {
        if (acceptanceStatus == null) {
            acceptanceStatus = BillAcceptanceStatus.PENDING;
        }
        return acceptanceStatus;
    }

    public void setAcceptanceStatus(BillAcceptanceStatus acceptanceStatus) {
        this.acceptanceStatus = acceptanceStatus;
    }

    public boolean isPendingDecision() {
        return getAcceptanceStatus() == BillAcceptanceStatus.PENDING;
    }

    public boolean isAccepted() {
        return getAcceptanceStatus() == BillAcceptanceStatus.ACCEPTED;
    }

    public boolean isResubmitRequested() {
        return getAcceptanceStatus() == BillAcceptanceStatus.RESUBMIT_REQUESTED;
    }

    public WebUser getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(WebUser acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public WebUser getResubmitRequestedBy() {
        return resubmitRequestedBy;
    }

    public void setResubmitRequestedBy(WebUser resubmitRequestedBy) {
        this.resubmitRequestedBy = resubmitRequestedBy;
    }

    public Date getResubmitRequestedAt() {
        return resubmitRequestedAt;
    }

    public void setResubmitRequestedAt(Date resubmitRequestedAt) {
        this.resubmitRequestedAt = resubmitRequestedAt;
    }

    public String getResubmitComments() {
        return resubmitComments;
    }

    public void setResubmitComments(String resubmitComments) {
        this.resubmitComments = resubmitComments;
    }

    public WebUser getAcceptanceCancelledBy() {
        return acceptanceCancelledBy;
    }

    public void setAcceptanceCancelledBy(WebUser acceptanceCancelledBy) {
        this.acceptanceCancelledBy = acceptanceCancelledBy;
    }

    public Date getAcceptanceCancelledAt() {
        return acceptanceCancelledAt;
    }

    public void setAcceptanceCancelledAt(Date acceptanceCancelledAt) {
        this.acceptanceCancelledAt = acceptanceCancelledAt;
    }

    public String getAcceptanceCancelledComments() {
        return acceptanceCancelledComments;
    }

    public void setAcceptanceCancelledComments(String acceptanceCancelledComments) {
        this.acceptanceCancelledComments = acceptanceCancelledComments;
    }

    public WebUser getResubmittedBy() {
        return resubmittedBy;
    }

    public void setResubmittedBy(WebUser resubmittedBy) {
        this.resubmittedBy = resubmittedBy;
    }

    public Date getResubmittedAt() {
        return resubmittedAt;
    }

    public void setResubmittedAt(Date resubmittedAt) {
        this.resubmittedAt = resubmittedAt;
    }

}
