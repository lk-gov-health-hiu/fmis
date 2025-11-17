package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelPrice;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.FuelPriceFacade;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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

@Named
@SessionScoped
public class FuelPriceController implements Serializable {

    @EJB
    private FuelPriceFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    MenuController menuController;

    @Inject
    private ApplicationController applicationController;
    @Inject
    private UserTransactionController userTransactionController;

    private List<FuelPrice> items = null;
    private FuelPrice selected;
    private FuelPrice deleting;

    public FuelPriceController() {
    }

    public String toAddFuelPrice() {
        selected = new FuelPrice();
        userTransactionController.recordTransaction("To Add Fuel Price");
        fillItems();
        return "/fuelPrice/fuelPrice";
    }

    public String toListFuelPrices() {
        userTransactionController.recordTransaction("To List Fuel Prices");
        fillItems();
        return "/fuelPrice/list";
    }

    public String toEditFuelPrice() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        return "/fuelPrice/fuelPrice";
    }

    public String deleteFuelPrice() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }

        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        fillItems();
        return "/fuelPrice/list";
    }

    public FuelPrice prepareCreate() {
        selected = new FuelPrice();
        return selected;
    }

    public void prepareToAddNewFuelPrice() {
        selected = new FuelPrice();
    }

    public String saveOrUpdateFuelPrice() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return null;
        }

        if (selected.getFuelType() == null) {
            JsfUtil.addErrorMessage("Fuel Type is required");
            return null;
        }

        if (selected.getPrice() == null) {
            JsfUtil.addErrorMessage("Price is required");
            return null;
        }

        if (selected.getFromDate() == null) {
            JsfUtil.addErrorMessage("From Date is required");
            return null;
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        return toListFuelPrices();
    }

    public void create() {
        persist(PersistAction.CREATE, "Fuel Price Created");
        if (!JsfUtil.isValidationFailed()) {
            fillItems();
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, "Fuel Price Updated");
        if (!JsfUtil.isValidationFailed()) {
            fillItems();
        }
    }

    public void destroy() {
        persist(PersistAction.DELETE, "Fuel Price Deleted");
        if (!JsfUtil.isValidationFailed()) {
            selected = null;
            items = null;
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
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
                    JsfUtil.addErrorMessage(ex, "Persistence Error Occurred");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, "Persistence Error Occurred");
            }
        }
    }

    public void fillItems() {
        items = getFacade().findByJpql("select f from FuelPrice f where f.retired=false order by f.fromDate desc");
    }

    public List<FuelPrice> getItems() {
        if (items == null) {
            fillItems();
        }
        return items;
    }

    public FuelPrice getSelected() {
        return selected;
    }

    public void setSelected(FuelPrice selected) {
        this.selected = selected;
    }

    public FuelPrice getDeleting() {
        return deleting;
    }

    public void setDeleting(FuelPrice deleting) {
        this.deleting = deleting;
    }

    private FuelPriceFacade getFacade() {
        return ejbFacade;
    }

    public FuelPrice getFuelPrice(java.lang.Long id) {
        return getFacade().find(id);
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

    public String getMonthName(Integer month) {
        if (month == null) {
            return "";
        }
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                               "July", "August", "September", "October", "November", "December"};
        if (month >= 1 && month <= 12) {
            return monthNames[month - 1];
        }
        return month.toString();
    }

    @FacesConverter(forClass = FuelPrice.class)
    public static class FuelPriceControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FuelPriceController controller = (FuelPriceController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "fuelPriceController");
            return controller.getFuelPrice(getKey(value));
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
            if (object instanceof FuelPrice) {
                FuelPrice o = (FuelPrice) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), FuelPrice.class.getName()});
                return null;
            }
        }

    }

}
