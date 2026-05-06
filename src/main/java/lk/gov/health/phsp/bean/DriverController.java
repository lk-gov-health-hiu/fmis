package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Driver;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DriverFacade;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.InstitutionCategory;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.pojcs.DriverLightDTO;

@Named
@SessionScoped
public class DriverController implements Serializable {

    @EJB
    private DriverFacade ejbFacade;

    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    private ApplicationController applicationController;
    @Inject
    DriverApplicationController driverApplicationController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    MenuController menuController;

    private List<Driver> items = null;
    private Driver selected;
    private Driver deleting;

    private List<DriverLightDTO> lastDriverLightDtoResults;

    private Driver parent;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    public Driver getDriverById(Long id) {
        return getFacade().find(id);
    }

    public String toAddDriver() {
        selected = new Driver();
        userTransactionController.recordTransaction("To Add Driver");
        fillItems();
        return "/driver/driver";
    }

    public String toImportDriver() {
        selected = new Driver();
        userTransactionController.recordTransaction("To Add Driver");
        return "/driver/import";
    }

    public String toEditDriver() {
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/driver";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/driver";
                } else {
                    return "/institution/admin/driver";
                }
        }
    }

    public String deleteDriver() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }

        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        driverApplicationController.getDrivers().remove(deleting);
        fillItems();
        return "/driver/list";
    }

    public String toListDrivers() {
        userTransactionController.recordTransaction("To List Drivers");
        return "/driver/list";
    }

    public String toSearchDrivers() {
        return "/driver/search";
    }

    public List<Driver> searchDrivers(String searchingText) {
        List<Driver> matchingDrivers = new ArrayList<>();
        if (searchingText == null) {
            return matchingDrivers;
        }
        searchingText = searchingText.trim();
        if (searchingText.equals("")) {
            return matchingDrivers;
        }
        searchingText = searchingText.toLowerCase();
        List<Driver> allDrivers = driverApplicationController.getDrivers();

        for (Driver d : allDrivers) {
            if (d.getName().toLowerCase().contains(searchingText)) {
                matchingDrivers.add(d);
            }
        }

        return matchingDrivers;
    }

    public DriverController() {
    }

    public Driver getSelected() {
        return selected;
    }

    public void setSelected(Driver selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private DriverFacade getFacade() {
        return ejbFacade;
    }

    public List<Driver> completeDrivers(String nameQry) {
        return searchDrivers(nameQry);
    }

    public Driver findDriverByNic(String nic) {
        if (nic == null || nic.trim().equals("")) {
            return null;
        }
        Driver ni = null;
        for (Driver i : driverApplicationController.getDrivers()) {
            if (i.getName() != null && i.getName().equalsIgnoreCase(nic)) {
                if (ni != null) {
                }
                ni = i;
            }
        }
        return ni;
    }

    public void fillItems() {
        if (driverApplicationController.getDrivers() != null) {
            items = driverApplicationController.getDrivers();
            return;
        }
    }

    public void resetAllDrivers() {
        items = null;
        driverApplicationController.resetAllDrivers();
        items = driverApplicationController.getDrivers();
    }

    public List<Driver> fillDrivers(String nameQry) {
        return searchDrivers(nameQry);
    }

    public List<Driver> completeDriversByWords(String nameQry) {
        List<Driver> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Driver> allIns = driverApplicationController.getDrivers();
        nameQry = nameQry.trim();
        String words[] = nameQry.split("\\s+");

        for (Driver i : allIns) {
            boolean allWordsMatch = true;

            for (String word : words) {
                boolean thisWordMatch;
                word = word.trim().toLowerCase();
                if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
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

    /**
     * DTO-based autocomplete that searches every non-retired driver by name,
     * NIC, or phone. Used where the picker must span all institutions
     * (e.g. special fuel requests). Each typed word must match at least one
     * of the three columns; capped at 20 rows.
     */
    public List<DriverLightDTO> completeAllDriversByWordsDto(String nameQry) {
        if (nameQry == null || nameQry.trim().isEmpty()) {
            lastDriverLightDtoResults = new ArrayList<>();
            return lastDriverLightDtoResults;
        }

        String[] words = nameQry.trim().split("\\s+");
        StringBuilder jpql = new StringBuilder(
                "SELECT NEW lk.gov.health.phsp.pojcs.DriverLightDTO("
                + "d.id, d.name, d.nic, d.phone, d.allocationType, ins.id, ins.name) "
                + "FROM Driver d LEFT JOIN d.institution ins "
                + "WHERE d.retired = false ");

        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            String paramName = "q" + i;
            jpql.append("AND (LOWER(d.name) LIKE :").append(paramName)
                    .append(" OR LOWER(d.nic) LIKE :").append(paramName)
                    .append(" OR LOWER(d.phone) LIKE :").append(paramName)
                    .append(") ");
            params.put(paramName, "%" + words[i].toLowerCase() + "%");
        }
        jpql.append("ORDER BY d.name");

        @SuppressWarnings("unchecked")
        List<DriverLightDTO> results = (List<DriverLightDTO>) getFacade()
                .findLightsByJpql(jpql.toString(), params, TemporalType.TIMESTAMP, 20);

        lastDriverLightDtoResults = results != null ? results : new ArrayList<DriverLightDTO>();
        return lastDriverLightDtoResults;
    }

    public Driver prepareCreate() {
        selected = new Driver();
        initializeEmbeddableKey();
        return selected;
    }

    public void prepareToAddNewDriver() {
        selected = new Driver();
        selected.setInstitution(webUserController.getLoggedInstitution());
    }

    public void prepareToListDriver() {
        if (webUserController.getLoggedUser() == null) {
            items = null;
        }
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        if (roleLevel == WebUserRoleLevel.HEALTH_MINISTRY) {
            items = driverApplicationController.getDrivers();
        } else if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
            items = driverApplicationController.getDrivers();
        } else {
            items = driverApplicationController.findDriversByInstitutions(webUserController.getLoggableInstitutions());
        }
        webUserController.setManagableDrivers(items);
    }

    public List<Driver> institutionDrivers(Institution ins) {
        List<Driver> insDrivers = new ArrayList<>();
        if (ins == null) {
            return insDrivers;
        }
        List<Institution> inss = new ArrayList<>();
        inss.add(ins);
        insDrivers = driverApplicationController.findDriversByInstitutions(inss);
        return insDrivers;
    }

    public String saveOrUpdateDriver() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return "";
        }
        if (selected.getName() == null || selected.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Number is required");
            return "";
        }
        if (selected.getNic() == null || selected.getNic().trim().equals("")) {
            JsfUtil.addErrorMessage("Number is required");
            return "";
        }

        // Check for duplicate NIC
        Driver existingDriver = getFacade().findDriverByNic(selected.getNic(), selected.getId());
        if (existingDriver != null) {
            JsfUtil.addErrorMessage("A driver with this NIC (" + selected.getNic() + ") already exists. Name: " + existingDriver.getName());
            return "";
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);
            driverApplicationController.getDrivers().add(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
        return menuController.toListDriverss();
    }

    public void save(Driver ins) {
        if (ins == null) {
            return;
        }

        // Check for duplicate NIC
        if (ins.getNic() != null && !ins.getNic().trim().isEmpty()) {
            Driver existingDriver = getFacade().findDriverByNic(ins.getNic(), ins.getId());
            if (existingDriver != null) {
                JsfUtil.addErrorMessage("A driver with this NIC (" + ins.getNic() + ") already exists. Name: " + existingDriver.getName());
                return;
            }
        }

        if (ins.getId() == null) {
            if (ins.getCreater() == null) {
                ins.setCreater(webUserController.getLoggedUser());
            }
            ins.setCreatedAt(new Date());
            getFacade().create(ins);
            driverApplicationController.getDrivers().add(ins);
            items = null;
        } else {
            ins.setEditedAt(new Date());
//            ins.setEditer(webUserController.getLoggedUser());
            getFacade().edit(ins);
            items = null;
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("DriverCreated"));
        if (!JsfUtil.isValidationFailed()) {
            driverApplicationController.getDrivers().add(selected);
            fillItems();
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("DriverUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("DriverDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Driver> getItems() {
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

    public Driver getDriver(java.lang.Long id) {
        Driver ni = null;
        for (Driver i : driverApplicationController.getDrivers()) {
            if (i.getId() != null && i.getId().equals(id)) {
                ni = i;
            }
        }
        return ni;
    }

    public lk.gov.health.phsp.facade.DriverFacade getEjbFacade() {
        return ejbFacade;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public Driver getDeleting() {
        return deleting;
    }

    public void setDeleting(Driver deleting) {
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

    public Driver getParent() {
        return parent;
    }

    public void setParent(Driver parent) {
        this.parent = parent;
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

    @FacesConverter("driverLightDtoConverter")
    public static class DriverLightDtoConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            Long id;
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException e) {
                return null;
            }
            DriverController controller = (DriverController) facesContext.getApplication().getELResolver()
                    .getValue(facesContext.getELContext(), null, "driverController");
            if (controller != null && controller.lastDriverLightDtoResults != null) {
                for (DriverLightDTO dto : controller.lastDriverLightDtoResults) {
                    if (id.equals(dto.getId())) {
                        return dto;
                    }
                }
            }
            return new DriverLightDTO(id);
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return "";
            }
            if (object instanceof DriverLightDTO) {
                Long id = ((DriverLightDTO) object).getId();
                return id != null ? id.toString() : "";
            }
            return "";
        }
    }

    @FacesConverter(forClass = Driver.class)
    public static class DriverControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DriverController controller = (DriverController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "driverController");
            return controller.getDriver(getKey(value));
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
            if (object instanceof Driver) {
                Driver o = (Driver) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Driver.class.getName()});
                return null;
            }
        }

    }

}
