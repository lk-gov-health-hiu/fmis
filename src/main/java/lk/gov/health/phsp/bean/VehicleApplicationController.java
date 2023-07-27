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
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.facade.VehicleFacade;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named
@ApplicationScoped
public class VehicleApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private VehicleFacade vehicleFacade;
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Vehicle> vehicles;
    List<Vehicle> hospitals;
    // </editor-fold>

    public VehicleApplicationController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Enums">
    public VehicleType[] getVehicleTypes() {
        return VehicleType.values();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    private List<Vehicle> fillAllVehicles() {
        String j;
        Map m = new HashMap();
        j = "select i "
                + " from Vehicle i"
                + " where i.retired=:ret "
                + " order by i.vehicleNumber ";
        m.put("ret", false);
        return vehicleFacade.findByJpql(j, m);
    }

    public void resetAllVehicles() {
        vehicles = null;
    }

// </editor-fold>
    public List<Vehicle> getVehicles() {
        if (vehicles == null) {
            vehicles = fillAllVehicles();
        }
        return vehicles;
    }

    public List<Vehicle> findVehicles(VehicleType type) {
        List<Vehicle> cins = getVehicles();
        List<Vehicle> tins = new ArrayList<>();
        for (Vehicle i : cins) {
            if (i.getVehicleType() == null) {
                continue;
            }
            if (i.getVehicleType().equals(type)) {
                tins.add(i);
            }
        }
        return tins;
    }

    public List<Vehicle> findVehicles(List<VehicleType> types) {
        List<Vehicle> cins = getVehicles();
        List<Vehicle> tins = new ArrayList<>();
        for (Vehicle i : cins) {
            boolean canInclude = false;
            if (i.getVehicleType() == null) {
                continue;
            }
            for (VehicleType type : types) {
                if (i.getVehicleType().equals(type)) {
                    canInclude = true;
                }
            }
            if (canInclude) {
                tins.add(i);
            }
        }
        return tins;
    }

    public List<Vehicle> findVehiclesByInstitution(Institution institution) {
        List<Vehicle> vs = new ArrayList<>();
        for (Vehicle vehicle : getVehicles()) {
            if (vehicle.getInstitution() != null && vehicle.getInstitution().equals(institution)) {
                vs.add(vehicle);
            }
        }
        return vs;
    }

    public List<Vehicle> findVehiclesByInstitutions(List<Institution> institutions) {
        List<Vehicle> vs = new ArrayList<>();
        for (Vehicle vehicle : getVehicles()) {
            if (vehicle.getInstitution() != null && institutions.contains(vehicle.getInstitution())) {
                vs.add(vehicle);
            }
        }
        return vs;
    }

    public List<Vehicle> findRegionalVehicles(List<VehicleType> types, Area rdhs) {
        List<Vehicle> cins = getVehicles();
        List<Vehicle> tins = new ArrayList<>();
        for (Vehicle i : cins) {
            boolean canInclude = false;
            if (i.getVehicleType() == null) {
                continue;
            }
            for (VehicleType type : types) {
                if (i.getVehicleType().equals(type)) {
                    canInclude = true;
                }
            }
            if (canInclude) {
                tins.add(i);
            }
        }
        return tins;
    }

    public boolean vehicleTypeCorrect(List<VehicleType> its, VehicleType it) {
        boolean correct = false;
        if (its == null || it == null) {
            return correct;
        }
        for (VehicleType tit : its) {
            if (tit.equals(it)) {
                correct = true;
            }
        }
        return correct;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Vehicle findVehicleById(Long id) {
        Vehicle ins = null;
        for (Vehicle i : getVehicles()) {
            if (Objects.equals(i.getId(), id)) {
                ins = i;
                return ins;
            }
        }
        return ins;
    }

}
