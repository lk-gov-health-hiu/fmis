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
import lk.gov.health.phsp.enums.InstitutionCategory;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
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
    InstitutionController institutionController;
    @Inject
    VehicleController vehicleController;
    @Inject
    DriverController driverController;
    @Inject
    PreferenceController preferenceController;
    @Inject
    DashboardController dashboardController;
    @Inject
    AreaController areaController;

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
                return "/national/reports_index";
            case CTB:
                return "/sltb/reports/index";
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
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();
        if (roleLevel == WebUserRoleLevel.HEALTH_MINISTRY) {
            return "/national/admin/index";
        } else if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
            return "/cpc/admin/index";
        } else {
            return "/institution/admin/index";
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
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();
        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/user_new";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/user_new";
                } else {
                    return "/institution/admin/user_new";
                }
        }
    }

    public String toAddNewUserDataEntry() {
        webUserController.prepareToAddNewUser();
        return "/dataentry/user_new";
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
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/institution";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/institution";
                } else {
                    return "/institution/admin/institution";
                }
        }
    }

    public String toAddNewHealthInstitution() {
        institutionController.prepareToAddNewInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
                return "/national/admin/institution_health";
            default:
                return "/institution/admin/institution_health";
        }
    }

    public String toAddNewFuelStation() {
        institutionController.prepareToAddNewFuelStation();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/fuel_station";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/fuel_station";
                } else {
                    return "/institution/admin/fuel_station";
                }
        }
    }

    public String toAddNewVehicle() {
        vehicleController.prepareToAddNewVehicle();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/vehicle";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/vehicle";
                } else {
                    return "/institution/admin/vehicle";
                }
        }
    }

    public String toAddNewDriver() {
        driverController.prepareToAddNewDriver();
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

    public String toAddNewArea() {
        areaController.prepareCreate();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case HEALTH_MINISTRY:
            case CTB:
                return "/national/admin/area";
            case FUEL_REQUESTING_INSTITUTION:
                return "/institution/admin/area";
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
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                webUserController.fillAllUsers();
                return "/national/admin/user_list";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/user_list";
                } else {
                    return "/institution/admin/user_list";
                }
        }
    }

    public String toListUsersDataentry() {
        webUserController.fillAllUsers();
        return "/dataentry/user_list";
    }

    public String toListInstitutions() {
        institutionController.prepareToListInstitution();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/institution_list";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/institution_list";
                } else {
                    return "/institution/admin/institution_list";
                }
        }
    }

    public String toListInstitutionsHealth() {
        institutionController.prepareToListInstitution();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();
        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/institution_list_health";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/institution_list_health";
                } else {
                    return "/institution/admin/institution_list_health";
                }
        }
    }

    public String toListFuelStations() {
        institutionController.prepareToListFuelStations();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/fuel_station_list";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/fuel_station_list";
                } else {
                    return "/institution/admin/fuel_station_list";
                }
        }
    }

    public String toListVehicles() {
        vehicleController.prepareToListVehicle();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/vehicle_list";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/vehicle_list";
                } else {
                    return "/institution/admin/vehicle_list";
                }
        }
    }

    public String toEditVehicle() {
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/vehicle";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/vehicle";
                } else {
                    return "/institution/admin/vehicle";
                }
        }
    }

    public String toListDriverss() {
        driverController.prepareToListDriver();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();
        System.out.println("institutionCategory = " + institutionCategory);
        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/driver_list";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/driver_list";
                } else {
                    return "/institution/admin/driver_list";
                }
        }
    }

    public String toListAreas() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case CTB:
            case HEALTH_MINISTRY:
                return "/national/admin/area_list";
            default:
                return "/institution/admin/area_list";
        }
    }

    public String toAddNewInstitutionDataEntry() {
        institutionController.prepareToAddNewInstitution();
        return "/dataentry/institution";
    }

    public String toDataEntry() {
        return "/dataentry/index";
    }

    public String toListInstitutionsDataEntry() {
        return "/dataentry/institution_list";
    }

    public String toAddNewVehicleDataEntry() {
        driverController.prepareToAddNewDriver();
        return "/dataentry/vehicle";
    }

    public String toListVehiclesDataEntry() {
        return "/dataentry/vehicle_list";
    }

    public String toAddNewDriverDataEntry() {
        vehicleController.prepareToAddNewVehicle();
        return "/dataentry/driver";
    }

    public String toListDriversDataEntry() {
        return "/dataentry/driver_list";
    }

    public String toEditUserDataEntry() {
        return "/dataentry/user_edit";
    }

    public String toEditPasswordDataEntry() {
        return "/dataentry/user_password";
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
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/user_edit";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/user_edit";
                } else {
                    return "/institution/admin/user_edit";
                }
        }
    }

    public String toEditInstitution() {
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/institution_health";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/institution_health";
                } else {
                    return "/institution/admin/institution_health";
                }
        }
    }

    public String toEditMyInstitution() {
        institutionController.setSelected(webUserController.getLoggedInstitution());
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/institution";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/my_institution";
                } else {
                    return "/institution/admin/my_institution";
                }
        }
    }

    public String toEditFuelStation() {
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();
        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/fuel_station";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/fuel_station";
                } else {
                    return "/institution/admin/fuel_station";
                }
        }
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

    public String toEditPassword() {
        webUserController.prepareEditPassword();
        WebUserRoleLevel roleLevel = webUserController.getLoggedUser().getWebUserRoleLevel();
        InstitutionCategory institutionCategory = webUserController.getLoggedUser().getInstitution().getInstitutionType().getCategory();

        switch (roleLevel) {
            case HEALTH_MINISTRY:
                return "/national/admin/user_password";
            default:
                if (institutionCategory == InstitutionCategory.CPC || institutionCategory == InstitutionCategory.CPC_HEAD_OFFICE) {
                    return "/cpc/admin/user_password";
                } else {
                    return "/institution/admin/user_password";
                }
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
