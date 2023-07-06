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
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.VehicleFacade;
import org.apache.commons.codec.digest.DigestUtils;
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
    private List<VehicleType> hospitalTypes;
    private List<VehicleType> covidDataHirachiVehicles;
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
        j = "select i from Vehicle i where i.retired=:ret "
                + " order by i.name ";
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

    public String getVehicleHash() {
        return DigestUtils.md5Hex(getVehicles().toString()).toUpperCase();
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

    public Long findVehiclePopulationData(Vehicle tins, RelationshipType ttr, Integer ty) {

        if (ty == null) {
            // // System.out.println("No Year");
            return 0l;
        }
        if (tins == null) {
            // // System.out.println("No Vehicle");
            return 0l;
        }
        if (ttr == null) {
            // // System.out.println("No Relationship Type");
            return 0l;
        }

        String j = "select r from Relationship r "
                + " where r.retired<>:ret "
                + " and r.yearInt=:y";

        Map m = new HashMap();

        j += " and r.vehicle=:ins  ";
        j += " and r.relationshipType=:rt ";

        m.put("ins", tins);
        m.put("rt", ttr);
        m.put("y", ty);
        m.put("ret", true);

        // // System.out.println("m = " + m);
        // // System.out.println("j = " + j);
       
        return null;
    }

    
    

    public Vehicle findVehicle(Long insId) {
        Vehicle ri = null;
        for (Vehicle i : getVehicles()) {
            if (i.getId().equals(insId)) {
                ri = i;
            }
        }
        return ri;
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
