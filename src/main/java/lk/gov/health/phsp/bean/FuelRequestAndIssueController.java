package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.FuelTransactionHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.VehicleFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import org.primefaces.event.CaptureEvent;

@Named
@SessionScoped
public class FuelRequestAndIssueController implements Serializable {

    @EJB
    private FuelTransactionHistoryFacade fuelTransactionHistoryFacade;
    @EJB
    private FuelTransactionFacade fuelTransactionFacade;
    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    VehicleFacade vehicleFacade;
    @EJB
    WebUserFacade webUserFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    VehicleController vehicleController;
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
    @Inject
    QRCodeController qrCodeController;

    private List<FuelTransaction> transactions = null;
    private List<FuelTransaction> selectedTransactions = null;
    private FuelTransaction selected;

    private FuelTransactionHistory selectedTransactionHistory;
    private List<FuelTransactionHistory> selectedTransactionHistories;
    private List<FuelTransactionHistory> transactionHistories;

    private Institution institution;
    private Vehicle vehicle;
    private WebUser webUser;
    private Date fromDate;
    private Date toDate;

    private String searchingFuelRequestVehicleNumber;

    public FuelRequestAndIssueController() {
    }

    public String searchFuelRequestByVehicleNumber() {
        if (searchingFuelRequestVehicleNumber == null || searchingFuelRequestVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please provide a vehicle number");
            return "";
        }

        Institution toInstitution = webUserController.getLoggedInstitution();
        List<Vehicle> vs = vehicleController.searchVehicles(searchingFuelRequestVehicleNumber);
        if (vs == null || vs.isEmpty()) {
            JsfUtil.addErrorMessage("No Matching Vehicle");
            return "";
        }

        List<FuelTransaction> searchResults = findFuelTransactions(null,
                null,
                toInstitution,
                vs,
                null,
                null,
                null, null, null);


        if (searchResults == null || searchResults.isEmpty()) {
            JsfUtil.addErrorMessage("No search results. Please check and retry.");
            return "";
        }
        if (searchResults.size() == 1) {
            selected = searchResults.get(0);
            return navigateToIssueVehicleFuelRequest();
        } else {
            selected = null;
            transactions = searchResults;
            return navigateToSelectToIssueVehicleFuelRequest();
        }
    }

    public String searchFuelRequestToIssueByVehicleNumber() {
        if (searchingFuelRequestVehicleNumber == null || searchingFuelRequestVehicleNumber.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please provide a vehicle number");
            return "";
        }

        Institution toInstitution = webUserController.getLoggedInstitution();
        List<Vehicle> vs = vehicleController.searchVehicles(searchingFuelRequestVehicleNumber);
        if (vs == null || vs.isEmpty()) {
            JsfUtil.addErrorMessage("No Matching Vehicle");
            return "";
        }

        List<FuelTransaction> searchResults = findFuelTransactions(null,
                null,
                toInstitution,
                vs,
                null,
                null,
                false, false, false);


        if (searchResults == null || searchResults.isEmpty()) {
            JsfUtil.addErrorMessage("No search results. Please check and retry.");
            return "";
        }
        if (searchResults.size() == 1) {
            selected = searchResults.get(0);
            return navigateToIssueVehicleFuelRequest();
        } else {
            selected = null;
            transactions = searchResults;
            return navigateToSelectToIssueVehicleFuelRequest();
        }
    }

    public String navigateToIssueVehicleFuelRequest() {
        return "/issues/issue";
    }

    public String navigateToMarkVehicleFuelRequest() {
        if(selected==null){
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        selected.setIssuedQuantity(selected.getRequestQuantity());
        selected.setIssuedInstitution(selected.getToInstitution());
        return "/requests/mark?faces-redirect=true";
    }

    public String navigateToViewIssuedVehicleFuelRequest() {
        return "/issues/issued";
    }

    public void rejectFuelIssueAtDepot() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRejected(true);
        selected.setRejectedAt(new Date());
        selected.setRejectedBy(webUserController.getLoggedUser());
        selected.setRejectedInstitution(webUserController.getLoggedInstitution());
        getFacade().edit(selected);
        transactions.remove(selected);
        JsfUtil.addSuccessMessage("Rejected");
    }

    public void issueFuelIssueAtDepotDirectly() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }

        if (selected.getTransactionType() == null) {
            selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        }

        selected.setIssuedQuantity(selected.getRequestQuantity());

        selected.setIssued(true);
        selected.setIssuedAt(new Date());
        selected.setIssuedInstitution(webUserController.getLoggedInstitution());
        selected.setIssuedUser(webUserController.getLoggedUser());
        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
        selected.setStockAfterTheTransaction(institutionApplicationController.deductFromStock(webUserController.getLoggedInstitution(), selected.getIssuedQuantity()));
        save(selected);
        transactions.remove(selected);
        JsfUtil.addSuccessMessage("Rejected");
    }

    public String navigateToReceiveFuelAtDepot() {
        selected = new FuelTransaction();
        selected.setTransactionType(FuelTransactionType.CtbFuelReceive);
        selected.setToInstitution(webUserController.getLoggedInstitution());
        selected.setTxDate(new Date());
        selected.setTxTime(new Date());
        selected.setInstitution(webUserController.getLoggedInstitution());
        return "/depot/receive";
    }

    public String navigateToSelectToIssueVehicleFuelRequest() {
        return "/issues/select_issue";
    }

    public void saveSelected() {
        if (selected == null) {
            return;
        }
        save(selected);
    }

    public String submitVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        save(selected);
        JsfUtil.addSuccessMessage("Request Submitted");
        return navigateToViewInstitutionFuelRequestToSltbDepot();
    }

    public String submitSltbFuelRequestFromCpc() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.DepotFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        save(selected);
        JsfUtil.addSuccessMessage("Request Submitted");
        return navigateToViewDepotFuelRequestToCpc();
    }

    public String submitVehicleFuelRequestIssue() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getIssuedQuantity() == null) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() < 1.0) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() > selected.getRequestQuantity()) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        selected.setIssued(true);
        selected.setIssuedAt(new Date());
        selected.setIssuedInstitution(webUserController.getLoggedInstitution());
        selected.setIssuedUser(webUserController.getLoggedUser());
        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
        selected.setStockAfterTheTransaction(institutionApplicationController.deductFromStock(webUserController.getLoggedInstitution(), selected.getIssuedQuantity()));
        save(selected);
        JsfUtil.addSuccessMessage("Successfully Issued");
        return navigateToSearchRequestsForVehicleFuelIssue();
    }
    
    public String submitMarkVehicleFuelRequestIssue() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.VehicleFuelRequest && selected.getTransactionType() != FuelTransactionType.SpecialVehicleFuelRequest) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getIssuedQuantity() == null) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() < 1.0) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        if (selected.getIssuedQuantity() > selected.getRequestQuantity()) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        selected.setIssued(true);
        selected.setIssuedAt(new Date());
        selected.setIssuedUser(webUserController.getLoggedUser());
//        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
//        selected.setStockAfterTheTransaction(institutionApplicationController.deductFromStock(webUserController.getLoggedInstitution(), selected.getIssuedQuantity()));
        save(selected);
        JsfUtil.addSuccessMessage("Successfully Issued");
        listInstitutionRequestsToMark();
        return navigateToListInstitutionRequestsToMark();
    }

    public String submitVehicleFuelReceive() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selected.getTransactionType() == null) {
            JsfUtil.addErrorMessage("Transaction Type is not set.");
            return "";
        }
        if (selected.getTransactionType() != FuelTransactionType.CtbFuelReceive) {
            JsfUtil.addErrorMessage("Wrong Transaction Type");
            return "";
        }
        if (selected.getReceivedQty() == null) {
            JsfUtil.addErrorMessage("Wrong Qty");
            return "";
        }
        selected.setStockBeforeTheTransaction(institutionApplicationController.getInstitutionStock(webUserController.getLoggedInstitution()));
        selected.setStockAfterTheTransaction(institutionApplicationController.addToStock(webUserController.getLoggedInstitution(), selected.getReceivedQty()));
        save(selected);
        JsfUtil.addSuccessMessage("Successfully Received");
        return navigateToListDepotReceiveList();
    }

    public String navigateToListDepotReceiveList() {
        institution = webUserController.getLoggedInstitution();
        fillDepotReceiveList();
        return "/depot/depot_receive_list";
    }

    public String navigateToSltbReportsFuelRequests() {
        return "/sltb/reports/requests";
    }

    public String navigateToSltbReportsFuelIssues() {
        return "/sltb/reports/issues";
    }

    public String navigateToSltbReportsFuelRejections() {
        return "/sltb/reports/rejections";
    }

    public void fillDepotReceiveList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, null, null, null, null);
    }

    public void fillDepotToIssueList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, Boolean.FALSE,
                Boolean.FALSE, Boolean.FALSE, null);
    }

    public void fillIssuedRequestsFromDepotList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, Boolean.TRUE,
                null, null, null);
    }

    public void fillRejectedIssueRequestsFromDepotList() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution ?");
            return;
        }
        transactions = findFuelTransactions(null, null, institution, null, fromDate, toDate, null,
                null, Boolean.TRUE, null);
    }

    public String navigateToListFuelTransactions() {
        return "/issues/list";
    }

    public String navigateToViewVehicleFuelRequest() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        return "/issues/requested";
    }

    public String navigateToAddVehicleFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.VehicleFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setToInstitution(webUserController.getLoggedInstitution().getSupplyInstitution());
        if (webUserController.getManagableVehicles().size() == 1) {
            selected.setVehicle(webUserController.getManagableVehicles().get(0));
        }
        if (webUserController.getManagableDrivers().size() == 1) {
            selected.setDriver(webUserController.getManagableDrivers().get(0));
        }
        return "/requests/request";
    }

    public String navigateToAddCpcFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.DepotFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(institutionApplicationController.findCpc());
        return "/moh/request";
    }

    public String navigateToAddSpecialVehicleFuelRequest() {
        selected = new FuelTransaction();
        selected.setRequestAt(new Date());
        selected.setTransactionType(FuelTransactionType.SpecialVehicleFuelRequest);
        selected.setRequestedBy(webUserController.getLoggedUser());
        selected.setRequestedInstitution(webUserController.getLoggedInstitution());
        selected.setFromInstitution(webUserController.getLoggedInstitution());
        selected.setInstitution(webUserController.getLoggedInstitution());
        if (webUserController.getManagableVehicles().size() == 1) {
            selected.setVehicle(webUserController.getManagableVehicles().get(0));
        }
        if (webUserController.getManagableDrivers().size() == 1) {
            selected.setDriver(webUserController.getManagableDrivers().get(0));
        }
        return "/requests/special_request";
    }

    public String navigateToSearchRequestsForVehicleFuelIssue() {
        return "/issues/search";
    }

    public String navigateToSearchRequestsForVehicleFuelIssueQr() {
        return "/issues/search_qr";
    }

    public String generateRequest() {
        return "/requests/requested";
    }

    public String completeIssue() {
        return "/issues/issued";
    }

    public String navigateToListInstitutionRequests() {
        return "/requests/list";
    }

    public String navigateToListInstitutionRequestsToMark() {
        return "/requests/list_to_mark";
    }

    public String navigateToListSltbRequestsFromCpc() {
        return "/moh/list";
    }

    public String onCaptureOfVehicleQr(CaptureEvent captureEvent) {
        byte[] imageData = captureEvent.getData();
        searchingFuelRequestVehicleNumber = qrCodeController.scanQRCode(imageData);
        return searchFuelRequestToIssueByVehicleNumber();
    }

    public void listInstitutionRequests() {
        transactions = findFuelTransactions(webUserController.getLoggedInstitution(), null, null, null, fromDate, toDate, null, null, null);
    }

    public void listInstitutionRequestsToMark() {
        transactions = findFuelTransactions(webUserController.getLoggedInstitution(), null, null, null, fromDate, toDate, false, false, false);
    }

    public void listCtbFuelRequestsFromCpc() {
        transactions = findFuelTransactions(webUserController.getLoggedInstitution(), null, null, null, fromDate, toDate, null, null, null, null, FuelTransactionType.CtbFuelRequest);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected) {
        return findFuelTransactions(institution, fromInstitution, toInstitution, vehicles, fromDateTime, toDateTime, issued, cancelled, rejected, null);
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected,
            List<FuelTransactionType> txTypes) {
        String j = "SELECT ft "
                + " FROM FuelTransaction ft "
                + " WHERE ft.retired = false";
        Map<String, Object> params = new HashMap<>();

        if (institution != null) {
            j += " AND ft.institution = :institution";
            params.put("institution", institution);
        }
        if (fromInstitution != null) {
            j += " AND ft.fromInstitution = :fromInstitution";
            params.put("fromInstitution", fromInstitution);
        }
        if (toInstitution != null) {
            j += " AND ft.toInstitution = :toInstitution";
            params.put("toInstitution", toInstitution);
        }
        if (vehicles != null && !vehicles.isEmpty()) {
            j += " AND ft.vehicle IN :vehicles";
            params.put("vehicles", vehicles);
        }
        if (fromDateTime != null) {
            j += " AND ft.requestAt >= :fromDateTime";
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            j += " AND ft.requestAt <= :toDateTime";
            params.put("toDateTime", toDateTime);
        }
        if (issued != null) {
            j += " AND ft.issued = :issued ";
            params.put("issued", issued);
        }
        if (cancelled != null) {
            j += " AND ft.cancelled = :cancelled ";
            params.put("cancelled", cancelled);
        }
        if (rejected != null) {
            j += " AND ft.rejected = :rejected ";
            params.put("rejected", rejected);
        }
        if (txTypes != null) {
            j += " AND ft.transactionType in :ftxs ";
            params.put("ftxs", txTypes);
        }
        List<FuelTransaction> fuelTransactions = getFacade().findByJpql(j, params);
        if (fuelTransactions != null) {
        }
        return fuelTransactions;
    }

    public List<FuelTransaction> findFuelTransactions(Institution institution, Institution fromInstitution, Institution toInstitution,
            List<Vehicle> vehicles, Date fromDateTime, Date toDateTime,
            Boolean issued,
            Boolean cancelled,
            Boolean rejected,
            List<FuelTransactionType> txTypes,
            FuelTransactionType type) {
        String j = "SELECT ft "
                + " FROM FuelTransaction ft "
                + " WHERE ft.retired = false";
        Map<String, Object> params = new HashMap<>();

        if (institution != null) {
            j += " AND ft.institution = :institution";
            params.put("institution", institution);
        }
        if (fromInstitution != null) {
            j += " AND ft.fromInstitution = :fromInstitution";
            params.put("fromInstitution", fromInstitution);
        }
        if (toInstitution != null) {
            j += " AND ft.toInstitution = :toInstitution";
            params.put("toInstitution", toInstitution);
        }
        if (vehicles != null && !vehicles.isEmpty()) {
            j += " AND ft.vehicle IN :vehicles";
            params.put("vehicles", vehicles);
        }
        if (fromDateTime != null) {
            j += " AND ft.requestAt >= :fromDateTime";
            params.put("fromDateTime", fromDateTime);
        }
        if (toDateTime != null) {
            j += " AND ft.requestAt <= :toDateTime";
            params.put("toDateTime", toDateTime);
        }
        if (issued != null) {
            j += " AND ft.issued = :issued ";
            params.put("issued", issued);
        }
        if (cancelled != null) {
            j += " AND ft.cancelled = :cancelled ";
            params.put("cancelled", cancelled);
        }
        if (rejected != null) {
            j += " AND ft.rejected = :rejected ";
            params.put("rejected", rejected);
        }
        if (type != null) {
            j += " AND ft.transactionType = :ftxs ";
            params.put("ftxs", type);
        }
        List<FuelTransaction> fuelTransactions = getFacade().findByJpql(j, params);
        if (fuelTransactions != null) {
        }
        return fuelTransactions;
    }

    public String navigateToListInstitutionIssues() {
        return "/issues/list";
    }

    public String navigateToIssueMultipleRequests() {
        institution = webUserController.getLoggedInstitution();
        fillDepotToIssueList();
        return "/issues/issue_multiple";
    }

    public String navigateToListToIssueRequestsForDepot() {
        institution = webUserController.getLoggedInstitution();
        fillDepotToIssueList();
        return "/sltb/reports/list_to_issue_depot";
    }

    public String navigateToListIssuedRequestsFormDepot() {
        institution = webUserController.getLoggedInstitution();
        fillIssuedRequestsFromDepotList();
        return "/sltb/reports/list_issued_from_depot";
    }

    public String navigateToListRejectedIssueRequestsFormDepot() {
        institution = webUserController.getLoggedInstitution();
        fillRejectedIssueRequestsFromDepotList();
        return "/sltb/reports/list_rejected_issue_requests_from_depot";
    }

    public FuelTransaction getSelected() {
        return selected;
    }

    public void setSelected(FuelTransaction selected) {
        this.selected = selected;
    }

    public FuelTransaction find(Object id) {
        return fuelTransactionFacade.find(id);
    }

    private FuelTransactionHistoryFacade getFacade() {
        return fuelTransactionHistoryFacade;
    }

    public void save(FuelTransaction saving) {
        if (saving == null) {
            return;
        }
        if (saving.getId() == null) {
            fuelTransactionFacade.create(saving);
        } else {
            fuelTransactionFacade.edit(saving);
        }
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<FuelTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FuelTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<FuelTransaction> getSelectedTransactions() {
        return selectedTransactions;
    }

    public void setSelectedTransactions(List<FuelTransaction> selectedTransactions) {
        this.selectedTransactions = selectedTransactions;
    }

    public FuelTransactionHistory getSelectedTransactionHistory() {
        return selectedTransactionHistory;
    }

    public void setSelectedTransactionHistory(FuelTransactionHistory selectedTransactionHistory) {
        this.selectedTransactionHistory = selectedTransactionHistory;
    }

    public List<FuelTransactionHistory> getSelectedTransactionHistories() {
        return selectedTransactionHistories;
    }

    public void setSelectedTransactionHistories(List<FuelTransactionHistory> selectedTransactionHistories) {
        this.selectedTransactionHistories = selectedTransactionHistories;
    }

    public List<FuelTransactionHistory> getTransactionHistories() {
        return transactionHistories;
    }

    public void setTransactionHistories(List<FuelTransactionHistory> transactionHistories) {
        this.transactionHistories = transactionHistories;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheMonth();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonController.endOfTheMonth();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String navigateToViewInstitutionFuelRequestToSltbDepot() {
        return "/requests/requested";
    }

    public String navigateToViewDepotFuelRequestToCpc() {
        return "/moh/requested";
    }

    public String getSearchingFuelRequestVehicleNumber() {
        return searchingFuelRequestVehicleNumber;
    }

    public void setSearchingFuelRequestVehicleNumber(String searchingFuelRequestVehicleNumber) {
        this.searchingFuelRequestVehicleNumber = searchingFuelRequestVehicleNumber;
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
