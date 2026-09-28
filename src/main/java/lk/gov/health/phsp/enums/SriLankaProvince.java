package lk.gov.health.phsp.enums;

import java.util.Arrays;
import java.util.List;

/**
 * The nine provinces of Sri Lanka and the districts in each. Used to derive an
 * institution's province from its district, since province data is not kept
 * in the Area table.
 */
public enum SriLankaProvince {
    Western("Western", "WP", "Colombo", "Gampaha", "Kalutara"),
    Central("Central", "CP", "Kandy", "Matale", "Nuwara Eliya"),
    Southern("Southern", "SP", "Galle", "Matara", "Hambantota"),
    Northern("Northern", "NP", "Jaffna", "Kilinochchi", "Mannar", "Mullaitivu", "Mullaitive", "Vavuniya"),
    Eastern("Eastern", "EP", "Ampara", "Batticaloa", "Trincomalee"),
    NorthWestern("North Western", "NW", "Kurunegala", "Puttalam"),
    NorthCentral("North Central", "NC", "Anuradhapura", "Polonnaruwa"),
    Uva("Uva", "UV", "Badulla", "Monaragala", "Moneragala"),
    Sabaragamuwa("Sabaragamuwa", "SB", "Ratnapura", "Kegalle");

    private final String label;
    // Prefix of the CPC bill acceptance number, e.g. WP/000123
    private final String code;
    private final List<String> districts;

    private SriLankaProvince(String label, String code, String... districts) {
        this.label = label;
        this.code = code;
        this.districts = Arrays.asList(districts);
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return code;
    }

    // Finds the province whose label appears in a name such as "CPC North Western"
    // or "Ceypetco Western Province". Longer labels are tried first so that
    // "North Western" is not mistaken for "Western".
    public static SriLankaProvince fromName(String name) {
        if (name == null) {
            return null;
        }
        String n = name.toLowerCase();
        SriLankaProvince best = null;
        for (SriLankaProvince p : values()) {
            if (n.contains(p.label.toLowerCase())
                    && (best == null || p.label.length() > best.label.length())) {
                best = p;
            }
        }
        return best;
    }

    // Matches ignoring case and surrounding whitespace (some district names are
    // stored with stray tabs). Returns null if the district is not recognised.
    public static SriLankaProvince fromDistrictName(String districtName) {
        if (districtName == null) {
            return null;
        }
        String name = districtName.trim();
        for (SriLankaProvince p : values()) {
            for (String d : p.districts) {
                if (d.equalsIgnoreCase(name)) {
                    return p;
                }
            }
        }
        return null;
    }
}
