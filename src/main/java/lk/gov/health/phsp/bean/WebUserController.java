package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import lk.gov.health.phsp.entity.Driver;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.PrivilegeTreeNode;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.facade.PersonFacade;
import lk.gov.health.phsp.facade.UserPrivilegeFacade;
import org.primefaces.event.ColumnResizeEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.TreeNode;

@Named
@SessionScoped
public class WebUserController implements Serializable {

    /*
    EJBs
     */
    @EJB
    private WebUserFacade ejbFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private UserPrivilegeFacade userPrivilegeFacade;
    @EJB
    PersonFacade personFacade;
    /*
    Controllers
     */

    @Inject
    PersonController personController;
    @Inject
    private CommonController commonController;
    @Inject
    private AreaController areaController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private ItemController itemController;
    @Inject
    ExcelReportController reportController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    VehicleApplicationController vehicleApplicationController;
    @Inject
    DriverApplicationController driverApplicationController;
    @Inject
    WebUserApplicationController webUserApplicationController;
    @Inject
    DashboardApplicationController dashboardApplicationController;
    @Inject
    DashboardController dashboardController;
    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    MenuController menuController;
    @Inject
    VehicleController vehicleController;
    /*
    Variables
     */
    private List<WebUser> items = null;
    private List<WebUser> selectedUsers;

    private List<WebUser> managableUsers;
    private List<Institution> loggableInstitutions;
    private List<Vehicle> managableVehicles;
    private List<Driver> managableDrivers;

    private WebUserRole userRole;

    private Area selectedProvince;
    private Area selectedDistrict;
    private Area selectedDsArea;
    private Area selectedGnArea;
    private Institution selectedLocation;
    private Item selectedSourceOfFund;
    private Double selectedFundValue;
    private Item selectedFundUnit;
    private String selectedFundComments;

    private List<Area> districtsAvailableForSelection;

    private List<Area> selectedGnAreas;
    private Area[] selectedProvinces;

    private WebUser current;
    private Upload currentUpload;
    private Institution institution;

    private WebUser loggedUser;
    private String userName;
    private String password;
    private String passwordReenter;
    private MapModel emptyModel;
    private List<UserPrivilege> loggedUserPrivileges;

    private UploadedFile file;
    private String comments;

    private StreamedContent downloadingFile;

    private Date fromDate;
    private Date toDate;

    private Integer year;
    private Area province;
    private Area district;
    private Institution location;
    private Boolean allIslandProjects;
    private String searchKeyword;

    private String loginRequestResponse;

    private String locale;

    private WebUserRole assumedRole;
    private Institution assumedInstitution;
    private Area assumedArea;
    private List<UserPrivilege> assumedPrivileges;

    int reportTabIndex;
    private int indicatorTabIndex;
    private int metadataTabIndex;

    private String ipAddress;

    private String bulkText;
    Area area;
    List<WebUser> addedUsers;

    private Institution loggedInstitution;

    /**
     *
     * Privileges
     *
     */
    private TreeNode allPrivilegeRoot;
    private TreeNode myPrivilegeRoot;
    private TreeNode[] selectedNodes;
    private TreeNode[] selectedNodeSet;

    private InstitutionType institutionType;

    @PostConstruct
    public void init() {
        emptyModel = new DefaultMapModel();
        findIpAddress();
    }

    @PreDestroy
    public void sessionDestroy() {
        userTransactionController.recordTransaction("Invalidating the Session", this.toString());
    }

    public boolean isDieselMenuAvailable() {
        if (loggedUser == null) {
            return false;
        }
        if (loggedUser.getWebUserRole() == null) {
            return false;
        }
        if (loggedUser.getWebUserRoleLevel() == null) {
            return false;
        }
        if(loggedUser.getWebUserRoleLevel()!=WebUserRoleLevel.FUEL_REQUESTING_INSTITUTION){
            return false;
        }
        return true;
    }
    
    public boolean isDieselFuelRequestMenuAvailable() {
        if (loggedUser == null) {
            return false;
        }
        if (loggedUser.getWebUserRole() == null) {
            return false;
        }
        if (loggedUser.getWebUserRoleLevel() == null) {
            return false;
        }
        if(loggedUser.getWebUserRoleLevel()!=WebUserRoleLevel.FUEL_REQUESTING_INSTITUTION){
            return false;
        }
        if(loggedUser.getWebUserRole()!=WebUserRole.INSTITUTION_TRANSPORT){
            return false;
        }
        return true;
    }
    
    public boolean isDieselSpecialFuelRequestMenuAvailable() {
        if (loggedUser == null) {
            return false;
        }
        if (loggedUser.getWebUserRole() == null) {
            return false;
        }
        if (loggedUser.getWebUserRoleLevel() == null) {
            return false;
        }
        if(loggedUser.getWebUserRoleLevel()!=WebUserRoleLevel.FUEL_REQUESTING_INSTITUTION){
            return false;
        }
        if(loggedUser.getWebUserRole()==WebUserRole.INSTITUTION_TRANSPORT || loggedUser.getWebUserRole() == WebUserRole.INSTITUTION_ADMINISTRATOR){
            return true;
        }
        return false;
    }
    
    public boolean isDieselFuelRequestMarkAsReceivedMenuAvailable() {
        if (loggedUser == null) {
            return false;
        }
        if (loggedUser.getWebUserRole() == null) {
            return false;
        }
        if (loggedUser.getWebUserRoleLevel() == null) {
            return false;
        }
        if(loggedUser.getWebUserRoleLevel()!=WebUserRoleLevel.FUEL_REQUESTING_INSTITUTION){
            return false;
        }
        if(loggedUser.getWebUserRole()==WebUserRole.INSTITUTION_ACCOUNTS || loggedUser.getWebUserRole() == WebUserRole.INSTITUTION_ADMINISTRATOR){
            return true;
        }
        return false;
    }
    
    

    public void onResize(ColumnResizeEvent event) {
        String viewId = event.getFacesContext().getViewRoot().getViewId();
        String columnId = event.getColumn().getClientId();
        Integer w = event.getWidth();
        Integer h = event.getHeight();
        String colName = event.getColumn().getHeaderText();
    }

    private void findIpAddress() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

    }

    public String parseDate(Date complexDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(complexDate);
    }

    public String displayUsers(Institution ins) {
        String us = "";
        for (WebUser wu : webUserApplicationController.getItems()) {
            if (wu.getInstitution() != null && wu.getInstitution().equals(ins)) {
                if (!us.equals("")) {
                    us += ", ";
                }
                us += wu.getName();
            }
        }
        return us;
    }

    public List<Institution> findAutherizedInstitutions() {
        Set<Institution> insSet = new HashSet<>();
        if (loggedUser == null || loggedUser.getInstitution() == null) {
            return new ArrayList<>(insSet);
        }
        insSet.add(loggedUser.getInstitution());
        insSet.addAll(institutionApplicationController.findChildrenInstitutions(loggedUser.getInstitution()));

        return new ArrayList<>(insSet);
    }

    public List<Institution> findAuthorizedInstitutions() {
        Set<Institution> insSet = new HashSet<>();
        if (loggedUser == null || loggedUser.getInstitution() == null) {
            return new ArrayList<>(insSet);
        }
        insSet.add(loggedUser.getInstitution());
        insSet.addAll(institutionApplicationController.findChildrenInstitutions(loggedUser.getInstitution()));

        return new ArrayList<>(insSet);
    }

    public List<Vehicle> findAutherizedVehicles() {
        List<Vehicle> ins = new ArrayList<>();
        if (loggedUser == null) {
            return ins;
        }
        if (loggedUser.getInstitution() == null) {
            return ins;
        }

        switch (getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
                ins.addAll(vehicleApplicationController.getVehicles());
                break;
            default:
                ins.addAll(vehicleController.fillVehicles(loggableInstitutions));
        }
        return ins;
    }

    public List<WebUser> findManagableUsers() {
        List<WebUser> users = new ArrayList<>();
        if (loggedUser == null) {
            return users;
        }
        if (loggedUser.getInstitution() == null) {
            return users;
        }

        switch (getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
                users.addAll(webUserApplicationController.getItems());
                break;
            default:
                users.addAll(fillWebUsers(loggableInstitutions));
                break;
        }
        return users;
    }

    private List<WebUser> fillWebUsers(List<Institution> inss) {
//        System.out.println("inss = " + inss);
        List<WebUser> wus = new ArrayList<>();
        if (inss == null) {
            return wus;
        }
        if (inss.isEmpty()) {
            return wus;
        }

        for (WebUser w : webUserApplicationController.getItems()) {
            boolean canInclude = false;
            if (w.getInstitution() == null) {
                continue;
            }
            for (Institution ins : inss) {
                if (w.getInstitution().equals(ins)) {
                    canInclude = true;
                }
            }
            if (canInclude) {
                wus.add(w);
            }
        }
        return wus;
    }

    public List<Institution> findMailBranchInstitutions() {
        List<Institution> ins = new ArrayList<>();
        if (loggedUser == null) {
            return ins;
        }
        if (loggedUser.getInstitution() == null) {
            return ins;
        }
        if (loggedUser.getInstitution().getParent() == null) {
            return ins;
        }
        ins.add(loggedUser.getInstitution().getParent());
        ins.addAll(institutionApplicationController.findChildrenInstitutions(loggedUser.getInstitution().getParent()));
        return ins;
    }

    public List<Institution> findAutherizedPmcis() {
        List<Institution> ins = new ArrayList<>();
        if (loggedUser == null) {
            return ins;
        }
        if (loggedUser.getInstitution() == null) {
            return ins;
        }
        if (loggedUser.getInstitution().isPmci()) {
            ins.add(loggedUser.getInstitution());
        }
        ins.addAll(institutionController.findChildrenPmcis(loggedUser.getInstitution()));
        return ins;
    }

    public boolean hasSelectedUsers() {
        return this.selectedUsers != null && !this.selectedUsers.isEmpty();
    }

    public String toManageInstitutionUsers() {
        String j = "select u from WebUser u "
                + " where u.retired=false "
                + " and u.institution in :inss ";
        Map m = new HashMap();
        m.put("inss", getLoggableInstitutions());
        items = getFacade().findByJpql(j, m);
        userTransactionController.recordTransaction("To Manage Institution Users");
        return "/insAdmin/user_list";
    }

    public void prepareListingAllUsers() {
        items = webUserApplicationController.getItems();
    }

    public void prepareListingUsersUnderMe() {
        items = new ArrayList<>();
        if (loggedUser == null) {
            return;
        }
        Institution i;
        if (loggedUser.getInstitution() == null) {
            return;
        } else {
            i = loggedUser.getInstitution();
        }

        for (WebUser wu : webUserApplicationController.getItems()) {
            if (wu.getInstitution() == null) {
            } else {
                if (wu.getInstitution().equals(i)) {
                    items.add(wu);
                } else {
                    if (wu.getInstitution().getParent() == null) {
                    } else {
                        if (wu.getInstitution().getParent().equals(i)) {
                            items.add(wu);
                        } else {
                            if (wu.getInstitution().getParent().getParent() == null) {
                            } else {
                                if (wu.getInstitution().getParent().getParent().equals(i)) {
                                    items.add(wu);
                                } else {
                                    if (wu.getInstitution().getParent().getParent().getParent() == null) {
                                    } else {
                                        if (wu.getInstitution().getParent().getParent().getParent().equals(i)) {
                                            items.add(wu);
                                        } else {
                                            if (wu.getInstitution().getParent().getParent().getParent().getParent() == null) {
                                            } else {
                                                if (wu.getInstitution().getParent().getParent().getParent().getParent().equals(i)) {
                                                    items.add(wu);
                                                } else {
                                                    if (wu.getInstitution().getParent().getParent().getParent().getParent().getParent() == null) {
                                                    } else {
                                                        if (wu.getInstitution().getParent().getParent().getParent().getParent().getParent().equals(i)) {
                                                            items.add(wu);
                                                        } else {

                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }

                                }
                            }

                        }
                    }
                }
            }
        }
    }

    public String toAddNewUserByInsAdmin() {
        current = new WebUser();
        password = "";
        passwordReenter = "";
        return "/insAdmin/user_new";
    }

    public void prepareToAddNewUser() {
        current = new WebUser();
        password = "";
        passwordReenter = "";
    }

    public String toInsAdmin() {
        return "/insAdmin/index";
    }

    public String toAdministration() {
        if (loggedUser == null) {
            JsfUtil.addErrorMessage("Not Logged");
            return "";
        }
        if (loggedUser.getWebUserRole() == null) {
            JsfUtil.addErrorMessage("No Role for logged user");
        }
        String url = "";
        switch (loggedUser.getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
                url = "/provincial/administration/index";
                break;
            case HEALTH_MINISTRY:
                url = "/regional/administration/index";
                break;
            default:
        }
        return url;
    }

    public String toChangeMyDetails() {
        if (loggedUser == null) {
            return "";
        }
        current = loggedUser;
        return "/change_my_details";
    }

    public String toChangeMyInstitutionDetails() {
        if (loggedUser == null) {
            return "";
        }
        current = loggedUser;
        return "/change_my_institution_details";
    }

    public String toChangeMyUsername() {
        if (loggedUser == null) {
            return "";
        }
        current = loggedUser;
        return "/change_my_username";
    }

    public String toChangeMyPassword() {
        if (loggedUser == null) {
            return "";
        }
        password = "";
        passwordReenter = "";
        current = loggedUser;
        return "/change_my_password";
    }

    public String viewMedia() {
        if (currentUpload == null) {
            JsfUtil.addErrorMessage("Nothing is selected to view");
            return "";
        }
        if (currentUpload.getFileType().contains("image")) {
            return "/view_image";
        } else if (currentUpload.getFileType().contains("pdf")) {
            return "/view_pdf";
        } else {
            JsfUtil.addErrorMessage("NOT an image of a pdf file. ");
            return "";
        }
    }

    public String toSubmitClientRequest() {
        return "/finalize_client_request";
    }

    public void sendSubmitClientRequestConfirmationEmail() {

    }

    public List<Institution> completeLoggableInstitutions(String qry) {
        List<Institution> ins = new ArrayList<>();
        if (qry == null) {
            return ins;
        }
        if (qry.trim().equals("")) {
            return ins;
        }
        qry = qry.trim().toLowerCase();
        for (Institution i : getLoggableInstitutions()) {
            if (i.getName() == null) {
                continue;
            }
            if (i.getName().toLowerCase().contains(qry)) {
                ins.add(i);
            }
        }
        return ins;
    }

    public List<Vehicle> completeManagableVehicles(String qry) {
        List<Vehicle> ins = new ArrayList<>();
        if (qry == null) {
            return ins;
        }
        if (qry.trim().equals("")) {
            return ins;
        }
        qry = qry.trim().toLowerCase();
        for (Vehicle i : getManagableVehicles()) {
            if (i.getName() == null) {
                continue;
            }
            if (i.getName().toLowerCase().contains(qry)) {
                ins.add(i);
            }
            if (i.getVehicleNumber().toLowerCase().contains(qry)) {
                ins.add(i);
            }
        }
        return ins;
    }

    public List<Driver> completeManagableDrivers(String qry) {
        List<Driver> ins = new ArrayList<>();
        if (qry == null) {
            return ins;
        }
        if (qry.trim().equals("")) {
            return ins;
        }
        qry = qry.trim().toLowerCase();
        for (Driver i : getManagableDrivers()) {
            if (i.getName() == null) {
                continue;
            }
            if (i.getName().toLowerCase().contains(qry)) {
                ins.add(i);
            }
            if (i.getNic().toLowerCase().contains(qry)) {
                ins.add(i);
            }
        }
        return ins;
    }

    public String registerUser() {
        if (!current.getWebUserPassword().equals(password)) {
            JsfUtil.addErrorMessage("Passwords are not matching. Please retry.");
            return "";
        }
        current.setWebUserRole(WebUserRole.INSTITUTION_ADMINISTRATOR);
        try {
            getFacade().create(current);
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Username already taken. Please enter a different username");
            return "";
        }

        setLoggedUser(current);
        JsfUtil.addSuccessMessage("Your Details Added as an institution user. Please contact us for changes");
        return "/index";
    }

    public String logOut() {
        userTransactionController.recordTransaction("Logout");
        loggedUser = null;
        loggedInstitution = null;
        return "/index";
    }

    public String login() {
        loggableInstitutions = null;
        managableVehicles = null;
        managableDrivers = null;
        if (userName == null || userName.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter a Username");
            return "";
        }
        if (password == null || password.trim().equals("")) {
            JsfUtil.addErrorMessage("Please enter the Password");
            return "";
        }
        if (!checkLogin()) {
            JsfUtil.addErrorMessage("Username/Password Error. Please retry.");
            userTransactionController.recordTransaction("Failed Login Attempt", userName);
            return "";
        }
        loggedUserPrivileges = userPrivilegeList(loggedUser);
        if (loggedUser != null) {
            loggedInstitution = loggedUser.getInstitution();
        }
        executeSuccessfulLoginActions();
        return "/index";
    }

    private void executeSuccessfulLoginActions() {
        JsfUtil.addSuccessMessage("Successfully Logged");
        userTransactionController.recordTransaction("Successful Login");
        loggableInstitutions = institutionApplicationController.findChildrenInstitutions(loggedInstitution);
        loggableInstitutions.add(loggedInstitution);
        managableVehicles = vehicleApplicationController.findVehiclesByInstitutions(loggableInstitutions);
        managableDrivers = driverApplicationController.findDriversByInstitutions(loggableInstitutions);
        managableUsers = findManagableUsers();

        Calendar c = Calendar.getInstance();
        toDate = c.getTime();
        c.add(Calendar.DAY_OF_MONTH, -7);
        fromDate = c.getTime();
        dashboardController.prepareDashboard();
    }

    public String toChangeLoggedInstitution() {
        return "/webUser/change_logged_institute";
    }

    public String changeLoggedInstitution() {
        executeSuccessfulLoginActions();
        return "/index";
    }

    public void fillAllUsers() {
        items = webUserApplicationController.getItems();
    }

    private boolean checkLogin() {
        if (getFacade() == null) {
            JsfUtil.addErrorMessage("Server Error");
            return false;
        }

        if (thisIsFirstLogin()) {
            createFirstUser();
        }

        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE u.name=:userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", userName.trim().toLowerCase());
        m.put("ret", false);
//        JsfUtil.addErrorMessage("M=" + m);
//        JsfUtil.addErrorMessage("S=" + temSQL);
        loggedUser = getFacade().findFirstByJpql(temSQL, m);
//        JsfUtil.addErrorMessage("User = " + loggedUser);
        if (loggedUser == null) {
            return false;
        }
        if (commonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            return true;
        } else {
            loggedUser = null;
            return false;
        }
    }

    public String toHome() {
        userTransactionController.recordTransaction("To Home");
        return "/index";
    }

    public String loginForMobile() {
        loginRequestResponse = "";
        if (userName == null || userName.trim().equals("")) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        if (password == null || password.trim().equals("")) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        if (!checkLogin(false)) {
            loginRequestResponse += "Wrong Isername. Please go back to settings and update.";
            return "/mobile/login_failure";
        }
        return "/mobile/index";
    }

    public List<WebUser> completeUsers(String qry) {
        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE lower(u.name) like :userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", "%" + qry.trim().toLowerCase() + "%");
        m.put("ret", false);
        return getFacade().findByJpql(temSQL, m);
    }

    public List<WebUser> completeUsersByWord(String nameQry) {
        List<WebUser> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<WebUser> allIns = webUserApplicationController.getItems();
        nameQry = nameQry.trim();
        String words[] = nameQry.split("\\s+");

        for (WebUser i : allIns) {
            boolean allWordsMatch = true;

            for (String word : words) {
                boolean thisWordMatch;
                word = word.trim().toLowerCase();
                if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getVehiclesModel() != null && i.getVehiclesModel().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getPerson() != null && i.getPerson().getName() != null && i.getPerson().getName().toLowerCase().contains(word)) {
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

    private boolean checkLogin(boolean withoutPassword) {
        if (loggedUser != null && withoutPassword) {
            return true;
        }

        if (getFacade() == null) {
            JsfUtil.addErrorMessage("Server Error");
            return false;
        }

        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE lower(u.name)=:userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", userName.trim().toLowerCase());
        m.put("ret", false);
        loggedUser = getFacade().findFirstByJpql(temSQL, m);
        if (loggedUser == null) {
            return false;
        }
        if (withoutPassword) {
            return true;
        }
        if (commonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            return true;
        } else {
            loggedUser = null;
            return false;
        }

    }

    private boolean thisIsFirstLogin() {
        String temSQL;
        temSQL = "SELECT u "
                + " FROM WebUser u ";
        loggedUser = getFacade().findFirstByJpql(temSQL);
        return loggedUser == null;
    }

    private void createFirstUser() {
        Institution ins = new Institution();
        ins.setName("Institution");
        getInstitutionFacade().create(ins);

        Person p = new Person();
        p.setName(userName);
        personFacade.create(p);

        WebUser u = new WebUser();
        u.setName(userName);
        u.setActivated(true);
        u.setWebUserPassword(commonController.hash(password));
        u.setPerson(p);

        getFacade().create(u);
        JsfUtil.addSuccessMessage("A First Login Detected. A new User Created. Please login now.");

    }

    List<Privilege> getInitialPrivileges(WebUserRole role) {
        List<Privilege> wups = new ArrayList<>();
        if (role == null) {
            return wups;
        }
        switch (role) {
            case INSTITUTION_ADMINISTRATOR:
                wups.add(Privilege.Manage_Institution_Users);
                wups.add(Privilege.Manage_Authorised_Institutions);
            case INSTITUTION_SUPER_USER:
            case INSTITUTION_USER:
                //Menu
                wups.add(Privilege.File_Management);
                wups.add(Privilege.Institutional_Mail_Management);
                wups.add(Privilege.User);
                wups.add(Privilege.Search_File);
                wups.add(Privilege.Retire_File);
                break;
            case SYSTEM_ADMINISTRATOR:
                wups.add(Privilege.Monitoring_and_evaluation);
                wups.add(Privilege.Monitoring_and_evaluation_reports);
                wups.add(Privilege.View_aggragate_date);
                wups.add(Privilege.File_Management);
                wups.add(Privilege.Institutional_Mail_Management);
                wups.add(Privilege.User);
                wups.add(Privilege.Add_File);
                wups.add(Privilege.Search_File);
                wups.add(Privilege.Retire_File);
//                Motinoring & Evaluation
                wups.add(Privilege.Monitoring_and_evaluation);
                wups.add(Privilege.Monitoring_and_evaluation_reports);
                wups.add(Privilege.View_individual_data);
                wups.add(Privilege.View_aggragate_date);

                break;

        }
        return wups;
    }

    public void addAllWebUserPrivileges(WebUser u) {
        List<Privilege> ps = Arrays.asList(Privilege.values());
        addWebUserPrivileges(u, ps);
    }

    public void addWebUserPrivileges(WebUser u, List<Privilege> ps) {
        for (Privilege p : ps) {
            addWebUserPrivileges(u, p);
        }
    }

    public void addWebUserPrivileges(WebUser u, Privilege p) {
        if (u == null || u.getId() == null) {
            return;
        }
        try {
            String j = "Select up from UserPrivilege up where "
                    + " up.webUser=:u and up.privilege=:p "
                    + " order by up.id desc";
            Map m = new HashMap();
            m.put("u", u);
            m.put("p", p);
            UserPrivilege up = getUserPrivilegeFacade().findFirstByJpql(j, m);
            if (up == null) {
                up = new UserPrivilege();
                up.setCreatedAt(new Date());
                up.setCreatedBy(loggedUser);
                up.setWebUser(u);
                up.setPrivilege(p);
                getUserPrivilegeFacade().create(up);
            } else {
                up.setRetired(false);
                up.setCreatedAt(new Date());
                up.setCreatedBy(loggedUser);
                up.setWebUser(u);
                up.setPrivilege(p);

                getUserPrivilegeFacade().edit(up);
            }
        } catch (Exception e) {
        }

    }

    public boolean hasPrivilege(String privilege) {
        Privilege p;
        try {
            p = Privilege.valueOf(privilege);
            if (p != null) {
                return hasPrivilege(p);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasPrivilege(Privilege p) {
        return hasPrivilege(loggedUserPrivileges, p);
    }

    public boolean hasPrivilege(List<UserPrivilege> ups, Privilege p) {
        boolean f = false;
        for (UserPrivilege up : ups) {
            if (up.getPrivilege().equals(p)) {
                f = true;
            }
        }
        return f;
    }

    public List<UserPrivilege> userPrivilegeList(WebUser u) {
        return userPrivilegeList(u, null);
    }

    public List<UserPrivilege> userPrivilegeList(Item i) {
        return userPrivilegeList(null, i);
    }

    public List<UserPrivilege> userPrivilegeList(WebUser u, Item i) {
        String j = "select p from UserPrivilege p "
                + " where p.retired=false ";
        Map m = new HashMap();
        if (u != null) {
            j += " and p.webUser=:u ";
            m.put("u", u);
        }
        if (i != null) {
            j += " and p.item=:i ";
            m.put("i", i);
        }
        return getUserPrivilegeFacade().findByJpql(j, m);
    }

    public WebUserController() {
    }

    public WebUser getSelected() {
        return current;
    }

    private WebUserFacade getFacade() {
        return ejbFacade;
    }

    public String prepareList() {
        recreateModel();
        return "manage_users";
    }

    public String prepareView() {
        return "/webUser/View";
    }

    public String toCreateNewUserBySysAdmin() {
        current = new WebUser();
        password = "";
        passwordReenter = "";
        userTransactionController.recordTransaction("Create New User By SysAdmin");
        return "/webUser/create_new_user";
    }

    public String create() {
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        return "index";
    }

    public String saveNewWebUserByInsAdmin() {
        if (current == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-Passwords do NOT match");
            return "";
        }
        if (userNameExsists(current.getName())) {
            JsfUtil.addErrorMessage("Username already exists. Please try another.");
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-Username already exists");
            return "";
        }
        if (current.getId() != null) {
            current.setLastEditBy(loggedUser);
            current.setLastEditeAt(new Date());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("User Details Updated");
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-User Details Updated");
            return "";
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            webUserApplicationController.getItems().add(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
            userTransactionController.recordTransaction("Save NewWebUser By InsAdmin-Successfully");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        webUserApplicationController.resetWebUsers();
        webUserApplicationController.getItems();
        userTransactionController.recordTransaction("New Web User Added by InsAdmin");
        return menuController.toListUsers();
    }

    public boolean userNameExsists() {
        if (getSelected() == null) {
            return false;
        }

        boolean une = userNameExsists(getSelected().getName());
        return une;
    }

    public boolean userNameExsists(String un) {
        if (un == null) {
            return false;
        }
        String j = "select u from WebUser u where lower(u.name)=:un order by u.id desc";
        Map m = new HashMap();
        m.put("un", un.toLowerCase());
        WebUser u = getFacade().findFirstByJpql(j, m);
        return u != null;
    }

    public String saveNewWebUser() {
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        if (userNameExsists(getSelected().getName())) {
            JsfUtil.addErrorMessage("Username already exists. Please try another.");
            return "";
        }
        if (getSelected().getId() != null) {
            getSelected().setLastEditBy(loggedUser);
            getSelected().setLastEditeAt(new Date());
            getFacade().edit(getSelected());
            JsfUtil.addSuccessMessage("User Details Updated");
            return menuController.toListUsers();
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
            userTransactionController.recordTransaction("New user Created");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        webUserApplicationController.resetWebUsers();
        userTransactionController.recordTransaction("New User Created");
        return menuController.toListUsers();
    }

    public String saveNewWebUserDataEntry() {
        if (getSelected() == null) {
            JsfUtil.addErrorMessage("Noting to save");
            return "";
        }
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match");
            return "";
        }
        if (userNameExsists(getSelected().getName())) {
            JsfUtil.addErrorMessage("Username already exists. Please try another.");
            return "";
        }
        if (getSelected().getId() != null) {
            getSelected().setLastEditBy(loggedUser);
            getSelected().setLastEditeAt(new Date());
            getFacade().edit(getSelected());
            JsfUtil.addSuccessMessage("User Details Updated");
            return menuController.toListUsers();
        }
        try {
            current.setWebUserPassword(commonController.hash(password));
            current.setCreatedAt(new Date());
            current.setCreater(loggedUser);
            getFacade().create(current);
            addWebUserPrivileges(current, getInitialPrivileges(current.getWebUserRole()));
            JsfUtil.addSuccessMessage(("A new User Created Successfully."));
            userTransactionController.recordTransaction("New user Created");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("Error Occured. Please change username and try again."));
            return "";
        }
        if (items == null) {
            fillAllUsers();
        }
        getItems().add(current);
        userTransactionController.recordTransaction("New User Created");
        return menuController.toListUsersDataentry();
    }

    public String prepareEdit() {
        return "/webUser/Edit";
    }

    public String prepareEditIns() {
        userTransactionController.recordTransaction("Edit user list By InsAdmin");
        return "/insAdmin/user_edit";
    }

    public void prepareEditPassword() {
        password = "";
        passwordReenter = "";
    }

    public String prepareEditPasswordIns() {
        password = "";
        passwordReenter = "";
        userTransactionController.recordTransaction("Edit Password user list By InsAdmin");
        return "/insAdmin/user_password";
    }

    public String deleteUser() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return "";
        }
        current.setRetired(true);
        current.setRetirer(getLoggedUser());
        current.setRetiredAt(new Date());
        save(current);
        webUserApplicationController.getItems().remove(current);
        getItems().remove(current);
        return menuController.toListUsers();
    }

    public void clearAddedUsers() {
        addedUsers = new ArrayList<>();
    }

    public String addMultipleUsers() {
        if (addedUsers == null) {
            addedUsers = new ArrayList<>();
        }
        if (bulkText == null || bulkText.trim().equals("")) {
            JsfUtil.addErrorMessage("Text ?");
            return "";
        }
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution?");
            return "";
        }
        if (userRole == null) {
            JsfUtil.addErrorMessage("Institution?");
            return "";
        }

        String lines[] = bulkText.split("\\r?\\n");

        for (String line : lines) {
            if (line == null || line.trim().equals("")) {
                continue;
            }
            line = line.trim();

            Person newPerson = new Person();
            newPerson.setName(line);
            personController.save(newPerson);

            WebUser newUser = new WebUser();
            newUser.setName(line.toLowerCase());
            newUser.setPerson(newPerson);
            newUser.setInstitution(institution);
            newUser.setArea(area);
            newUser.setWebUserRole(userRole);
            newUser.setWebUserPassword(commonController.hash("abcd1234"));
            newUser.setCreatedAt(new Date());
            newUser.setCreater(getLoggedUser());
            save(newUser);
            addWebUserPrivileges(newUser, getInitialPrivileges(newUser.getWebUserRole()));
            save(newUser);
            addedUsers.add(newUser);
        }

        bulkText = "";
        return "";
    }

    public String addMultipleInstitutionAndUsers() {
        addedUsers = new ArrayList<>();
        if (bulkText == null || bulkText.trim().equals("")) {
            JsfUtil.addErrorMessage("Text ?");
            return "";
        }
        if (institutionType == null) {
            JsfUtil.addErrorMessage("Institution Type?");
            return "";
        }
        if (institution == null) {
            JsfUtil.addErrorMessage("Institution?");
            return "";
        }
        if (userRole == null) {
            JsfUtil.addErrorMessage("Institution?");
            return "";
        }

        String lines[] = bulkText.split("\\r?\\n");

        String j;
        Map m;

        for (String line : lines) {
            if (line == null || line.trim().equals("")) {
                continue;
            }
            line = line.trim();

            Institution newIns;

            newIns = new Institution();
            newIns.setName(line);
            newIns.setTname(line);
            newIns.setSname(line);
            newIns.setCode(CommonController.prepareAsCode(line));
            newIns.setParent(institution);
            newIns.setDistrict(institution.getDistrict());
            newIns.setInstitutionType(institutionType);
            newIns.setPdhsArea(institution.getPdhsArea());
            newIns.setSupplyInstitution(institution.getSupplyInstitution());
            newIns.setProvince(institution.getProvince());
            newIns.setRdhsArea(institution.getRdhsArea());
            newIns.setCreatedAt(new Date());
            newIns.setCreater(getLoggedUser());
            institutionController.save(newIns);

            Person newPerson = new Person();
            newPerson.setName("System Administrator of " + line);
            personController.save(newPerson);

            WebUser newUser = new WebUser();
            String un = "sa_" + CommonController.prepareAsCode(line).toLowerCase();
            if (un.length() > 50) {
                un = un.substring(0, 49);
            }
            newUser.setName(un);
            newUser.setPerson(newPerson);
            newUser.setInstitution(newIns);
            newUser.setArea(institution.getRdhsArea());
            newUser.setWebUserRole(userRole);
            newUser.setWebUserPassword(commonController.hash("abcd1234"));
            newUser.setCreatedAt(new Date());
            newUser.setCreater(getLoggedUser());
            save(newUser);
            addWebUserPrivileges(newUser, getInitialPrivileges(newUser.getWebUserRole()));
            save(newUser);
            addedUsers.add(newUser);

        }

        bulkText = "";
        return "";
    }

    public void save(WebUser u) {
        try {
            if (u == null) {
                return;
            }
            if (u.getId() == null) {
                u.setCreatedAt(new Date());
                u.setCreater(getLoggedUser());
                getFacade().create(u);
            } else {
                u.setLastEditBy(getLoggedUser());
                u.setLastEditeAt(new Date());
                getFacade().edit(u);
            }
        } catch (Exception e) {
        }

    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Updated"));
            userTransactionController.recordTransaction("User Details Updated");
            return menuController.toListUsers();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }
    }

    public String updateUserPrivileges() {
        if (current == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return "";
        }
        List<UserPrivilege> userps = userPrivilegeList(current);
        List<Privilege> tps = new ArrayList<>();
        if (selectedNodes != null && selectedNodes.length > 0) {
            for (TreeNode node : selectedNodes) {
                Privilege p;
                p = ((PrivilegeTreeNode) node).getP();
                if (p != null) {
                    tps.add(p);
                }
            }
        }
        for (Privilege p : tps) {
            boolean found = false;
            for (UserPrivilege tup : userps) {

                if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                    found = true;
                }
            }
            if (!found) {
                addWebUserPrivileges(current, p);
            }
        }

        userps = userPrivilegeList(current);
        for (UserPrivilege tup : userps) {
            boolean found = false;
            for (Privilege p : tps) {
                if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                    found = true;
                }
            }
            if (!found) {
                tup.setRetired(true);
                tup.setRetiredAt(new Date());
                tup.setRetiredBy(loggedUser);
                getUserPrivilegeFacade().edit(tup);
            }
        }
        userTransactionController.recordTransaction("Updated User Privileges");
        return menuController.toListUsers();
    }

    public String updatePrivilagesPerUsers() {
        if (this.hasSelectedUsers()) {
            for (WebUser wu : selectedUsers) {
                List<UserPrivilege> userps = new ArrayList<>();
                userps = userPrivilegeList(wu);
                List<Privilege> tps = new ArrayList<>();
                if (selectedNodeSet != null && selectedNodeSet.length > 0) {
                    for (TreeNode node : selectedNodeSet) {
                        Privilege p = ((PrivilegeTreeNode) node).getP();
                        if (p != null) {
                            tps.add(p);
                        }
                    }
                }

                for (Privilege p : tps) {
                    boolean found = false;
                    for (UserPrivilege tup : userps) {
                        if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        addWebUserPrivileges(wu, p);
                    }
                }
            }
        }
        return "";
    }

    public String updateUserPrivilegesIns() {
        if (current == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return "";
        }
        List<UserPrivilege> userps = userPrivilegeList(current);
        List<Privilege> tps = new ArrayList<>();
        if (selectedNodes != null && selectedNodes.length > 0) {
            for (TreeNode node : selectedNodes) {
                Privilege p;
                p = ((PrivilegeTreeNode) node).getP();
                if (p != null) {
                    tps.add(p);
                }
            }
        }
        for (Privilege p : tps) {
            boolean found = false;
            for (UserPrivilege tup : userps) {

                if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                    found = true;
                }
            }
            if (!found) {
                addWebUserPrivileges(current, p);
            }
        }

        userps = userPrivilegeList(current);

        for (UserPrivilege tup : userps) {
            boolean found = false;
            for (Privilege p : tps) {
                if (p != null && tup.getPrivilege() != null && p.equals(tup.getPrivilege())) {
                    found = true;
                }
            }
            if (!found) {
                tup.setRetired(true);
                tup.setRetiredAt(new Date());
                tup.setRetiredBy(loggedUser);
                getUserPrivilegeFacade().edit(tup);
            }
        }
        return menuController.toAdministrationIndex();
    }

    public String updateMyDetails() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Your details Updated."));
            userTransactionController.recordTransaction("Updated My Details");
            return "/index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }
    }

    public String updateMyUserName() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Your details Updated."));
            userTransactionController.recordTransaction("Updated My Details");
            return "/index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Username already exists. Please select another.");
            return "";
        }
    }

    public String updateMyInstitutionDetails() {
        try {
            getInstitutionFacade().edit(current.getInstitution());
            userTransactionController.recordTransaction("Update My Institution Details");
            return "/index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return null;
        }
    }

    public String updateMyPassword() {
        current = loggedUser;
        if (current == null) {
            JsfUtil.addSuccessMessage(("Error. No Logged User"));
            return "";
        }

        if (!password.equals(passwordReenter)) {
            JsfUtil.addSuccessMessage(("Password Mismatch."));
            userTransactionController.recordTransaction("My Password Mismatch");
            return "";
        }
        current.setWebUserPassword(commonController.hash(password));
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Password Updated"));
            password = "";
            passwordReenter = "";
            userTransactionController.recordTransaction("My Password Updated");
            return "/index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
            return "";
        }
    }

    public void updateLoggedUser() {
        if (loggedUser == null) {
            return;
        }
        try {
            getFacade().edit(loggedUser);
            JsfUtil.addSuccessMessage(("Updated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    public String updatePassword() {
        if (!password.equals(passwordReenter)) {
            JsfUtil.addErrorMessage("Passwords do NOT match.");
            userTransactionController.recordTransaction("webUser Password not match");
            return "";
        }
        try {
            String hashedPassword = commonController.hash(password);
            current.setWebUserPassword(hashedPassword);
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(("Password Changed."));
            userTransactionController.recordTransaction("webUser Password Changed");
            return "index";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ("PersistenceErrorOccured"));
            userTransactionController.recordTransaction("webUser Password error");
            return null;
        }
    }

    public WebUserRole[] getWebUserRolesForInsAdmin() {
        List<WebUserRole> ars = findManagableRoles(loggedUser.getWebUserRole());
        WebUserRole[] rs = ars.toArray(new WebUserRole[0]);
        return rs;
    }

    public WebUserRole[] getWebUserRolesForInstitutionAdmin() {
        List<WebUserRole> urs = new ArrayList<>();
        urs.add(WebUserRole.INSTITUTION_ADMINISTRATOR);
        urs.add(WebUserRole.INSTITUTION_SUPER_USER);
        urs.add(WebUserRole.INSTITUTION_TRANSPORT);
        urs.add(WebUserRole.INSTITUTION_ADMINISTRATOR);
        urs.add(WebUserRole.INSTITUTION_USER);
        WebUserRole[] rs = urs.toArray(new WebUserRole[0]);
        return rs;
    }

    private List<WebUserRole> findManagableRoles(WebUserRole ur) {
        List<WebUserRole> urs = new ArrayList<>();
        switch (ur) {
            case SYSTEM_ADMINISTRATOR:
                urs.addAll(Arrays.asList(WebUserRole.values()));
                break;
//            case SUPER_USER:
//                urs.addAll(Arrays.asList(WebUserRole.values()));
//                urs.remove(WebUserRole.SYSTEM_ADMINISTRATOR);
//                break;
            case INSTITUTION_ADMINISTRATOR:
                urs.add(WebUserRole.INSTITUTION_ADMINISTRATOR);
//                urs.add(WebUserRole.INSTITUTION_SUPER_USER);
                urs.add(WebUserRole.INSTITUTION_ACCOUNTS);
                urs.add(WebUserRole.INSTITUTION_TRANSPORT);
//                urs.add(WebUserRole.INSTITUTION_USER);
                break;
//            case INSTITUTION_SUPER_USER:
//                urs.add(WebUserRole.INSTITUTION_SUPER_USER);
//                urs.add(WebUserRole.INSTITUTION_USER);
//                break;
//            case INSTITUTION_USER:
//                urs.add(WebUserRole.INSTITUTION_USER);
//                break;
//            case USER:
//                urs.add(ur);
        }
        return urs;
    }

    public List<WebUser> getItems() {
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public WebUser getWebUser(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public WebUser getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(WebUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public lk.gov.health.phsp.facade.WebUserFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(lk.gov.health.phsp.facade.WebUserFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public WebUser getCurrent() {
        return current;
    }

    public void setCurrent(WebUser current) {
        this.current = current;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public MapModel getEmptyModel() {
        return emptyModel;
    }

    public void setEmptyModel(MapModel emptyModel) {
        this.emptyModel = emptyModel;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public void setUploadFacade(UploadFacade uploadFacade) {
        this.uploadFacade = uploadFacade;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Upload getCurrentUpload() {
        return currentUpload;
    }

    public void setCurrentUpload(Upload currentUpload) {
        this.currentUpload = currentUpload;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DATE, 1);
            fromDate = c.getTime();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area[] getSelectedProvinces() {
        return selectedProvinces;
    }

    public void setSelectedProvinces(Area[] selectedProvinces) {
        this.selectedProvinces = selectedProvinces;
    }

    public List<Area> getSelectedGnAreas() {
        return selectedGnAreas;
    }

    public void setSelectedGnAreas(List<Area> selectedGnAreas) {
        if (selectedGnAreas == null) {
            selectedGnAreas = new ArrayList<>();
        }
        this.selectedGnAreas = selectedGnAreas;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Institution getLocation() {
        return location;
    }

    public void setLocation(Institution location) {
        this.location = location;
    }

    public Boolean getAllIslandProjects() {
        return allIslandProjects;
    }

    public void setAllIslandProjects(Boolean allIslandProjects) {
        this.allIslandProjects = allIslandProjects;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getLoginRequestResponse() {
        return loginRequestResponse;
    }

    public void setLoginRequestResponse(String loginRequestResponse) {
        this.loginRequestResponse = loginRequestResponse;
    }

    public String getLocale() {
        if (loggedUser != null) {
            locale = loggedUser.getDefLocale();
        }
        if (locale == null || locale.trim().equals("")) {
            locale = "en";
        }
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public String getPasswordReenter() {
        return passwordReenter;
    }

    public void setPasswordReenter(String passwordReenter) {
        this.passwordReenter = passwordReenter;
    }

    public Area getSelectedProvince() {
        return selectedProvince;
    }

    public void setSelectedProvince(Area selectedProvince) {
        this.selectedProvince = selectedProvince;
    }

    public Area getSelectedDistrict() {
        return selectedDistrict;
    }

    public void setSelectedDistrict(Area selectedDistrict) {
        this.selectedDistrict = selectedDistrict;
    }

    public Area getSelectedDsArea() {
        return selectedDsArea;
    }

    public void setSelectedDsArea(Area selectedDsArea) {
        this.selectedDsArea = selectedDsArea;
    }

    public Area getSelectedGnArea() {
        return selectedGnArea;
    }

    public void setSelectedGnArea(Area selectedGnArea) {
        this.selectedGnArea = selectedGnArea;
    }

    public Institution getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(Institution selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    public Item getSelectedSourceOfFund() {
        return selectedSourceOfFund;
    }

    public void setSelectedSourceOfFund(Item selectedSourceOfFund) {
        this.selectedSourceOfFund = selectedSourceOfFund;
    }

    public Double getSelectedFundValue() {
        return selectedFundValue;
    }

    public void setSelectedFundValue(Double selectedFundValue) {
        this.selectedFundValue = selectedFundValue;
    }

    public Item getSelectedFundUnit() {
        return selectedFundUnit;
    }

    public void setSelectedFundUnit(Item selectedFundUnit) {
        this.selectedFundUnit = selectedFundUnit;
    }

    public String getSelectedFundComments() {
        return selectedFundComments;
    }

    public void setSelectedFundComments(String selectedFundComments) {
        this.selectedFundComments = selectedFundComments;
    }

    public TreeNode getAllPrivilegeRoot() {
        allPrivilegeRoot = webUserApplicationController.getAllPrivilegeRoot();
        return allPrivilegeRoot;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public TreeNode getMyPrivilegeRoot() {
        return myPrivilegeRoot;
    }

    public void setMyPrivilegeRoot(TreeNode myPrivilegeRoot) {
        this.myPrivilegeRoot = myPrivilegeRoot;
    }

    public UserPrivilegeFacade getUserPrivilegeFacade() {
        return userPrivilegeFacade;
    }

    public List<Area> getDistrictsAvailableForSelection() {
        return districtsAvailableForSelection;
    }

    public void setDistrictsAvailableForSelection(List<Area> districtsAvailableForSelection) {
        this.districtsAvailableForSelection = districtsAvailableForSelection;
    }

    public List<UserPrivilege> getLoggedUserPrivileges() {
        return loggedUserPrivileges;
    }

    public void setLoggedUserPrivileges(List<UserPrivilege> loggedUserPrivileges) {
        this.loggedUserPrivileges = loggedUserPrivileges;
    }

    public List<Institution> getLoggableInstitutions() {
        if (loggableInstitutions == null) {
            loggableInstitutions = findAutherizedInstitutions();
        }
        return loggableInstitutions;
    }

    public void setLoggableInstitutions(List<Institution> loggableInstitutions) {
        this.loggableInstitutions = loggableInstitutions;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public int getReportTabIndex() {
        return reportTabIndex;
    }

    public List<WebUser> getAddedUsers() {
        return addedUsers;
    }

    public void setReportTabIndex(int reportTabIndex) {
        this.reportTabIndex = reportTabIndex;
    }

    public WebUserRole getAssumedRole() {
        return assumedRole;
    }

    public void setAssumedRole(WebUserRole assumedRole) {
        this.assumedRole = assumedRole;
    }

    public Institution getAssumedInstitution() {
        return assumedInstitution;
    }

    public void setAssumedInstitution(Institution assumedInstitution) {
        this.assumedInstitution = assumedInstitution;
    }

    public Area getAssumedArea() {
        return assumedArea;
    }

    public void setAssumedArea(Area assumedArea) {
        this.assumedArea = assumedArea;
    }

    public List<UserPrivilege> getAssumedPrivileges() {
        return assumedPrivileges;
    }

    public void setAssumedPrivileges(List<UserPrivilege> assumedPrivileges) {
        this.assumedPrivileges = assumedPrivileges;
    }

    private List<UserPrivilege> generateAssumedPrivileges(WebUser wu, List<Privilege> ps) {
        List<UserPrivilege> ups = new ArrayList<>();
        for (Privilege p : ps) {
            UserPrivilege up = new UserPrivilege();
            up.setPrivilege(p);
            up.setWebUser(wu);
            ups.add(up);
        }
        return ups;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    private UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public int getIndicatorTabIndex() {
        return indicatorTabIndex;
    }

    public void setIndicatorTabIndex(int indicatorTabIndex) {
        this.indicatorTabIndex = indicatorTabIndex;
    }

    public int getMetadataTabIndex() {
        return metadataTabIndex;
    }

    public void setMetadataTabIndex(int metadataTabIndex) {
        this.metadataTabIndex = metadataTabIndex;
    }

    public List<WebUser> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<WebUser> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public TreeNode[] getSelectedNodeSet() {
        return selectedNodeSet;
    }

    public void setSelectedNodeSet(TreeNode[] selectedNodeSet) {
        this.selectedNodeSet = selectedNodeSet;
    }

    public List<WebUser> getManagableUsers() {
        return managableUsers;
    }

    public void setManagableUsers(List<WebUser> managableUsers) {
        this.managableUsers = managableUsers;
    }

    public WebUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(WebUserRole userRole) {
        this.userRole = userRole;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getBulkText() {
        return bulkText;
    }

    public void setBulkText(String bulkText) {
        this.bulkText = bulkText;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public Institution getLoggedInstitution() {
        if (loggedInstitution == null) {
            if (getLoggedUser() != null) {
                loggedInstitution = getLoggedUser().getInstitution();
            }
        }
        return loggedInstitution;
    }

    public void setLoggedInstitution(Institution loggedInstitution) {
        this.loggedInstitution = loggedInstitution;
    }

    public void markAllUsersPublic() {
        for (WebUser u : webUserApplicationController.getItems()) {
            u.setPubliclyListed(true);
            save(u);
        }
    }

    public List<Vehicle> getManagableVehicles() {
        if(managableVehicles==null){
            managableVehicles = new ArrayList<>();
        }
        return managableVehicles;
    }

    public void setManagableVehicles(List<Vehicle> managableVehicles) {
        this.managableVehicles = managableVehicles;
    }

    public List<Driver> getManagableDrivers() {
        return managableDrivers;
    }

    public void setManagableDrivers(List<Driver> managableDrivers) {
        this.managableDrivers = managableDrivers;
    }

    @FacesConverter(forClass = WebUser.class)
    public static class WebUserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            WebUserController controller = (WebUserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "webUserController");
            return controller.getWebUser(getKey(value));
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
            if (object instanceof WebUser) {
                WebUser o = (WebUser) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + WebUser.class.getName());
            }
        }

    }

    @FacesConverter("webUserConverter")
    public static class WebUserConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            WebUserController controller = (WebUserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "webUserController");
            return controller.getWebUser(getKey(value));
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
            if (object instanceof WebUser) {
                WebUser o = (WebUser) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + WebUser.class.getName());
            }
        }

    }

}
