package lk.gov.health.phsp.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Last number issued for a running bill number series, one row per series
 * (e.g. one per province for CPC bill acceptance numbers). Numbers are never
 * reused: the row is locked, incremented and saved in the same transaction
 * as the bill that takes the number.
 */
@Entity
public class BillNumberCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String counterKey;

    private long lastNumber;

    public BillNumberCounter() {
    }

    public BillNumberCounter(String counterKey) {
        this.counterKey = counterKey;
    }

    public String getCounterKey() {
        return counterKey;
    }

    public void setCounterKey(String counterKey) {
        this.counterKey = counterKey;
    }

    public long getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(long lastNumber) {
        this.lastNumber = lastNumber;
    }

}
