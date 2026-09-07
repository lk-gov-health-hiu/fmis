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
package lk.gov.health.phsp.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import lk.gov.health.phsp.entity.FuelPrice;
import lk.gov.health.phsp.facade.FuelPriceFacade;

/**
 * Caches the fuel price schedule (one row per effective-from date) and
 * resolves which price applies on a given date. A price is in effect from
 * 00:00:00 of its effectiveFrom date until the day before the next price's
 * effectiveFrom date (or indefinitely, for the latest price).
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Named
@ApplicationScoped
public class FuelPriceApplicationController {

    @EJB
    private FuelPriceFacade fuelPriceFacade;

    private List<FuelPrice> prices;

    private List<FuelPrice> fillAllPrices() {
        String j = "select f "
                + " from FuelPrice f"
                + " where f.retired=:ret "
                + " order by f.effectiveFrom";
        Map m = new HashMap();
        m.put("ret", false);
        return fuelPriceFacade.findByJpql(j, m);
    }

    public void resetAllPrices() {
        prices = null;
    }

    public List<FuelPrice> getPrices() {
        if (prices == null) {
            prices = fillAllPrices();
        }
        return prices;
    }

    private Date dateOnly(Date d) {
        if (d == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * The FuelPrice in effect on the given date, i.e. the row with the
     * latest effectiveFrom that is not after this date. Returns null if the
     * date is before the earliest recorded price, or no prices exist.
     */
    public FuelPrice getPriceEffectiveOn(Date date) {
        if (date == null) {
            return null;
        }
        Date d = dateOnly(date);
        FuelPrice applicable = null;
        for (FuelPrice fp : getPrices()) {
            if (fp.getEffectiveFrom() == null) {
                continue;
            }
            if (!fp.getEffectiveFrom().after(d)) {
                applicable = fp;
            } else {
                break;
            }
        }
        return applicable;
    }

    /**
     * True if a FuelPrice already exists (other than skipId, when editing)
     * with this exact effectiveFrom date.
     */
    public boolean existsWithEffectiveFrom(Date effectiveFrom, Long skipId) {
        if (effectiveFrom == null) {
            return false;
        }
        Date d = dateOnly(effectiveFrom);
        for (FuelPrice fp : getPrices()) {
            if (Objects.equals(fp.getId(), skipId)) {
                continue;
            }
            if (fp.getEffectiveFrom() != null && dateOnly(fp.getEffectiveFrom()).equals(d)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Distinct FuelPrice blocks applicable to the given transaction dates,
     * ordered by effectiveFrom. Used to warn hospital account-branch users
     * when a set of transactions they are about to bill spans more than one
     * price.
     */
    public List<FuelPrice> distinctPricesFor(List<Date> dates) {
        List<FuelPrice> result = new ArrayList<>();
        if (dates == null) {
            return result;
        }
        for (Date d : dates) {
            FuelPrice fp = getPriceEffectiveOn(d);
            if (fp != null && !result.contains(fp)) {
                result.add(fp);
            }
        }
        result.sort((a, b) -> a.getEffectiveFrom().compareTo(b.getEffectiveFrom()));
        return result;
    }

    public FuelPrice findById(Long id) {
        for (FuelPrice fp : getPrices()) {
            if (Objects.equals(fp.getId(), id)) {
                return fp;
            }
        }
        return null;
    }

}
