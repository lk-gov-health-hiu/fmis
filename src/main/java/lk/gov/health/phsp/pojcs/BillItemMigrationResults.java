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
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Holds results from the BillItem migration process
 *
 * @author buddh
 */
public class BillItemMigrationResults implements Serializable {

    private int billsProcessed;
    private int billsSkipped;
    private int billsMigrated;
    private int billItemsCreated;
    private int errorsCount;
    private List<String> errors;
    private Date startTime;
    private Date endTime;

    public BillItemMigrationResults() {
        this.errors = new ArrayList<>();
    }

    public String getDuration() {
        if (startTime == null || endTime == null) {
            return "N/A";
        }
        long durationMs = endTime.getTime() - startTime.getTime();
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + " minutes " + seconds + " seconds";
    }

    public void addError(String error) {
        this.errors.add(error);
        this.errorsCount++;
    }

    // Getters and Setters

    public int getBillsProcessed() {
        return billsProcessed;
    }

    public void setBillsProcessed(int billsProcessed) {
        this.billsProcessed = billsProcessed;
    }

    public int getBillsSkipped() {
        return billsSkipped;
    }

    public void setBillsSkipped(int billsSkipped) {
        this.billsSkipped = billsSkipped;
    }

    public int getBillsMigrated() {
        return billsMigrated;
    }

    public void setBillsMigrated(int billsMigrated) {
        this.billsMigrated = billsMigrated;
    }

    public int getBillItemsCreated() {
        return billItemsCreated;
    }

    public void setBillItemsCreated(int billItemsCreated) {
        this.billItemsCreated = billItemsCreated;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public void setErrorsCount(int errorsCount) {
        this.errorsCount = errorsCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
