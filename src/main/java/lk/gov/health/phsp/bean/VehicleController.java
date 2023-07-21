package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.VehicleFacade;

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
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.facade.AreaFacade;
import org.primefaces.model.file.UploadedFile;

@Named
@SessionScoped
public class VehicleController implements Serializable {

    @EJB
    private VehicleFacade ejbFacade;

    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    private ApplicationController applicationController;
    @Inject
    VehicleApplicationController vehicleApplicationController;
    @Inject
    private UserTransactionController userTransactionController;

    private List<Vehicle> items = null;
    private Vehicle selected;
    private Vehicle deleting;
    private List<Vehicle> myClinics;
    private List<Area> gnAreasOfSelected;
    private Area area;
    private Area removingArea;

    private VehicleType vehicleType;
    private Vehicle parent;
    private Area province;
    private Area pdhsArea;
    private Area district;
    private Area rdhsArea;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    private UploadedFile file;

    public Vehicle getVehicleById(Long id) {
        return getFacade().find(id);
    }

    public String toAddVehicle() {
        selected = new Vehicle();
        userTransactionController.recordTransaction("To Add Vehicle");
        fillItems();
        return "/vehicle/vehicle";
    }

    public String toImportVehicle() {
        selected = new Vehicle();
        userTransactionController.recordTransaction("To Add Vehicle");
        return "/vehicle/import";
    }

    public String toEditVehicle() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        return "/vehicle/vehicle";
    }

    public String deleteVehicle() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }

        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        vehicleApplicationController.getVehicles().remove(deleting);
        fillItems();
        return "/vehicle/list";
    }

    public String toListVehicles() {
        userTransactionController.recordTransaction("To List Vehicles");
        return "/vehicle/list";
    }

    public String toSearchVehicles() {
        return "/vehicle/search";
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

    public List<Vehicle> searchVehicles(String searchingText) {
        List<Vehicle> allVehicles = vehicleApplicationController.getVehicles();
        List<Vehicle> matchingVehicles = new ArrayList<>();

        String cleanSearchingText = searchingText.replaceAll("\\W", "").toLowerCase();

        // Priority 1: exact matches
        for (Vehicle vehicle : allVehicles) {
            String vehicleNumber = vehicle.getVehicleNumber().replaceAll("\\W", "").toLowerCase();
            if (vehicleNumber.equals(cleanSearchingText)) {
                matchingVehicles.add(vehicle);
            }
        }

        // If there are no exact matches, move to priority 2
        if (matchingVehicles.isEmpty()) {
            String[] searchTerms = cleanSearchingText.split("");
            for (Vehicle vehicle : allVehicles) {
                String vehicleNumber = vehicle.getVehicleNumber().replaceAll("\\W", "").toLowerCase();
                boolean allTermsMatch = true;
                for (String term : searchTerms) {
                    if (!vehicleNumber.contains(term)) {
                        allTermsMatch = false;
                        break;
                    }
                }
                if (allTermsMatch) {
                    matchingVehicles.add(vehicle);
                }
            }
        }

        return matchingVehicles;
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

    public List<Area> findDrainingGnAreas(Vehicle ins) {
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

    public VehicleController() {
    }

    public Vehicle getSelected() {
        return selected;
    }

    public void setSelected(Vehicle selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private VehicleFacade getFacade() {
        return ejbFacade;
    }

    public List<Vehicle> findVehicles(VehicleType type) {
        return vehicleApplicationController.findVehicles(type);
    }

    public List<Vehicle> completeVehicles(String nameQry) {
        List<VehicleType> ts = Arrays.asList(VehicleType.values());
        if (ts == null) {
            ts = new ArrayList<>();
        }
        return fillVehicles(ts, nameQry);
    }

    public Vehicle findVehicleByName(String name) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        Vehicle ni = null;
        for (Vehicle i : vehicleApplicationController.getVehicles()) {
            if (i.getName() != null && i.getName().equalsIgnoreCase(name)) {
                if (ni != null) {
                    // // System.out.println("Duplicate Vehicle Name : " + name);
                }
                ni = i;
            }
        }
        return ni;
    }

    public void fillItems() {
        if (vehicleApplicationController.getVehicles() != null) {
            items = vehicleApplicationController.getVehicles();
            return;
        }
    }

    public void resetAllVehicles() {
        items = null;
        vehicleApplicationController.resetAllVehicles();
        items = vehicleApplicationController.getVehicles();
    }

    public List<Vehicle> fillVehicles(VehicleType type, String nameQry) {
        List<Vehicle> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Vehicle> allIns = vehicleApplicationController.getVehicles();

        for (Vehicle i : allIns) {
            boolean canInclude = true;
            if (type != null) {
                if (i.getVehicleType() == null) {
                    canInclude = false;
                } else {
                    if (!i.getVehicleType().equals(type)) {
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

    public List<Vehicle> fillVehicles(List<VehicleType> types, String nameQry) {
        List<Vehicle> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Vehicle> allVehicles = vehicleApplicationController.getVehicles();

        for (Vehicle i : allVehicles) {
            boolean canInclude = true;

            boolean typeFound = false;
            for (VehicleType type : types) {
                if (type != null) {
                    if (i.getVehicleType() != null && i.getVehicleType().equals(type)) {
                        typeFound = true;
                    }
                }
            }
            if (!typeFound) {
                canInclude = false;
            }
            if (i.getVehicleNumber() == null || i.getVehicleNumber().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getVehicleNumber().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
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

    public List<Vehicle> completeVehiclesByWords(String nameQry) {
        List<Vehicle> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Vehicle> allIns = vehicleApplicationController.getVehicles();
        nameQry = nameQry.trim();
        String words[] = nameQry.split("\\s+");

        for (Vehicle i : allIns) {
            boolean allWordsMatch = true;

            for (String word : words) {
                boolean thisWordMatch;
                word = word.trim().toLowerCase();
                if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getVehicleNumber() != null && i.getVehicleNumber().toLowerCase().contains(word)) {
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

    public Vehicle prepareCreate() {
        selected = new Vehicle();
        initializeEmbeddableKey();
        return selected;
    }

    public void prepareToAddNewVehicle() {
        selected = new Vehicle();
    }

    public void prepareToListVehicle() {
        if (webUserController.getLoggedUser() == null) {
            items = null;
        }
        if (webUserController.getLoggedUser().getWebUserRoleLevel() == WebUserRoleLevel.HEALTH_MINISTRY) {
            items = vehicleApplicationController.getVehicles();
        } else {
            items = webUserController.findAutherizedVehicles();
        }
    }

    public void saveOrUpdateVehicle() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return;
        }
        if (selected.getVehicleNumber() == null || selected.getVehicleNumber().trim().equals("")) {
            JsfUtil.addErrorMessage("Number is required");
            return;
        }

        if (selected.getName() == null || selected.getName().trim().equals("")) {
            selected.setName(selected.getVehicleNumber());
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);

            vehicleApplicationController.getVehicles().add(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
    }

    public void save(Vehicle ins) {
        if (ins == null) {
            return;
        }
        if (ins.getId() == null) {
            ins.setCreatedAt(new Date());
            ins.setCreater(webUserController.getLoggedUser());
            getFacade().create(ins);
            vehicleApplicationController.getVehicles().add(ins);
            items = null;
        } else {
            ins.setEditedAt(new Date());
            ins.setEditer(webUserController.getLoggedUser());
            getFacade().edit(ins);
            items = null;
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("VehicleCreated"));
        if (!JsfUtil.isValidationFailed()) {
            vehicleApplicationController.getVehicles().add(selected);
            fillItems();
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("VehicleUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("VehicleDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Vehicle> getItems() {
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

    public Vehicle getVehicle(java.lang.Long id) {
        Vehicle ni = null;
        for (Vehicle i : vehicleApplicationController.getVehicles()) {
            if (i.getId() != null && i.getId().equals(id)) {
                ni = i;
            }
        }
        return ni;
    }

    public void refreshMyVehicles() {
        userTransactionController.recordTransaction("refresh My Vehicles");
        myClinics = null;
    }

    public lk.gov.health.phsp.facade.VehicleFacade getEjbFacade() {
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

    public void setMyClinics(List<Vehicle> myClinics) {
        this.myClinics = myClinics;
    }

    public Vehicle getDeleting() {
        return deleting;
    }

    public void setDeleting(Vehicle deleting) {
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

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Vehicle getParent() {
        return parent;
    }

    public void setParent(Vehicle parent) {
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

    @FacesConverter(forClass = Vehicle.class)
    public static class VehicleControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            VehicleController controller = (VehicleController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "vehicleController");
            return controller.getVehicle(getKey(value));
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
            if (object instanceof Vehicle) {
                Vehicle o = (Vehicle) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Vehicle.class.getName()});
                return null;
            }
        }

    }

}
