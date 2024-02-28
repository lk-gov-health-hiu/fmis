package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.InstitutionFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.facade.AreaFacade;
import org.primefaces.model.file.UploadedFile;

@Named
@SessionScoped
public class InstitutionController implements Serializable {

    @EJB
    private InstitutionFacade ejbFacade;

    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    private ApplicationController applicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    private UserTransactionController userTransactionController;

    @Inject
    MenuController menuController;

    private List<Institution> items = null;
    private List<Institution> fuelStations = null;
    private List<Institution> institutionsWithoutfuelStations = null;
    private Institution selected;
    private Institution deleting;
    private List<Area> gnAreasOfSelected;
    private Area area;
    private Area removingArea;

    private InstitutionType institutionType;
    private Institution parent;
    private Area province;
    private Area pdhsArea;
    private Area district;
    private Area rdhsArea;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    private UploadedFile file;

    public Institution getInstitutionById(Long id) {
        return getFacade().find(id);
    }

    public Institution findHospital(Institution unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.getInstitutionType()) {
            case Base_Hospital:
            case District_General_Hospital:
            case Divisional_Hospital:
            case National_Hospital:
            case Teaching_Hospital:
            case Primary_Medical_Care_Unit:
                return unit;
            case CTB_Depot:
            case CTB_Head_Office:
            case Ministry_of_Health:
            case Other:
            case Provincial_Department_of_Health_Services:
            case Regional_Department_of_Health_Department:
            case Audit:
            default:
                if (unit.getParent() != null) {
                    return findHospital(unit.getParent());
                } else {
                    return null;
                }
        }
    }

    public String toAddInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        fillItems();
        return "/institution/institution";
    }

    public String toImportInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        return "/institution/import";
    }

    public String toEditInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        return "/institution/institution";
    }

    public boolean thisIsAParentInstitution(Institution checkingInstitution) {
        boolean flag = false;
        if (checkingInstitution == null) {
            return false;
        }
        for (Institution i : getItems()) {
            if (i.getParent() != null && i.getParent().equals(checkingInstitution)) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    public String deleteInstitution() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        if (thisIsAParentInstitution(deleting)) {
            JsfUtil.addErrorMessage("Can't delete. This has child institutions.");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        institutionApplicationController.getInstitutions().remove(deleting);
        institutionApplicationController.resetAllInstitutions();
        fillItems();
        return "/institution/list";
    }

    public String deleteFuelStation() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        if (thisIsAParentInstitution(deleting)) {
            JsfUtil.addErrorMessage("Can't delete. This has child institutions.");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        institutionApplicationController.getInstitutions().remove(deleting);
        return menuController.toListFuelStations();
    }

    public String toListInstitutions() {
        userTransactionController.recordTransaction("To List Institutions");
        return "/institution/list";
    }

    public String toSearchInstitutions() {
        return "/institution/search";
    }

    public void removeGnFromPmc() {
        if (removingArea == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        removingArea.setPmci(null);
        getAreaFacade().edit(removingArea);
        fillGnAreasOfSelected();
        removingArea = null;
        userTransactionController.recordTransaction("Remove Gn From Pmc");
    }

    public void fillGnAreasOfSelected() {
        if (selected == null) {
            gnAreasOfSelected = new ArrayList<>();
            return;
        }
        String j = "select a from Area a where a.retired=false "
                + " and a.type=:t "
                + " and a.pmci=:p "
                + " order by a.name";
        Map m = new HashMap();
        m.put("t", AreaType.GN);
        m.put("p", selected);
        gnAreasOfSelected = areaFacade.findByJpql(j, m);
        userTransactionController.recordTransaction("Fill Gn Areas Of Selected");
    }

    public List<Area> findDrainingGnAreas(Institution ins) {
        List<Area> gns;
        if (ins == null) {
            gns = new ArrayList<>();
            return gns;
        }
        String j = "select a from Area a where a.retired=false "
                + " and a.type=:t "
                + " and a.pmci=:p "
                + " order by a.name";
        Map m = new HashMap();
        m.put("t", AreaType.GN);
        m.put("p", ins);
        gns = areaFacade.findByJpql(j, m);
        return gns;
    }

    public InstitutionController() {
    }

    public Institution getSelected() {
        return selected;
    }

    public void setSelected(Institution selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private InstitutionFacade getFacade() {
        return ejbFacade;
    }

    public List<Institution> findChildrenPmcis(Institution ins) {
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins) && i.isPmci()) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenPmcis(i));
            }
        }
        return tins;
    }

    public List<Institution> findChildrenPmcis(Institution ins, String qry) {
        if (qry == null || qry.trim().equals("")) {
            return null;
        }
        qry = qry.toLowerCase();
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins) && i.isPmci()) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenPmcis(i));
            }
        }
        List<Institution> ttins = new ArrayList<>();
        for (Institution i : tins) {
            if (i.getName().toLowerCase().contains(qry)) {
                ttins.add(i);
            }
        }
        return ttins;
    }

    public List<Institution> findInstitutions(InstitutionType type) {
        return institutionApplicationController.findInstitutions(type);
    }

    public List<Institution> findInstitutions(Area area, InstitutionType type) {
        List<Institution> cins = institutionApplicationController.getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            if (type != null) {
                if (i.getInstitutionType() == null) {
                    continue;
                }
                if (!i.getInstitutionType().equals(type)) {
                    continue;
                }
            }
            if (area.getType() == AreaType.District) {
                if (i.getDistrict() == null) {
                    continue;
                }
                if (i.getDistrict().equals(area)) {
                    tins.add(i);
                }
            } else if (area.getType() == AreaType.Province) {
                if (i.getProvince() == null) {
                    continue;
                }
                if (i.getProvince().equals(area)) {
                    tins.add(i);
                }
            }

        }
        return tins;
    }

    public List<Institution> completeInstitutions(String nameQry) {
        List<InstitutionType> ts = Arrays.asList(InstitutionType.values());
        if (ts == null) {
            ts = new ArrayList<>();
        }
        return fillInstitutions(ts, nameQry, null);
    }

    public List<Institution> completeHlClinics(String nameQry) {
        return fillInstitutions(InstitutionType.CTB_Depot, nameQry, null);
    }

    public List<Institution> completeFuelStations(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.CTB_Depot);
        its.add(InstitutionType.CTB_Country_Office);
        its.add(InstitutionType.CTB_Head_Office);
        its.add(InstitutionType.Fuel_Station);
        return fillInstitutions(its, qry, null);
    }

    public List<Institution> completeFuelRequestingFromInstitutions(String qry) {
        List<InstitutionType> its = fuelRequestsFromInstitutionTypes();
        return fillInstitutions(its, qry, null);
    }

    public List<Institution> completeMohs(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.CTB_Head_Office);
        return fillInstitutions(its, qry, null);
    }

    public List<InstitutionType> hospitalInstitutionTypes() {
        List<InstitutionType> ts = new ArrayList<>();
        InstitutionType[] ta = InstitutionType.values();
        for (InstitutionType t : ta) {
            switch (t) {
                case Base_Hospital:
                case District_General_Hospital:
                case National_Hospital:
                case Primary_Medical_Care_Unit:
                case Teaching_Hospital:
                case Divisional_Hospital:
                    ts.add(t);
                    break;
            }
        }
        return ts;
    }

    public List<InstitutionType> fuelRequestsFromInstitutionTypes() {
        List<InstitutionType> ts = new ArrayList<>();
        InstitutionType[] ta = InstitutionType.values();
        for (InstitutionType t : ta) {
            switch (t) {
                case Base_Hospital:
                case District_General_Hospital:
                case National_Hospital:
                case Primary_Medical_Care_Unit:
                case Teaching_Hospital:
                case Divisional_Hospital:
                case Hospital:
                case MOH_Office:
                case Ministry_of_Health:
                case OtherSpecializedUnit:
                case Provincial_Department_of_Health_Services:
                case Provincial_General_Hospital:
                case Regional_Department_of_Health_Department:
                case Indigenous_Medicine_Department:
                case Ayurvedic_Department:
                case Provincial_Ayurvedic_Department:
                case District_Ayurvedic_Department:
                case Ayurvedic_Hospital:
                case Herbal_Guardian:
                    ts.add(t);
                    break;
                case Audit:
                case CB:
                case CPC_Depot:
                case CPC_Head_Office:
                case CTB_Country_Office:
                case CTB_Depot:
                case CTB_Head_Office:
                case DistrictSecretariat:
                case Donar:
                case ERD:
                case Fuel_Station:
                case Other:
                case Other_Ministry:
                case Police_Department:
                case Police_Station:
                    break;
            }
        }
        return ts;
    }

    public List<Institution> completeHospitals(String nameQry) {
        return fillInstitutions(hospitalInstitutionTypes(), nameQry, null);
    }

    public List<Institution> completeRdhs(String nameQry) {
        return fillInstitutions(InstitutionType.Regional_Department_of_Health_Department, nameQry, null);
    }

    public List<Institution> completePdhs(String nameQry) {
        return fillInstitutions(InstitutionType.Provincial_Department_of_Health_Services, nameQry, null);
    }

    public List<Institution> completeProcedureRooms(String nameQry) {
        return fillInstitutions(InstitutionType.CTB_Depot, nameQry, null);
    }

    public Institution findInstitutionByName(String name) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        Institution ni = null;
        for (Institution i : institutionApplicationController.getInstitutions()) {
            if (i.getName() != null && i.getName().equalsIgnoreCase(name)) {
                if (ni != null) {
                    // // System.out.println("Duplicate Institution Name : " + name);
                }
                ni = i;
            }
        }
        return ni;
    }

    public void fillItems() {
        items = institutionApplicationController.getInstitutions();
    }

    public void resetAllInstitutions() {
        items = null;
        institutionApplicationController.resetAllInstitutions();
        items = institutionApplicationController.getInstitutions();
    }

    public List<Institution> fillInstitutions(InstitutionType type, String nameQry, Institution parent) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();

        for (Institution i : allIns) {
            boolean canInclude = true;
            if (parent != null) {
                if (i.getParent() == null) {
                    canInclude = false;
                } else {
                    if (!i.getParent().equals(parent)) {
                        canInclude = false;
                    }
                }
            }
            if (type != null) {
                if (i.getInstitutionType() == null) {
                    canInclude = false;
                } else {
                    if (!i.getInstitutionType().equals(type)) {
                        canInclude = false;
                    }
                }
            }
            if (i.getName() == null || i.getName().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }
            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public List<Institution> fillInstitutions(List<InstitutionType> types, String nameQry, Institution parent) {
        List<Institution> resIns = new ArrayList<>();
        List<Institution> allIns = institutionApplicationController.getInstitutions();

        for (Institution i : allIns) {
            boolean canInclude = true;
            if (parent != null) {
                if (i.getParent() == null || !i.getParent().equals(parent)) {
                    canInclude = false;
                }
            }
            boolean typeFound = false;
            for (InstitutionType type : types) {
                if (type != null && i.getInstitutionType() != null && i.getInstitutionType().equals(type)) {
                    typeFound = true;
                    break; // Stop checking types once a match is found
                }
            }
            if (!typeFound) {
                canInclude = false;
            }

            // Check name only if nameQry is provided and not empty
            if (nameQry != null && !nameQry.trim().isEmpty()) {
                if (i.getName() == null || !i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }

            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public List<Institution> findInstitutionsByMainFuelStation(Institution fuelStation) {
        List<Institution> instittuionsForFuelStationAsMain = new ArrayList<>();
        if (fuelStation == null) {
            return instittuionsForFuelStationAsMain;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        for (Institution i : allIns) {
            if (i.getSupplyInstitution() != null) {
                if (i.getSupplyInstitution().equals(fuelStation)) {
                    instittuionsForFuelStationAsMain.add(i);
                }
            }
        }
        return instittuionsForFuelStationAsMain;
    }

    public List<Institution> fillInstitutions(List<InstitutionType> types) {
        return fillInstitutions(types, null, null);
    }

    public List<Institution> completeInstitutionsByWords(String nameQry) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        nameQry = nameQry.trim();
        String words[] = nameQry.split("\\s+");

        for (Institution i : allIns) {
            boolean allWordsMatch = true;

            for (String word : words) {
                boolean thisWordMatch;
                word = word.trim().toLowerCase();
                if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getTname() != null && i.getTname().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getSname() != null && i.getSname().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else {
                    thisWordMatch = false;
                }
                if (thisWordMatch == false) {
                    allWordsMatch = false;
                }
            }

            if (allWordsMatch) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public Institution prepareCreate() {
        selected = new Institution();
        initializeEmbeddableKey();
        return selected;
    }

    public void prepareToAddNewInstitution() {
        selected = new Institution();
    }

    public void prepareToAddNewFuelStation() {
        selected = new Institution();
        selected.setInstitutionType(InstitutionType.Fuel_Station);
    }

    public void prepareToListInstitution() {
        if (webUserController.getLoggedUser() == null) {
            items = null;
        }
        if (webUserController.getLoggedUser().getWebUserRoleLevel() == WebUserRoleLevel.HEALTH_MINISTRY) {
            items = institutionApplicationController.getInstitutions();

        } else {
            items = webUserController.findAutherizedInstitutions();
        }
    }

    public void prepareToListFuelStations() {
        institutionApplicationController.fillFuelStations();
        fuelStations = institutionApplicationController.getFuelStations();
    }

    public String saveOrUpdateInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return "";
        }
        if (selected.getName() == null || selected.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Name is required");
            return "";
        }

        if (selected.getSname() == null || selected.getSname().trim().equals("")) {
            selected.setSname(selected.getName());
        }

        if (selected.getTname() == null || selected.getTname().trim().equals("")) {
            selected.setTname(selected.getName());
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);

            institutionApplicationController.getInstitutions().add(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
        institutionApplicationController.resetAllInstitutions();
        return menuController.toListInstitutions();
    }

    public String saveOrUpdateInstitutionHealth() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return "";
        }
        if (selected.getName() == null || selected.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Name is required");
            return "";
        }

        if (selected.getSname() == null || selected.getSname().trim().equals("")) {
            selected.setSname(selected.getName());
        }

        if (selected.getTname() == null || selected.getTname().trim().equals("")) {
            selected.setTname(selected.getName());
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);

            institutionApplicationController.getInstitutions().add(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
        institutionApplicationController.resetAllInstitutions();
        return menuController.toListInstitutionsHealth();
    }

    public String updateMyInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return "";
        }
        if (selected.getName() == null || selected.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Name is required");
            return "";
        }

        if (selected.getSname() == null || selected.getSname().trim().equals("")) {
            selected.setSname(selected.getName());
        }

        if (selected.getTname() == null || selected.getTname().trim().equals("")) {
            selected.setTname(selected.getName());
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);

            institutionApplicationController.getInstitutions().add(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
        return menuController.toAdministrationIndex();
    }

    public String saveOrUpdateFuelStation() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return "";
        }
        if (selected.getName() == null || selected.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Name is required");
            return "";
        }

        if (selected.getSname() == null || selected.getSname().trim().equals("")) {
            selected.setSname(selected.getName());
        }

        if (selected.getTname() == null || selected.getTname().trim().equals("")) {
            selected.setTname(selected.getName());
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);
            institutionApplicationController.getInstitutions().add(selected);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        return menuController.toListFuelStations();
    }

    public void save(Institution ins) {
        if (ins == null) {
            return;
        }
        if (ins.getId() == null) {
            ins.setCreatedAt(new Date());
            ins.setCreater(webUserController.getLoggedUser());
            getFacade().create(ins);
            institutionApplicationController.getInstitutions().add(ins);
            items = null;
        } else {
            ins.setEditedAt(new Date());
            ins.setEditer(webUserController.getLoggedUser());
            getFacade().edit(ins);
            items = null;
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            institutionApplicationController.getInstitutions().add(selected);
            fillItems();
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Institution> getItems() {
        if (items == null) {
            fillItems();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Institution getInstitution(java.lang.Long id) {
        Institution ni = null;
        for (Institution i : institutionApplicationController.getInstitutions()) {
            if (i.getId() != null && i.getId().equals(id)) {
                ni = i;
            }
        }
        return ni;
    }

    public lk.gov.health.phsp.facade.InstitutionFacade getEjbFacade() {
        return ejbFacade;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Area> getGnAreasOfSelected() {
        if (gnAreasOfSelected == null) {
            gnAreasOfSelected = new ArrayList<>();
        }
        return gnAreasOfSelected;
    }

    public void setGnAreasOfSelected(List<Area> gnAreasOfSelected) {
        this.gnAreasOfSelected = gnAreasOfSelected;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Area getRemovingArea() {
        return removingArea;
    }

    public void setRemovingArea(Area removingArea) {
        this.removingArea = removingArea;

    }

    public Institution getDeleting() {
        return deleting;
    }

    public void setDeleting(Institution deleting) {
        this.deleting = deleting;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public Institution getParent() {
        return parent;
    }

    public void setParent(Institution parent) {
        this.parent = parent;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getPdhsArea() {
        return pdhsArea;
    }

    public void setPdhsArea(Area pdhsArea) {
        this.pdhsArea = pdhsArea;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getRdhsArea() {
        return rdhsArea;
    }

    public void setRdhsArea(Area rdhsArea) {
        this.rdhsArea = rdhsArea;
    }

    public List<Institution> getFuelStations() {
        return fuelStations;
    }

    public void setFuelStations(List<Institution> fuelStations) {
        this.fuelStations = fuelStations;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public List<Institution> getInstitutionsWithoutfuelStations() {

        List<InstitutionType> types = new ArrayList<>();
        types.add(InstitutionType.Base_Hospital);
        types.add(InstitutionType.District_General_Hospital);
        types.add(InstitutionType.Divisional_Hospital);
        types.add(InstitutionType.Hospital);
        types.add(InstitutionType.MOH_Office);
        types.add(InstitutionType.Ministry_of_Health);
        types.add(InstitutionType.National_Hospital);
        types.add(InstitutionType.Other);
        types.add(InstitutionType.OtherSpecializedUnit);
        types.add(InstitutionType.Other_Ministry);
        types.add(InstitutionType.Primary_Medical_Care_Unit);
        types.add(InstitutionType.Provincial_Department_of_Health_Services);
        types.add(InstitutionType.Provincial_General_Hospital);
        types.add(InstitutionType.Regional_Department_of_Health_Department);
        types.add(InstitutionType.Teaching_Hospital);
        types.add(InstitutionType.Base_Hospital);
        types.add(InstitutionType.Ayurvedic_Department);
        types.add(InstitutionType.Ayurvedic_Hospital);
        types.add(InstitutionType.Provincial_Ayurvedic_Department);
        types.add(InstitutionType.District_Ayurvedic_Department);
        types.add(InstitutionType.Herbal_Guardian);
        types.add(InstitutionType.Other);
        types.add(InstitutionType.Indigenous_Medicine_Department);
        types.add(InstitutionType.District_Ayurvedic_Department);

        /**
         * Indigenous_Medicine_Department("Indigenous Medicine Department"),
         * Ayurvedic_Department("Ayurvedic Department"),
         * Ayurvedic_Hospital("Ayurvedic Hospital"),
         * Provincial_Ayurvedic_Department("Provincial Ayurvedic Department"),
         * District_Ayurvedic_Department("District Ayurvedic Department"),
         * Herbal_Guardian("Herbal Guardian"),
         */
        institutionsWithoutfuelStations = fillInstitutions(types, null, null);

        return institutionsWithoutfuelStations;
    }

    public List<Institution> fillInstitutionsWithoutfuelStations() {
        if (institutionsWithoutfuelStations == null) {
            List<InstitutionType> types = new ArrayList<>();
            types.add(InstitutionType.Base_Hospital);
            types.add(InstitutionType.District_General_Hospital);
            types.add(InstitutionType.Divisional_Hospital);
            types.add(InstitutionType.Hospital);
            types.add(InstitutionType.MOH_Office);
            types.add(InstitutionType.Ministry_of_Health);
            types.add(InstitutionType.National_Hospital);
            types.add(InstitutionType.Other);
            types.add(InstitutionType.OtherSpecializedUnit);
            types.add(InstitutionType.Other_Ministry);
            types.add(InstitutionType.Primary_Medical_Care_Unit);
            types.add(InstitutionType.Provincial_Department_of_Health_Services);
            types.add(InstitutionType.Provincial_General_Hospital);
            types.add(InstitutionType.Regional_Department_of_Health_Department);
            types.add(InstitutionType.Teaching_Hospital);
            types.add(InstitutionType.Base_Hospital);
            types.add(InstitutionType.Indigenous_Medicine_Department);
            types.add(InstitutionType.Ayurvedic_Department);
            types.add(InstitutionType.Ayurvedic_Hospital);
            types.add(InstitutionType.Herbal_Guardian);
            types.add(InstitutionType.Provincial_Ayurvedic_Department);
            types.add(InstitutionType.District_Ayurvedic_Department);

            institutionsWithoutfuelStations = fillInstitutions(types, null, null);
        }
        return institutionsWithoutfuelStations;
    }

    public void setInstitutionsWithoutfuelStations(List<Institution> institutionsWithoutfuelStations) {
        this.institutionsWithoutfuelStations = institutionsWithoutfuelStations;
    }

    @FacesConverter(forClass = Institution.class)
    public static class InstitutionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getInstitution(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Institution.class.getName()});
                return null;
            }
        }

    }

}
