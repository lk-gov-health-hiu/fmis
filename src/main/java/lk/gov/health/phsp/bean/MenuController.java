/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.FuelTransactionHistory;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.enums.DocumentGenerationType;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.enums.Privilege;
import static lk.gov.health.phsp.enums.WebUserRoleLevel.CTB;
import static lk.gov.health.phsp.enums.WebUserRoleLevel.FUEL_REQUESTING_INSTITUTION;
import static lk.gov.health.phsp.enums.WebUserRoleLevel.HEALTH_MINISTRY;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class MenuController implements Serializable {

    @Inject
    private WebUserController webUserController;
    @Inject
    WebUserApplicationController webUserApplicationController;
    @Inject
    FuelRequestAndIssueController letterController;

    @Inject
    InstitutionController institutionController;
    @Inject
    VehicleController vehicleController;
    @Inject
    DriverController driverController;
    @Inject
    PreferenceController preferenceController;
    @Inject
    DashboardController dashboardController;

    /**
     * Creates a new instance of MenuController
     */
    public MenuController() {
    }

    public String toIndex() {
        dashboardController.preparePersonalDashboard();
        return "/index";
    }

    public String toReportsIndex() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/reports_index";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/reports_index";
            default:
                return "";
        }
    }

    public String toSearch() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/search";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/search";
            default:
                return "";
        }
    }

    public String toAdministrationIndex() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
                return "/national/admin/index";
            case CTB:
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/index";
            default:
                return "";
        }
    }

    public String toAdministrationIndexFirstLogin() {
        return "/national/admin/index";
    }

    public String toPreferences() {
        preferenceController.preparePreferences();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/regional/institution/preferences";
            case HEALTH_MINISTRY:
                return "/national/admin/preferences";
            default:
                return "";
        }
    }

    public String toAddNewUser() {
        webUserController.prepareToAddNewUser();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/user_new";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/user_new";
            default:
                return "";
        }
    }

    public String toAddMultipleUsers() {
        webUserController.prepareToAddNewUser();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/add_multiple_users";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/add_multiple_users";
            default:
                return "";
        }
    }

    public String toAddNewInstitution() {
        institutionController.prepareToAddNewInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/institution";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/institution";
            default:
                return "";
        }
    }

    public String toAddNewVehicle() {
        vehicleController.prepareToAddNewVehicle();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/vehicle";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/vehicle";
            default:
                return "";
        }
    }

    public String toAddNewDriver() {
        driverController.prepareToAddNewDriver();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/driver";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/driver";
            default:
                return "";
        }
    }

    public String toAddMultipleNewInstitutions() {
        institutionController.prepareToAddNewInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/add_multiple_institutions_with_a_user";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/add_multiple_institutions_with_a_user";
            default:
                return "";
        }
    }

    public String toAddNewInstitutionAtLetterEntry() {
        institutionController.prepareToAddNewInstitution();
        return "/institution/institution_at_letter_entry";
    }

    public String toListUsersFirstLogin() {
        webUserController.prepareListingAllUsers();
        return "/national/admin/user_list";
    }

    public String toListUsers() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                webUserController.findManagableUsers();
                return "/institution/admin/user_list";
            case HEALTH_MINISTRY:
                webUserController.fillAllUsers();
                return "/national/admin/user_list";
            default:
                return "";
        }
    }

    public String toListInstitutions() {
        institutionController.prepareToListInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case CTB:
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/institution_list";
            case HEALTH_MINISTRY:
                return "/national/admin/institution_list";
            default:
                return "";
        }
    }

    public String toListVehicles() {
        vehicleController.prepareToListVehicle();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case CTB:
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/vehicle_list";
            case HEALTH_MINISTRY:
                return "/national/admin/vehicle_list";
            default:
                return "";
        }
    }

    public String toEditVehicle() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case CTB:
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/vehicle";
            case HEALTH_MINISTRY:
                return "/national/admin/vehicle";
            default:
                return "";
        }
    }

    public String toListDriverss() {
        driverController.prepareToListDriver();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case CTB:
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/driver_list";
            case HEALTH_MINISTRY:
                return "/national/admin/driver_list";
            default:
                return "";
        }
    }

    public String toListInstitutionsWithUsers() {

        institutionController.prepareToListInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/institution/admin/institution_list_with_users";
            case HEALTH_MINISTRY:
                return "/national/admin/institution_list_with_users";
            default:
                return "";
        }
    }

    public String toPrivileges() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/institution/admin/privileges";
            case HEALTH_MINISTRY:
                return "/national/admin/privileges";
            default:
                return "";
        }
    }

    public String toPrivilegesFirstLogin() {
        return "/national/admin/privileges";
    }

    public String toEditUser() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/institution/admin/user_edit";
            case HEALTH_MINISTRY:
                return "/national/admin/user_edit";
            default:
                return "";
        }
    }

    public String toEditInstitution() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/institution/admin/institution";
            case HEALTH_MINISTRY:
                return "/national/admin/institution";
            default:
                return "";
        }
    }

    public String toEditDriver() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/institution/admin/driver";
            case HEALTH_MINISTRY:
                return "/national/admin/driver";
            default:
                return "";
        }
    }

    public String toEditPassword() {
        webUserController.prepareEditPassword();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
            case CTB:
                return "/institution/admin/user_password";
            case HEALTH_MINISTRY:
                return "/national/admin/user_password";
            default:
                return "";
        }
    }

    public String toUserPrivileges() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }

        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case FUEL_REQUESTING_INSTITUTION:
                return "/regional/admin/user_privileges";
            case HEALTH_MINISTRY:
                return "/national/admin/user_privileges";
            default:
                return "";
        }
    }

}
