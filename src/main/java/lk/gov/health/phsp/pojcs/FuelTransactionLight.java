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
import java.util.Date;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari at gmail.com>
 */
public class FuelTransactionLight implements Serializable {

    private Long id;
    private Date date;
    private String requestReferenceNumber;
    private String vehicleNumber;
    private Double requestQuantity;
    private Double issuedQuantity;
    private String issueReferenceNumber;
    private String fromInstitutionName;
    private String toInstitutionName;
    private String driverName; 

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
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
    
    

}
