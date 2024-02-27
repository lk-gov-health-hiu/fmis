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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.facade.InstitutionFacade;
import org.apache.commons.codec.digest.DigestUtils;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named
@ApplicationScoped
public class InstitutionApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private InstitutionFacade institutionFacade;
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Institution> institutions;
    List<Institution> hospitals;
    List<Institution> fuelStations;
    private List<InstitutionType> hospitalTypes;
    private List<InstitutionType> fuelStationsTypes;
    private List<InstitutionType> covidDataHirachiInstitutions;
    // </editor-fold>

    public InstitutionApplicationController() {
    }

    // <editor-fold defaultstate="collapsed" desc="Enums">
    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    private List<Institution> fillAllInstitutions() {
        String j;
        Map m = new HashMap();
        j = "select i "
                + " from Institution i "
                + " where i.retired=:ret "
                + " order by i.name ";
        m.put("ret", false);
        return institutionFacade.findByJpql(j, m);
    }

    public void resetAllInstitutions() {
        institutions = null;
    }

// </editor-fold>
    public List<Institution> getInstitutions() {
        if (institutions == null) {
            institutions = fillAllInstitutions();
        }
        return institutions;
    }

    public Double getInstitutionStock(Institution ins) {
        for (Institution i : getInstitutions()) {
            if (i.equals(ins)) {
                return i.getCurrentStock();
            }
        }
        return null;
    }

    public Double addToStock(Institution ins, Double qty) {
        if (ins == null) {
            return null;
        }
        if (qty == null) {
            return null;
        }
        for (Institution i : getInstitutions()) {
            if (i.equals(ins)) {
                Double cs = i.getCurrentStock();
                cs = cs + qty;
                i.setCurrentStock(cs);
                ins.setCurrentStock(cs);
                institutionFacade.edit(i);
                return cs;
            }
        }
        return null;
    }

    public Double deductFromStock(Institution ins, Double qty) {
        if (qty == null) {
            return null;
        }
        Double deductQty = 0 - qty;
        return addToStock(ins, deductQty);
    }

    public List<Institution> findInstitutions(InstitutionType type) {
        List<Institution> cins = getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            if (i.getInstitutionType() == null) {
                continue;
            }
            if (i.getInstitutionType().equals(type)) {
                tins.add(i);
            }
        }
        return tins;
    }

    public List<Institution> findInstitutions(List<InstitutionType> types) {
        List<Institution> cins = getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            boolean canInclude = false;
            if (i.getInstitutionType() == null) {
                continue;
            }
            for (InstitutionType type : types) {
                if (i.getInstitutionType().equals(type)) {
                    canInclude = true;
                }
            }
            if (canInclude) {
                tins.add(i);
            }
        }
        return tins;
    }

    public List<Institution> findRegionalInstitutions(List<InstitutionType> types, Area rdhs) {
        List<Institution> cins = getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            boolean canInclude = false;
            if (i.getInstitutionType() == null) {
                continue;
            }
            for (InstitutionType type : types) {
                if (i.getInstitutionType().equals(type)) {
                    canInclude = true;
                }
            }
            if (canInclude) {
                if (i.getRdhsArea() != null && i.getRdhsArea().equals(rdhs)) {
                    canInclude = true;
                } else {
                    canInclude = false;
                }
            }
            if (canInclude) {
                tins.add(i);
            }
        }
        return tins;
    }

    public String getInstitutionHash() {
        return DigestUtils.md5Hex(getInstitutions().toString()).toUpperCase();
    }

    public List<Institution> getHospitals() {
        fillHospitals();
        return hospitals;
    }

    public void fillHospitals() {
        hospitals = new ArrayList<>();
        for (Institution i : getInstitutions()) {
            if (institutionTypeCorrect(getHospitalTypes(), i.getInstitutionType())) {
                hospitals.add(i);
            }
        }
    }

    public List<Institution> getFuelStations() {
        fillFuelStations();
        return fuelStations;
    }

    public void fillFuelStations() {
        fuelStations = new ArrayList<>();
        for (Institution i : getInstitutions()) {
            if (institutionTypeCorrect(getFuelStationTypes(), i.getInstitutionType())) {
                fuelStations.add(i);
            }
        }
    }

    public boolean institutionTypeCorrect(List<InstitutionType> its, InstitutionType it) {

        boolean correct = false;
        if (its == null || it == null) {
            return correct;
        }
        for (InstitutionType tit : its) {
            if (tit.equals(it)) {
                correct = true;
            }
        }
        return correct;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

    public List<InstitutionType> getHospitalTypes() {
        if (hospitalTypes == null || hospitalTypes.isEmpty()) {
            hospitalTypes = new ArrayList<>();
            hospitalTypes.add(InstitutionType.Base_Hospital);
            hospitalTypes.add(InstitutionType.District_General_Hospital);
            hospitalTypes.add(InstitutionType.Divisional_Hospital);
            hospitalTypes.add(InstitutionType.National_Hospital);
            hospitalTypes.add(InstitutionType.Primary_Medical_Care_Unit);
            hospitalTypes.add(InstitutionType.Teaching_Hospital);
        }
        return hospitalTypes;
    }

    public List<InstitutionType> getFuelStationTypes() {
        if (fuelStationsTypes == null || fuelStationsTypes.isEmpty()) {
            fuelStationsTypes = new ArrayList<>();
            fuelStationsTypes.add(InstitutionType.Fuel_Station);
            fuelStationsTypes.add(InstitutionType.CTB_Depot);
            fuelStationsTypes.add(InstitutionType.CTB_Country_Office);
            fuelStationsTypes.add(InstitutionType.CTB_Head_Office);
        }
        return fuelStationsTypes;
    }

    public Institution findInstitution(Long insId) {
        Institution ri = null;
        for (Institution i : getInstitutions()) {
            if (i.getId().equals(insId)) {
                ri = i;
            }
        }
        return ri;
    }

    public List<Institution> findChildrenInstitutions(Institution parentInstitution) {
        List<Institution> allInstitutions = getInstitutions();
        return findChildrenInstitutionsHelper1(parentInstitution, allInstitutions, new HashSet<>());
    }

    private List<Institution> findChildrenInstitutionsHelper1(Institution parentInstitution, List<Institution> allInstitutions, Set<Institution> processed) {
        Set<Institution> childrenSet = new HashSet<>();
        for (Institution institution : allInstitutions) {
            if (!processed.contains(institution) && institution.getParent() != null && institution.getParent().equals(parentInstitution)) {
                childrenSet.add(institution);
                processed.add(institution);
                childrenSet.addAll(findChildrenInstitutionsHelper(institution, allInstitutions, processed));
            }
        }
        return new ArrayList<>(childrenSet);
    }

    public List<Institution> findChildrenInstitutions(Institution parentInstitution, List<Institution> allInstitutions) {
        Set<Institution> visited = new HashSet<>();
        return findChildrenInstitutionsHelper(parentInstitution, allInstitutions, visited);
    }

    private List<Institution> findChildrenInstitutionsHelper(Institution parentInstitution, List<Institution> allInstitutions, Set<Institution> visited) {
        List<Institution> children = new ArrayList<>();

        for (Institution institution : allInstitutions) {
            if (!visited.contains(institution) && institution.getParent() != null && institution.getParent().equals(parentInstitution)) {
                visited.add(institution);
                children.add(institution);
                children.addAll(findChildrenInstitutionsHelper(institution, allInstitutions, visited));
            }
        }

        return children;
    }

    public Institution findMinistryOfHealth() {
        Institution moh = null;
        for (Institution i : getInstitutions()) {
            if (i.getInstitutionType() == InstitutionType.Ministry_of_Health) {
                moh = i;
                return moh;
            }
        }
        return moh;
    }

    public Institution findCpc() {
        Institution cpc = null;
        for (Institution i : getInstitutions()) {
            if (i.getInstitutionType() == InstitutionType.CPC_Head_Office) {
                cpc = i;
                return cpc;
            }
        }
        return cpc;
    }

    public Institution findInstitutionById(Long id) {
        Institution ins = null;
        for (Institution i : getInstitutions()) {
            if (Objects.equals(i.getId(), id)) {
                ins = i;
                return ins;
            }
        }
        return ins;
    }

    public List<Institution> findChildrenInstitutions(Institution ins, InstitutionType type) {
        List<Institution> cins = findChildrenInstitutions(ins);
        List<Institution> tins = new ArrayList<>();
        cins.stream().filter(i -> !(i.getParent() == null)).filter(i -> !(i.getInstitutionType() == null)).filter(i -> (i.getInstitutionType().equals(type))).forEachOrdered(i -> {
            tins.add(i);
        });
        return tins;
    }

    public List<InstitutionType> getCovidDataHirachiInstitutions() {
        if (covidDataHirachiInstitutions == null) {
            covidDataHirachiInstitutions = new ArrayList<>();
            covidDataHirachiInstitutions.add(InstitutionType.Ministry_of_Health);
            covidDataHirachiInstitutions.add(InstitutionType.Provincial_Department_of_Health_Services);
            covidDataHirachiInstitutions.add(InstitutionType.Regional_Department_of_Health_Department);
            covidDataHirachiInstitutions.add(InstitutionType.CTB_Head_Office);
        }
        return covidDataHirachiInstitutions;
    }

}
