package lk.gov.health.phsp.enums;

/**
 * @author Dr M H B Ariyaratne
 */
public enum OwnershipType {
    CPC_OWNED("CPC Owned"),
    COMPANY_OWNED("Dealer Owned"),
    COOPERATE_CUSTOMER("Cooperate Customer");

    private final String label;


    private OwnershipType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    
}
