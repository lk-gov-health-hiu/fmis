package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.FuelTransactionHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.FuelTrnasactionFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.VehicleFacade;
import lk.gov.health.phsp.facade.WebUserFacade;

@Named
@SessionScoped
public class FuelRequestAndIssueController implements Serializable {

    @EJB
    FuelTransactionHistoryFacade fuelTransactionHistoryFacade;

    @EJB
    FuelTrnasactionFacade fuelTransactionFacade;

    @EJB
    InstitutionFacade institutionFacade;
    
    @EJB
    VehicleFacade vehicleFacade;

    @EJB
    WebUserFacade webUserFacade;

    private List<FuelTransaction> txs = null;
    private List<FuelTransaction> selectedTxs = null;
    private FuelTransaction selected;
    
    private FuelTransactionHistory selectedHistory;
    private List<FuelTransactionHistory> selectedTxHistories;
    private List<FuelTransactionHistory> txHistories;
   
    
    @Inject
    private WebUserController webUserController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    MenuController menuController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    VehicleApplicationController vehicleApplicationController;
    @Inject
    WebUserApplicationController webUserApplicationController;

    private Institution institution;
    Vehicle vehicle;
    private WebUser webUser;
    private Date fromDate;
    private Date toDate;
    


    public FuelRequestAndIssueController() {
    }

    public void saveSelected() {
        System.out.println("saveCurrentDocument = " + selected);
        if (selected == null) {
            return;
        }
        save(selected);
    }

    
   
    public void searchFuelTransaction() {
       
    }

    public void searchFuelRequestByVehicle() {
       
    }

    public String navigateToListFuelTransactions() {
        return "/issues/list";
    }
    
    public String navigateToAddRequest(){
        return "/requests/request";
    }
    
    public String navigateToIssueRequest(){
        return "/issues/issue";
    }

    public String generateRequest(){
        return "/requests/requested";
    }
    
    public String completeIssue(){
        return "/issues/issued";
    }
    
    
    
    public String navigateToListInstitutionRequests() {
        return "/requests/list";
    }

    
    
    public FuelTransaction getSelected() {
        return selected;
    }

    public void setSelected(FuelTransaction selected) {
        this.selected = selected;
    }

    public FuelTransaction find(Object id){
        return fuelTransactionFacade.find(id);
    }
    
    private FuelTransactionHistoryFacade getFacade() {
        return fuelTransactionHistoryFacade;
    }

    public void save(FuelTransaction saving) {
        if(saving==null){
            return;
        }
        if(saving.getId()==null){
            fuelTransactionFacade.create(saving);
        }else{
            fuelTransactionFacade.edit(saving);
        }
    }

   
    @FacesConverter(forClass = FuelTransaction.class)
    public static class FuelTransactionConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FuelRequestAndIssueController controller = (FuelRequestAndIssueController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "fuelRequestAndIssueController");
            return controller.find(getKey(value));
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
