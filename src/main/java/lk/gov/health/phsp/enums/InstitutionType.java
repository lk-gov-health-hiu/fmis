package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum InstitutionType {
    Ministry_of_Health("Ministry of Health"),
    Other_Ministry("Other Ministry"),
    Provincial_General_Hospital("Provincial General Hospital"),
    Provincial_Department_of_Health_Services("Provincial Department of Health Services"),
    Regional_Department_of_Health_Department("Regional Department of Health Department"),
    Hospital("Hospital"),
    National_Hospital("National Hospital"),
    Teaching_Hospital("Teaching Hospital"),
    District_General_Hospital("District General Hospital"),
    Base_Hospital("Base Hospital"),
    Divisional_Hospital("Divisional Hospital"),
    Primary_Medical_Care_Unit("Primary Medical Care Unit"),
    MOH_Office("MOH Office"),
    OtherSpecializedUnit("Other Specialized Units"),
    CTB_Head_Office("CTB Headoffice"),
    CTB_Depot("CTB Depot"),
    Donar("Donar"),
    DistrictSecretariat("District Secretariat"),
    Audit("Unit"),
    Police_Station("Police Station"),
    Police_Department("Police Department"),
    CPC_Head_Office("CPC Headoffice"),
    CPC_Depot("CPC Depot"),
    ERD("ERD"),
    CB("CB"),
    Other("Ward");

    private final String label;

    private InstitutionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
