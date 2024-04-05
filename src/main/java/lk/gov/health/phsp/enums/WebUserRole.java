package lk.gov.health.phsp.enums;

/**
 * Web User Role Enum
 * 
 * Represents the roles assigned to web users.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum WebUserRole {
    // National Level Roles
    SYSTEM_ADMINISTRATOR("System Administrator", AccessLevel.NATIONAL),
    SUPER_USER("Super User", AccessLevel.NATIONAL),
    USER("User", AccessLevel.NATIONAL),

    // Health Institutional Level Roles
    INSTITUTION_ADMINISTRATOR("Institution Administrator", AccessLevel.INSTITUTIONAL),
    INSTITUTION_SUPER_USER("Institution Super User", AccessLevel.INSTITUTIONAL),
    INSTITUTION_TRANSPORT("Transport Branch Users", AccessLevel.INSTITUTIONAL),
    INSTITUTION_ACCOUNTS("Account Branch Users", AccessLevel.INSTITUTIONAL),
    INSTITUTION_USER("Institution User", AccessLevel.INSTITUTIONAL),

    // CTB Level
    CTB_ADMINISTRATOR("SLTB Administrator", AccessLevel.NATIONAL),
    CTB_SUPER_USER("SLTB Super User", AccessLevel.NATIONAL),
    CTB_USER("SLTB User", AccessLevel.NATIONAL),

    // CPC Level
    CPC_ADMINISTRATOR("CPC Administrator", AccessLevel.NATIONAL),
    CPC_SUPER_USER("CPC Super User", AccessLevel.NATIONAL),
    CPC_USER("CPC User", AccessLevel.INSTITUTIONAL),

    // CB Level
    CB_ADMINISTRATOR("CB Administrator", AccessLevel.NATIONAL),
    CB_SUPER_USER("CB Super User", AccessLevel.NATIONAL),
    CB_USER("CB User", AccessLevel.INSTITUTIONAL),

    // ERD Level
    ERD_ADMINISTRATOR("ERD Administrator", AccessLevel.NATIONAL),
    ERD_SUPER_USER("ERD Super User", AccessLevel.NATIONAL),
    ERD_USER("ERD User", AccessLevel.NATIONAL),

    // SUWASERIYA
    SUWASERIYA_ADMINISTRATOR("SUWASERIYA Administrator", AccessLevel.NATIONAL),
    SUWASERIYA_SUPER_USER("SUWASERIYA Super User", AccessLevel.NATIONAL),
    SUWASERIYA_USER("SUWASERIYA User", AccessLevel.NATIONAL),

    // Donor Level
    DONOR_ADMINISTRATOR("Donor Administrator", AccessLevel.NATIONAL),
    DONOR_SUPER_USER("Donor Super User", AccessLevel.NATIONAL),
    DONOR_USER("Donor User", AccessLevel.NATIONAL),

    // Auditor Level
    AUDITOR_ADMINISTRATOR("Auditor Administrator", AccessLevel.NATIONAL),
    AUDITOR_SUPER_USER("Auditor Super User", AccessLevel.NATIONAL),
    AUDITOR_USER("Auditor User", AccessLevel.NATIONAL);

    private final String label;
    private final AccessLevel accessLevel;

    private WebUserRole(String label, AccessLevel accessLevel) {
        this.label = label;
        this.accessLevel = accessLevel;
    }

    public String getLabel() {
        return label;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }
}
