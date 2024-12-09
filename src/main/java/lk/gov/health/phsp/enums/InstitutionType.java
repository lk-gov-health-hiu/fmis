package lk.gov.health.phsp.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dr M H B Ariyaratne
 */
public enum InstitutionType {
    Ministry_of_Health("Ministry of Health", InstitutionCategory.FUEL_RECEIVER),
    Other_Ministry("Other Ministry", InstitutionCategory.OTHER),
    Provincial_General_Hospital("Provincial General Hospital", InstitutionCategory.FUEL_RECEIVER),
    Provincial_Department_of_Health_Services("Provincial Department of Health Services", InstitutionCategory.FUEL_RECEIVER),
    Regional_Department_of_Health_Department("Regional Department of Health Department", InstitutionCategory.FUEL_RECEIVER),
    Hospital("Hospital", InstitutionCategory.FUEL_RECEIVER),
    National_Hospital("National Hospital", InstitutionCategory.FUEL_RECEIVER),
    Teaching_Hospital("Teaching Hospital", InstitutionCategory.FUEL_RECEIVER),
    District_General_Hospital("District General Hospital", InstitutionCategory.FUEL_RECEIVER),
    Base_Hospital("Base Hospital", InstitutionCategory.FUEL_RECEIVER),
    Divisional_Hospital("Divisional Hospital", InstitutionCategory.FUEL_RECEIVER),
    Primary_Medical_Care_Unit("Primary Medical Care Unit", InstitutionCategory.FUEL_RECEIVER),
    MOH_Office("MOH Office", InstitutionCategory.FUEL_RECEIVER),
    OtherSpecializedUnit("Other Specialized Units", InstitutionCategory.OTHER),
    Fuel_Station("Fuel Station", InstitutionCategory.CPC),
    CTB_Country_Office("SLTB Head Office", InstitutionCategory.CPC),
    CTB_Head_Office("SLTB Regional Office", InstitutionCategory.CPC),
    CTB_Depot("SLTB Depot", InstitutionCategory.CPC),
    Donar("Donar", InstitutionCategory.MONITORING_AND_EVALUATION),
    DistrictSecretariat("District Secretariat", InstitutionCategory.MONITORING_AND_EVALUATION),
    Audit("Unit", InstitutionCategory.MONITORING_AND_EVALUATION),
    Police_Station("Police Station", InstitutionCategory.OTHER),
    Police_Department("Police Department", InstitutionCategory.OTHER),
    CPC_Head_Office("CPC Headoffice", InstitutionCategory.CPC_HEAD_OFFICE),
    CPC_Provincial_Office("CPC Provincial Office", InstitutionCategory.CPC),
    CPC_Depot("CPC Depot", InstitutionCategory.CPC),
    ERD("ERD", InstitutionCategory.MONITORING_AND_EVALUATION),
    CB("CB", InstitutionCategory.MONITORING_AND_EVALUATION),
    Indigenous_Medicine_Department("Indigenous Medicine Department", InstitutionCategory.FUEL_RECEIVER),
    Ayurvedic_Department("Ayurvedic Department", InstitutionCategory.FUEL_RECEIVER),
    Ayurvedic_Hospital("Ayurvedic Hospital", InstitutionCategory.FUEL_RECEIVER),
    Provincial_Ayurvedic_Department("Provincial Ayurvedic Department", InstitutionCategory.FUEL_RECEIVER),
    District_Ayurvedic_Department("District Ayurvedic Department", InstitutionCategory.FUEL_RECEIVER),
    Herbal_Guardian("Herbal Guardian", InstitutionCategory.FUEL_RECEIVER),
    Suwa_Sariya("SUWA SERIYA",InstitutionCategory.FUEL_RECEIVER),
    Other("Other", InstitutionCategory.OTHER);

    private final String label;
    private final InstitutionCategory category;

    private InstitutionType(String label, InstitutionCategory category) {
        this.label = label;
        this.category = category;
    }

    public String getLabel() {
        return label;
    }

    public InstitutionCategory getCategory() {
        return category;
    }
    
    public static List<InstitutionType> getHealthcareInstitutionTypes() {
        return Arrays.stream(InstitutionType.values())
                .filter(instType -> instType == InstitutionType.Ministry_of_Health
                        || instType == InstitutionType.Hospital
                        || instType == InstitutionType.National_Hospital
                        || instType == InstitutionType.Teaching_Hospital
                        || instType == InstitutionType.District_General_Hospital
                        || instType == InstitutionType.Base_Hospital
                        || instType == InstitutionType.Divisional_Hospital
                        || instType == InstitutionType.Primary_Medical_Care_Unit
                        || instType == InstitutionType.MOH_Office
                        || instType == InstitutionType.Provincial_General_Hospital
                        || instType == InstitutionType.Provincial_Department_of_Health_Services
                        || instType == InstitutionType.Regional_Department_of_Health_Department
                        || instType == InstitutionType.Ayurvedic_Hospital
                        || instType == InstitutionType.Indigenous_Medicine_Department
                        || instType == InstitutionType.Ayurvedic_Department
                        || instType == InstitutionType.Provincial_Ayurvedic_Department
                        || instType == InstitutionType.District_Ayurvedic_Department
                         || instType == InstitutionType.OtherSpecializedUnit
                         || instType == InstitutionType.Other_Ministry
 || instType == InstitutionType.Other
                        || instType == InstitutionType.Herbal_Guardian
                        || instType == InstitutionType.Suwa_Sariya)
                .collect(Collectors.toList());
    }
}
