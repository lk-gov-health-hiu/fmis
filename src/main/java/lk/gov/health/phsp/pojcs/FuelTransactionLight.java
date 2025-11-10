/*
 * The MIT License
 *
 * Copyright 2024 Dr M H B Ariyaratne <buddhika.ari at gmail.com>.
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
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import lk.gov.health.phsp.enums.FuelTransactionType;
import javax.persistence.Transient;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari at gmail.com>
 */
public class FuelTransactionLight implements Serializable {

    private Long id;
    private Date date;
    private FuelTransactionType transactionType;
    private Date issuedDate;
    private String requestReferenceNumber;
    private String vehicleNumber;
    private Double requestQuantity;
    private Double issuedQuantity;
    private String issueReferenceNumber;
    private String fromInstitutionName;
    private String toInstitutionName;
    String toInstitutionCode;
    private String driverName;
    private boolean cancelled;
    private boolean rejected;
    private boolean retired;
    private boolean issued;
    private boolean dispensed;
    private Date submittedToPaymentAt;

    public FuelTransactionLight() {
    }

    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
    }

    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String fromInstitutionName, String toInstitutionName) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
    }

    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String fromInstitutionName, String toInstitutionName,
            String driverName) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
    }

    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String fromInstitutionName, String toInstitutionName,
            String driverName,
            Date issedDate) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.issuedDate = issedDate;
    }

    //New
    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String toInstitutionCode) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.toInstitutionCode = toInstitutionCode;
    }

    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String toInstitutionCode,
            Date issedDate) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.toInstitutionCode = toInstitutionCode;
        this.issuedDate = issedDate;
    }


    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String fromInstitutionName, String toInstitutionName,
            String driverName,
            String toInstitutionCode) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.toInstitutionCode = toInstitutionCode;
    }

    // New constructor with transactionType
    public FuelTransactionLight(Long id, Date date, FuelTransactionType transactionType,
                                String requestReferenceNumber,
                                String vehicleNumber, Double requestQuantity,
                                Double issuedQuantity, String issueReferenceNumber,
                                String fromInstitutionName, String toInstitutionName,
                                String driverName,
                                String toInstitutionCode) {
        this.id = id;
        this.date = date;
        this.transactionType = transactionType;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.toInstitutionCode = toInstitutionCode;
    }

    // Constructor with both transactionType and issuedDate (13 parameters)
    public FuelTransactionLight(Long id, Date date, FuelTransactionType transactionType,
                                String requestReferenceNumber,
                                String vehicleNumber, Double requestQuantity,
                                Double issuedQuantity, String issueReferenceNumber,
                                String fromInstitutionName, String toInstitutionName,
                                String driverName,
                                String toInstitutionCode,
                                Date issedDate) {
        this.id = id;
        this.date = date;
        this.transactionType = transactionType;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.toInstitutionCode = toInstitutionCode;
        this.issuedDate = issedDate;
    }

    public FuelTransactionLight(Long id, Date date, String requestReferenceNumber,
            String vehicleNumber, Double requestQuantity,
            Double issuedQuantity, String issueReferenceNumber,
            String fromInstitutionName, String toInstitutionName,
            String driverName,
            String toInstitutionCode,
            Date issedDate) {
        this.id = id;
        this.date = date;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.toInstitutionCode = toInstitutionCode;
        this.issuedDate = issedDate;
    }

    // Constructor with status flags (18 parameters)
    public FuelTransactionLight(Long id, Date date, FuelTransactionType transactionType,
                                String requestReferenceNumber,
                                String vehicleNumber, Double requestQuantity,
                                Double issuedQuantity, String issueReferenceNumber,
                                String fromInstitutionName, String toInstitutionName,
                                String driverName,
                                String toInstitutionCode,
                                Date issuedDate,
                                boolean cancelled, boolean rejected, boolean retired,
                                boolean issued, boolean dispensed) {
        this.id = id;
        this.date = date;
        this.transactionType = transactionType;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.toInstitutionCode = toInstitutionCode;
        this.issuedDate = issuedDate;
        this.cancelled = cancelled;
        this.rejected = rejected;
        this.retired = retired;
        this.issued = issued;
        this.dispensed = dispensed;
    }

    // Constructor with status flags and submitted to payment date (19 parameters)
    public FuelTransactionLight(Long id, Date date, FuelTransactionType transactionType,
                                String requestReferenceNumber,
                                String vehicleNumber, Double requestQuantity,
                                Double issuedQuantity, String issueReferenceNumber,
                                String fromInstitutionName, String toInstitutionName,
                                String driverName,
                                String toInstitutionCode,
                                Date issuedDate,
                                boolean cancelled, boolean rejected, boolean retired,
                                boolean issued, boolean dispensed,
                                Date submittedToPaymentAt) {
        this.id = id;
        this.date = date;
        this.transactionType = transactionType;
        this.requestReferenceNumber = requestReferenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.requestQuantity = requestQuantity;
        this.issuedQuantity = issuedQuantity;
        this.issueReferenceNumber = issueReferenceNumber;
        this.fromInstitutionName = fromInstitutionName;
        this.toInstitutionName = toInstitutionName;
        this.driverName = driverName;
        this.toInstitutionCode = toInstitutionCode;
        this.issuedDate = issuedDate;
        this.cancelled = cancelled;
        this.rejected = rejected;
        this.retired = retired;
        this.issued = issued;
        this.dispensed = dispensed;
        this.submittedToPaymentAt = submittedToPaymentAt;
    }

    public String getToInstitutionCode() {
        return toInstitutionCode;
    }

    public void setToInstitutionCode(String toInstitutionCode) {
        this.toInstitutionCode = toInstitutionCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    @Transient
    public int getWeekOfTheYear() {
        if (date == null) {
            return 0;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    @Transient
    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        String pattern = "dd MMMM yyyy";
        DateFormat sfd = new SimpleDateFormat(pattern);
        return sfd.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRequestReferenceNumber() {
        return requestReferenceNumber;
    }

    public void setRequestReferenceNumber(String requestReferenceNumber) {
        this.requestReferenceNumber = requestReferenceNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
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

    public String getIssueReferenceNumber() {
        return issueReferenceNumber;
    }

    public void setIssueReferenceNumber(String issueReferenceNumber) {
        this.issueReferenceNumber = issueReferenceNumber;
    }

    public String getFromInstitutionName() {
        return fromInstitutionName;
    }

    public void setFromInstitutionName(String fromInstitutionName) {
        this.fromInstitutionName = fromInstitutionName;
    }

    public String getToInstitutionName() {
        return toInstitutionName;
    }

    public void setToInstitutionName(String toInstitutionName) {
        this.toInstitutionName = toInstitutionName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public FuelTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(FuelTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionTypeLabel() {
        if (transactionType != null) {
            return transactionType.getLabel();
        }
        return "";
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getStatus() {
        if (cancelled) {
            return "Cancelled";
        }
        if (rejected) {
            return "Rejected";
        }
        if (retired) {
            return "Retired";
        }
        if (dispensed) {
            return "Dispensed";
        }
        if (issued) {
            return "Confirmed";
        }
        return "Requested";
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public boolean isIssued() {
        return issued;
    }

    public void setIssued(boolean issued) {
        this.issued = issued;
    }

    public boolean isDispensed() {
        return dispensed;
    }

    public void setDispensed(boolean dispensed) {
        this.dispensed = dispensed;
    }

    public Date getSubmittedToPaymentAt() {
        return submittedToPaymentAt;
    }

    public void setSubmittedToPaymentAt(Date submittedToPaymentAt) {
        this.submittedToPaymentAt = submittedToPaymentAt;
    }

}
