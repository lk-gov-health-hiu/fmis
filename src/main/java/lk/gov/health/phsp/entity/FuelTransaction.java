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
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.enums.FuelTransactionType;

/**
 *
 * @author buddhika ariyaratne
 */
@Entity
public class FuelTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date requestAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date requestedDate;
    @ManyToOne
    private WebUser requestedBy;
    @ManyToOne
    private Institution requestedInstitution;
    @ManyToOne
    private Institution fromInstitution;
    @ManyToOne
    private Institution toInstitution;
    @ManyToOne
    private Vehicle vehicle;
    @ManyToOne
    private Driver driver;

    @Lob
    private String comments;

    @Enumerated(EnumType.STRING)
    private FuelTransactionType transactionType;

    private Double requestQuantity;
    private Double issuedQuantity;
    private Double odoMeterReading;

    private String requestReferenceNumber;
    private String issueReferenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;

    private boolean issued;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date issuedAt;
    @ManyToOne
    private Institution issuedInstitution;
    @ManyToOne
    private WebUser issuedUser;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date issuedDate;

    private boolean retired;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    @Lob
    private String retireComments;

    @ManyToOne
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retireReversedAt;
    @Lob
    private String retireReverseComments;

    private boolean cancelled;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser cancelledBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date cancelledAt;
    @Lob
    private String cancellationComments;
    @ManyToOne
    private Institution cancelledInstitution;

    private boolean rejected;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser rejectedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date rejectedAt;
    @Lob
    private String rejectedComments;
    @ManyToOne
    private Institution rejectedInstitution;

    private Double stockBeforeTheTransaction;
    private Double stockAfterTheTransaction;

    @Deprecated
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date txDate;
    @Deprecated
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date txTime;
    @ManyToOne
    private Institution institution;
    private String invoiceNo;
    private String bowserNumber;

    @Deprecated
    private Double receivedQty;

    public String getIdString() {
        if (id == null) {
            return "";
        }
        return id + "";
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

        if (!(object instanceof FuelTransaction)) {
            return false;
        }
        FuelTransaction other = (FuelTransaction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id + "";
    }

    public WebUser getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(WebUser requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Institution getRequestedInstitution() {
        return requestedInstitution;
    }

    public void setRequestedInstitution(Institution requestedInstitution) {
        this.requestedInstitution = requestedInstitution;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Double getRequestQuantity() {
        return requestQuantity;
    }

    public void setRequestQuantity(Double requestQuantity) {
        this.requestQuantity = requestQuantity;
    }

    public Double getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(Double issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public boolean isIssued() {
        return issued;
    }

    public void setIssued(boolean issued) {
        this.issued = issued;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Institution getIssuedInstitution() {
        return issuedInstitution;
    }

    public void setIssuedInstitution(Institution issuedInstitution) {
        this.issuedInstitution = issuedInstitution;
    }

    public WebUser getIssuedUser() {
        return issuedUser;
    }

    public void setIssuedUser(WebUser issuedUser) {
        this.issuedUser = issuedUser;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCancellationComments() {
        return cancellationComments;
    }

    public void setCancellationComments(String cancellationComments) {
        this.cancellationComments = cancellationComments;
    }

    public Institution getCancelledInstitution() {
        return cancelledInstitution;
    }

    public void setCancelledInstitution(Institution cancelledInstitution) {
        this.cancelledInstitution = cancelledInstitution;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public String getRejectedComments() {
        return rejectedComments;
    }

    public void setRejectedComments(String rejectedComments) {
        this.rejectedComments = rejectedComments;
    }

    public Institution getRejectedInstitution() {
        return rejectedInstitution;
    }

    public void setRejectedInstitution(Institution rejectedInstitution) {
        this.rejectedInstitution = rejectedInstitution;
    }

    public Date getRequestAt() {
        return requestAt;
    }

    public void setRequestAt(Date requestAt) {
        this.requestAt = requestAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public FuelTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(FuelTransactionType transactionType) {
        this.transactionType = transactionType;
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

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public WebUser getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(WebUser cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public Date getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public WebUser getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(WebUser rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public Date getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Date rejectedAt) {
        this.rejectedAt = rejectedAt;
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

    public String getRequestReferenceNumber() {
        return requestReferenceNumber;
    }

    public void setRequestReferenceNumber(String requestReferenceNumber) {
        this.requestReferenceNumber = requestReferenceNumber;
    }

    public String getIssueReferenceNumber() {
        return issueReferenceNumber;
    }

    public void setIssueReferenceNumber(String issueReferenceNumber) {
        this.issueReferenceNumber = issueReferenceNumber;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Double getStockBeforeTheTransaction() {
        return stockBeforeTheTransaction;
    }

    public void setStockBeforeTheTransaction(Double stockBeforeTheTransaction) {
        this.stockBeforeTheTransaction = stockBeforeTheTransaction;
    }

    public Double getStockAfterTheTransaction() {
        return stockAfterTheTransaction;
    }

    public void setStockAfterTheTransaction(Double stockAfterTheTransaction) {
        this.stockAfterTheTransaction = stockAfterTheTransaction;
    }

    @Deprecated
    public Date getTxDate() {
        return txDate;
    }

    @Deprecated
    public void setTxDate(Date txDate) {
        this.txDate = txDate;
    }

    @Deprecated
    public Date getTxTime() {
        return txTime;
    }

    @Deprecated
    public void setTxTime(Date txTime) {
        this.txTime = txTime;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getBowserNumber() {
        return bowserNumber;
    }

    public void setBowserNumber(String bowserNumber) {
        this.bowserNumber = bowserNumber;
    }

    @Deprecated
    public Double getReceivedQty() {
        return receivedQty;
    }

    @Deprecated
    public void setReceivedQty(Double receivedQty) {
        this.receivedQty = receivedQty;
    }

    public Double getOdoMeterReading() {
        return odoMeterReading;
    }

    public void setOdoMeterReading(Double odoMeterReading) {
        this.odoMeterReading = odoMeterReading;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetireReversedAt() {
        return retireReversedAt;
    }

    public void setRetireReversedAt(Date retireReversedAt) {
        this.retireReversedAt = retireReversedAt;
    }

    public String getRetireReverseComments() {
        return retireReverseComments;
    }

    public void setRetireReverseComments(String retireReverseComments) {
        this.retireReverseComments = retireReverseComments;
    }

    public Date getRequestedDate() {
        if(requestedDate==null){
            requestedDate = new Date();
        }
        return requestedDate;
    }

    public void setRequestedDate(Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Date getIssuedDate() {
        if(issuedDate==null){
            issuedDate = new Date();
        }
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }
    
    

}
