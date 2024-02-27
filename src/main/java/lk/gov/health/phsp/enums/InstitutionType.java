package lk.gov.health.phsp.enums;

/**
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
    Fuel_Station("Fuel Station"),
    CTB_Country_Office("SLTB Head Office"),
    CTB_Head_Office("SLTB Regional Office"),
    CTB_Depot("SLTB Depot"),
    Donar("Donar"),
    DistrictSecretariat("District Secretariat"),
    Audit("Unit"),
    Police_Station("Police Station"),
    Police_Department("Police Department"),
    CPC_Head_Office("CPC Headoffice"),
    CPC_Depot("CPC Depot"),
    ERD("ERD"),
    CB("CB"),
    Indigenous_Medicine_Department("Indigenous Medicine Department"),
    Ayurvedic_Department("Ayurvedic Department"),
    Ayurvedic_Hospital("Ayurvedic Hospital"),
    Provincial_Ayurvedic_Department("Provincial Ayurvedic Department"),
    District_Ayurvedic_Department("District Ayurvedic Department"),
    Herbal_Guardian("Herbal Guardian"),
    Other("Other");

    private final String label;

    private InstitutionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
