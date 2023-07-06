package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;

import java.io.Serializable;
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
import lk.gov.health.phsp.entity.FuelTransactionHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.facade.FuelTrnasactionFacade;

@Named
@SessionScoped
public class FileController implements Serializable {

    @EJB
    private FuelTransactionHistoryFacade documentFacade;

    @EJB
    FuelTrnasactionFacade documentHxFacade;

    private List<FuelTransaction> items = null;
    private List<FuelTransaction> selectedItems = null;
    private FuelTransaction selected;
    private FuelTransactionHistory selectedHistory;
    private List<FuelTransactionHistory> selectedDocumentHistories;
    @Inject
    private WebUserController webUserController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    ItemApplicationController itemApplicationController;

    private Institution institution;
    private WebUser webUser;
    private String searchTerm;

    public FileController() {
    }

    public void searchFile() {
        if (searchTerm == null || searchTerm.trim().equals("")) {
            JsfUtil.addErrorMessage("No Search Term");
            return;
        }
        String j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.documentNumber=:dn"
                + " order by d.documentDate desc";
        Map m = new HashMap();
        m.put("dt", FuelTransactionType.FuelRequest);
        m.put("dn", searchTerm.trim());
        items = documentFacade.findByJpql(j, m);

        if (items != null && !items.isEmpty()) {
            return;
        }

        j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and (d.documentNumber like :dn "
                + " or d.documentName like :dn "
                + " or d.documentCode like :dn )"
                + " order by d.documentDate desc";

        m = new HashMap();
        m.put("dt", FuelTransactionType.FuelRequest);
        m.put("dn", "%" + searchTerm.trim() + "%");

        items = documentFacade.findByJpql(j, m);

        /**
         * private String documentName; private String documentNumber; private
         * String documentCode;
         */
    }

    public void retireSelectedEncounter() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Retired Successfully");
        userTransactionController.recordTransaction("Retire Selected Encounter");
        selected = null;
    }

    public FuelTransaction getSelected() {
        return selected;
    }

    public void setSelected(FuelTransaction selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private FuelTransactionHistoryFacade getFacade() {
        return documentFacade;
    }

    public FuelTransaction prepareCreate() {
        selected = new FuelTransaction();
        initializeEmbeddableKey();
        return selected;
    }

    public void save() {
        save(selected);
    }

    public String transferOutFile() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select an institution to transfer out");
            return "";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return "";
        }

        FuelTransactionHistory docHx = new FuelTransactionHistory();
        docHx.setHistoryType(HistoryType.File_Institution_Transfer);
        docHx.setDocument(selected);
        docHx.setFromInstitution(selected.getCurrentInstitution());
        docHx.setToInstitution(institution);
        saveDocumentHx(docHx);

        JsfUtil.addSuccessMessage("Transferred out successfully");
        return toFileView();
    }

    public String transferOutOwnershipFile() {
        if (webUser == null) {
            JsfUtil.addErrorMessage("Select a user to transfer ownership");
            return "";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return "";
        }

        FuelTransactionHistory docHx = new FuelTransactionHistory();
        docHx.setHistoryType(HistoryType.File_Owner_Transfer);
        docHx.setDocument(selected);
        docHx.setFromUser(selected.getCurrentOwner());
        docHx.setToUser(webUser);

        saveDocumentHx(docHx);

        JsfUtil.addSuccessMessage("Ownership change initiated successfully");
        return toFileView();
    }

    public String saveAndViewFile() {
        boolean newHx = false;
        if (selected.getId() == null) {
            newHx = true;
        }
        save(selected);
        if (newHx) {
            if (selectedHistory == null) {
                selectedHistory = new FuelTransactionHistory();
                selectedHistory.setHistoryType(HistoryType.File_Created);
            }
            selectedHistory.setToInstitution(selected.getCurrentInstitution());
            selectedHistory.setToUser(selected.getCurrentOwner());
            selectedHistory.setCompleted(true);
            selectedHistory.setCompletedAt(new Date());
            selectedHistory.setCompletedBy(webUserController.getLoggedUser());
            selectedHistory.setDocument(selected);
            saveDocumentHx(selectedHistory);
        }
        return toFileView();
    }

    public void saveDocumentHx(FuelTransactionHistory hx) {
        if (hx == null) {
            return;
        }
        if (hx.getId() == null) {
            hx.setCreatedAt(new Date());
            hx.setCreatedBy(webUserController.getLoggedUser());
            documentHxFacade.create(hx);
        } else {
            documentHxFacade.edit(hx);
        }
    }
    
    public String toFileEdit() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        return "/document/file";
    }

    public String toFileView() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.document=:doc "
                + " order by h.id";
        Map m = new HashMap();
        m.put("doc", selected);
        selectedDocumentHistories = documentHxFacade.findByJpql(j, m);
        return "/document/file_view";
    }

    public void save(FuelTransaction e) {
        if (e == null) {
            return;
        }
        if (e.getId() == null) {
            e.setCreatedAt(new Date());
            e.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(e);
        } else {
            e.setLastEditBy(webUserController.getLoggedUser());
            e.setLastEditeAt(new Date());
            getFacade().edit(e);
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<FuelTransaction> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
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

    public FuelTransaction getEncounter(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<FuelTransaction> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<FuelTransaction> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.FuelTransactionHistoryFacade getDocumentFacade() {
        return documentFacade;
    }

    public List<FuelTransaction> getItems() {
        return items;
    }

    public void setItems(List<FuelTransaction> items) {
        this.items = items;
    }

    public List<FuelTransaction> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<FuelTransaction> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public FuelTransactionHistory getSelectedHistory() {
        return selectedHistory;
    }

    public void setSelectedHistory(FuelTransactionHistory selectedHistory) {
        this.selectedHistory = selectedHistory;
    }

    public WebUser getWebUser() {
        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
    }

    public List<FuelTransactionHistory> getSelectedDocumentHistories() {
        return selectedDocumentHistories;
    }

    public void setSelectedDocumentHistories(List<FuelTransactionHistory> selectedDocumentHistories) {
        this.selectedDocumentHistories = selectedDocumentHistories;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @FacesConverter(forClass = FuelTransaction.class)
    public static class EncounterControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FileController controller = (FileController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "encounterController");
            return controller.getEncounter(getKey(value));
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
            if (object instanceof FuelTransaction) {
                FuelTransaction o = (FuelTransaction) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), FuelTransaction.class.getName()});
                return null;
            }
        }

    }

}
