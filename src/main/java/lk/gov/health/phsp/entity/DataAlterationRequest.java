/*
 * The MIT License
 *
 * Copyright 2024 buddhika.
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
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lk.gov.health.phsp.enums.DataAlterationRequestType;

/**
 *
 * @author buddhika
 */
@Entity
public class DataAlterationRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private FuelTransaction fuelTransaction;
    @Enumerated
    private DataAlterationRequestType dataAlterationRequestType;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date requestAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date requestedDate;
    @ManyToOne
    private WebUser requestedBy;
    @ManyToOne
    private Institution requestedInstitution;
    @Lob
    private String requestComments;
    
    private Boolean rejected;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date rejectedAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date rejectedDate;
    @ManyToOne
    private WebUser rejectedBy;
    @Lob
    private String rejectComments;
    
    private Boolean completed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date completedAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date completedDate;
    @ManyToOne
    private WebUser completedBy;
    @Lob
    private String completeComments;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;

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
        if (!(object instanceof DataAlterationRequest)) {
            return false;
        }
        DataAlterationRequest other = (DataAlterationRequest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.phsp.entity.DataAlterationRequest[ id=" + id + " ]";
    }

    public FuelTransaction getFuelTransaction() {
        return fuelTransaction;
    }

    public void setFuelTransaction(FuelTransaction fuelTransaction) {
        this.fuelTransaction = fuelTransaction;
    }

    public DataAlterationRequestType getDataAlterationRequestType() {
        return dataAlterationRequestType;
    }

    public void setDataAlterationRequestType(DataAlterationRequestType dataAlterationRequestType) {
        this.dataAlterationRequestType = dataAlterationRequestType;
    }

    public String getRequestComments() {
        return requestComments;
    }

    public void setRequestComments(String requestComments) {
        this.requestComments = requestComments;
    }

    public String getRejectComments() {
        return rejectComments;
    }

    public void setRejectComments(String rejectComments) {
        this.rejectComments = rejectComments;
    }

    public String getCompleteComments() {
        return completeComments;
    }

    public void setCompleteComments(String completeComments) {
        this.completeComments = completeComments;
    }

    public Date getRequestAt() {
        return requestAt;
    }

    public void setRequestAt(Date requestAt) {
        this.requestAt = requestAt;
    }

    public Date getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    public WebUser getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(WebUser requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Date getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Date rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Date getRejectedDate() {
        return rejectedDate;
    }

    public void setRejectedDate(Date rejectedDate) {
        this.rejectedDate = rejectedDate;
    }

    public WebUser getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(WebUser rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
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

    public Institution getRequestedInstitution() {
        return requestedInstitution;
    }

    public void setRequestedInstitution(Institution requestedInstitution) {
        this.requestedInstitution = requestedInstitution;
    }

    public Boolean getRejected() {
        return rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
    
    
    
}
