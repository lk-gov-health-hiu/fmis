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
package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Driver;
import lk.gov.health.phsp.facade.DriverFacade;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named
@ApplicationScoped
public class DriverApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private DriverFacade driverFacade;
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Driver> drivers;
    List<Driver> hospitals;
    // </editor-fold>

    public DriverApplicationController() {
    }


    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    private List<Driver> fillAllDrivers() {
        String j;
        Map m = new HashMap();
        j = "select i "
                + " from Driver i"
                + " where i.retired=:ret "
                + " order by i.name ";
        m.put("ret", false);
        return driverFacade.findByJpql(j, m);
    }

    public void resetAllDrivers() {
        drivers = null;
    }

// </editor-fold>
    public List<Driver> getDrivers() {
        if (drivers == null) {
            drivers = fillAllDrivers();
        }
        return drivers;
    }

    

    public List<Driver> findDriversByInstitution(Institution institution) {
        List<Driver> vs = new ArrayList<>();
        for (Driver driver : getDrivers()) {
            if (driver.getInstitution() != null && driver.getInstitution().equals(institution)) {
                vs.add(driver);
            }
        }
        return vs;
    }

    public List<Driver> findDriversByInstitutions(List<Institution> institutions) {
        List<Driver> vs = new ArrayList<>();
        for (Driver driver : getDrivers()) {
            if (driver.getInstitution() != null && institutions.contains(driver.getInstitution())) {
                vs.add(driver);
            }
        }
        return vs;
    }

   

    public void setDrivers(List<Driver> drivers) {
        this.drivers = drivers;
    }

    public Driver findDriverById(Long id) {
        Driver ins = null;
        for (Driver i : getDrivers()) {
            if (Objects.equals(i.getId(), id)) {
                ins = i;
                return ins;
            }
        }
        return ins;
    }

}
