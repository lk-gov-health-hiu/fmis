package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.facade.PreferenceFacade;

@Named
@SessionScoped
public class PreferenceController implements Serializable {

    @EJB
    private PreferenceFacade ejbFacade;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    private UserTransactionController userTransactionController;

    /*
    Application Preferences
     */
    private String totalFuelInLeters;
    private Long totalFuelInLitersLong;

    /*
    Institution Preferences
     */
    private String labReportHtml;

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    private PreferenceFacade getFacade() {
        return ejbFacade;
    }

    public String findPreferanceValue(String name) {
        Preference p = findPreferance(name);
        if (p != null) {
            return p.getLongTextValue();
        } else {
            return "";
        }
    }

    public String findPreferanceValue(String name, Institution ins) {
        Preference p = findPreferance(name, ins);
        if (p != null) {
            if (p.getLongTextValue() != null) {
                return p.getLongTextValue();
            } else {
                return "";
            }

        } else {
            return "";
        }
    }

    public String findPreferanceValue(String name, Institution ins, String defaultValue) {
        Preference p = findPreferance(name, ins);
        if (p != null) {
            return p.getLongTextValue();
        } else {
            return defaultValue;
        }
    }

    public String toManagePreferences() {
        loadPreferences();
        return "/systemAdmin/preferences";
    }

    public String toManagePreferencesInstitution() {
        loadPreferencesInstitution();
        return "/insAdmin/preferences";
    }

    public void preparePreferences() {
        loadPreferencesInstitution();
    }

    public void loadPreferences() {
        System.out.println("loadPreferences");
        totalFuelInLeters = findPreferanceValue("totalFuelInLeters");
        System.out.println("totalFuelInLeters = " + totalFuelInLeters);
        if (totalFuelInLeters == null || totalFuelInLeters.isEmpty() || totalFuelInLeters.isBlank()) {
            totalFuelInLeters = "440000";
        }
        System.out.println("totalFuelInLeters = " + totalFuelInLeters);
        try {
            totalFuelInLitersLong = Long.valueOf(getTotalFuelInLeters());
        } catch (Exception e) {
            totalFuelInLitersLong = 440000l;
        }
        System.out.println("totalFuelInLitersLong = " + totalFuelInLitersLong);
        if (totalFuelInLitersLong == null || totalFuelInLitersLong == 0l) {
            totalFuelInLitersLong = 440000l;
        }
        System.out.println("totalFuelInLitersLong = " + totalFuelInLitersLong);
    }

    public void loadPreferencesInstitution() {
        labReportHtml = findPreferanceValue("labReportHeader", webUserController.getLoggedInstitution());
        if (labReportHtml == null) {
            labReportHtml = "";
        }
    }

    public void savePreferences() {
        savePreference("totalFuelInLeters", totalFuelInLeters);
    }

    public void savePreferencesInstitution() {
        savePreference("labReportHeader", webUserController.getLoggedInstitution(), labReportHtml);
    }

    public Preference findPreferance(String name) {
        if (name == null) {
            return null;
        }
        String j = "select p "
                + " from Preference p "
                + " where p.applicationPreferance=:ap "
                + " and p.name=:n";
        Map m = new HashMap();
        m.put("ap", true);
        m.put("n", name);
        Preference p = getFacade().findFirstByJpql(j, m);
        if (p == null) {
            p = new Preference();
            p.setApplicationPreferance(true);
            p.setName(name);
            savePreference(p);
        }
        return p;
    }

    public Preference findPreferance(String name, Institution ins) {
        if (name == null) {
            return null;
        }
        if (ins == null) {
            return null;
        }
        String j = "select p "
                + " from Preference p "
                + " where p.applicationPreferance=:ap "
                + " and p.name=:n "
                + " and p.institution=:ins ";
        Map m = new HashMap();
        m.put("ap", false);
        m.put("n", name);
        m.put("ins", ins);
        Preference p = getFacade().findFirstByJpql(j, m);
        if (p == null) {
            p = new Preference();
            p.setApplicationPreferance(false);
            p.setInstitution(ins);
            p.setName(name);
            savePreference(p);
        }
        return p;
    }

    public void savePreference(String name, String value) {
        Preference p = findPreferance(name);
        if (p != null) {
            p.setLongTextValue(value);
            savePreference(p);
        }
    }

    public void savePreference(String name, Institution ins, String value) {
        Preference p = findPreferance(name, ins);
        if (p != null) {
            p.setLongTextValue(value);
            savePreference(p);
        }
    }

    public void savePreference(Preference p) {
        if (p == null) {
            return;
        }
        if (p.getId() == null) {
            p.setCreatedAt(new Date());
            p.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(p);
        } else {
            p.setLastEditBy(webUserController.getLoggedUser());
            p.setLastEditeAt(new Date());
            getFacade().edit(p);
        }
    }

    public String getTotalFuelInLeters() {
        if (totalFuelInLeters == null) {
            loadPreferences();
        }
        return totalFuelInLeters;
    }

    public void setTotalFuelInLeters(String totalFuelInLeters) {
        this.totalFuelInLeters = totalFuelInLeters;
    }

    public Long getTotalFuelInLitersLong() {
        if (totalFuelInLitersLong == null) {
            loadPreferences();
        }
        return totalFuelInLitersLong;
    }

    public void setTotalFuelInLitersLong(Long totalFuelInLitersLong) {
        this.totalFuelInLitersLong = totalFuelInLitersLong;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public String getLabReportHtml() {
        if (labReportHtml == null) {
            loadPreferencesInstitution();
        }
        return labReportHtml;
    }

    public void setLabReportHtml(String labReportHtml) {
        this.labReportHtml = labReportHtml;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Preference.class)
    public static class PreferenceControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PreferenceController controller = (PreferenceController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "preferenceController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException e) {
                key = 0l;
            }
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
            if (object instanceof Preference) {
                Preference o = (Preference) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Preference.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
