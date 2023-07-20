/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import lk.gov.health.phsp.enums.WebUserRole;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.pojcs.Nameable;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Entity
public class WebUser implements Serializable, Nameable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String webUserPassword;
    @OneToOne(cascade = CascadeType.ALL)
    Person person;
    //Main Properties
    @Column(length = 50, nullable = false, unique = true)
    String name;
    private boolean publiclyListed;
    String description;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    //Activation properties
    boolean activated;
    @ManyToOne
    WebUser activator;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date activatedAt;
    String activateComments;
    @Enumerated(EnumType.STRING)
    WebUserRole webUserRole;
    @Transient
    private WebUserRoleLevel webUserRoleLevel;
    String primeTheme;
    String defLocale;
    String email;
    String telNo;
    @ManyToOne
    Institution institution;
    @ManyToOne
    private Area area;

    private String loginIPs;

    String code;

    @Transient
    private String webUserPersonName;
    @Transient
    String displayName;

    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;

    @Transient
    private boolean systemAdministrator;
    @Transient
    private boolean superUser;
    @Transient
    private boolean user;
    @Transient
    private boolean institutionUser;
    @Transient
    private boolean institutionSuperUser;
    @Transient
    private boolean institutionAdministrator;
    @Transient
    private boolean authorityUser;
    @Transient
    private WebUserRole assumedRole;
    @Transient
    private Institution assumedInstitution;
    @Transient
    private Area assumedArea;
    @Transient
    private boolean restrictedToInstitution;
    @Transient
    private boolean currentlyInAssumedState;
    @Transient
    private String insName;

    public WebUser() {
    }

    public Institution getInstitution() {
        if (getAssumedInstitution() != null) {
            return assumedInstitution;
        }
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getDefLocale() {
        return defLocale;
    }

    public void setDefLocale(String defLocale) {
        this.defLocale = defLocale;
    }

    public String getPrimeTheme() {
        return primeTheme;
    }

    public void setPrimeTheme(String primeTheme) {
        this.primeTheme = primeTheme;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWebUserPassword() {
        return webUserPassword;
    }

    public void setWebUserPassword(String webUserPassword) {
        this.webUserPassword = webUserPassword;
    }

    public Person getPerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getActivateComments() {
        return activateComments;
    }

    public void setActivateComments(String activateComments) {
        this.activateComments = activateComments;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }

    public WebUser getActivator() {
        return activator;
    }

    public void setActivator(WebUser activator) {
        this.activator = activator;
    }

    public WebUserRole getWebUserRole() {
        if (assumedRole != null) {
            return assumedRole;
        }
        if (webUserRole == null) {
            webUserRole = WebUserRole.SYSTEM_ADMINISTRATOR;
        }
        return webUserRole;
    }

    public void setWebUserRole(WebUserRole webUserRole) {
        this.webUserRole = webUserRole;
    }

   
    public Boolean getInstitute() {
        return false;
    }

   
    public Boolean getWebUser() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WebUser)) {
            return false;
        }
        WebUser other = (WebUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (id != null) {
            return id.toString();
        } else {
            return null;
        }
    }

    public String getVehiclesModel() {
        return code;
    }

    public void setVehiclesModel(String code) {
        this.code = code;
    }

    public boolean isSystemAdministrator() {
        systemAdministrator = getWebUserRole() == WebUserRole.SYSTEM_ADMINISTRATOR;
        return systemAdministrator;
    }

    public boolean isSuperUser() {
        superUser = getWebUserRole() == WebUserRole.SUPER_USER;
        return superUser;
    }

    public boolean isUser() {
        user = getWebUserRole() == WebUserRole.USER;
        return user;
    }

    public boolean isInstitutionUser() {
        institutionUser = getWebUserRole() == WebUserRole.INSTITUTION_USER;
        return institutionUser;
    }

    public boolean isInstitutionAdministrator() {
        institutionAdministrator = getWebUserRole() == WebUserRole.INSTITUTION_ADMINISTRATOR;
        return institutionAdministrator;
    }

    public boolean isAuthorityUser() {
        authorityUser = getWebUserRole() == WebUserRole.USER;
        return authorityUser;
    }

    public Area getArea() {
        if (assumedArea != null) {
            return assumedArea;
        }
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public boolean isInstitutionSuperUser() {
        if (getWebUserRole() == WebUserRole.INSTITUTION_SUPER_USER) {
            institutionSuperUser = true;
        } else {
            institutionSuperUser = false;
        }
        return institutionSuperUser;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
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

    public boolean isRestrictedToInstitution() {
        restrictedToInstitution = true;
        WebUserRole ur;
        if (currentlyInAssumedState) {
            ur = assumedRole;
        } else {
            ur = this.getWebUserRole();
        }
        if (ur == null) {
            return restrictedToInstitution;
        }
        switch (ur) {
            case SYSTEM_ADMINISTRATOR:
            case USER:
            case SUPER_USER:
                restrictedToInstitution = false;
                break;
            default:
                restrictedToInstitution = true;
        }
        return restrictedToInstitution;
    }

    public boolean isCurrentlyInAssumedState() {
        return currentlyInAssumedState;
    }

    public void setCurrentlyInAssumedState(boolean currentlyInAssumedState) {
        this.currentlyInAssumedState = currentlyInAssumedState;
    }

    public WebUserRoleLevel getWebUserRoleLevel() {
        if (getWebUserRole() == null) {
            return webUserRoleLevel = null;
        } else {
            switch (getWebUserRole()) {
                case SYSTEM_ADMINISTRATOR:
                case SUPER_USER:
                case USER:
                    webUserRoleLevel = WebUserRoleLevel.NATIONAL;
                    break;
                case INSTITUTION_ADMINISTRATOR:
                case INSTITUTION_SUPER_USER:
                case INSTITUTION_USER:
                    webUserRoleLevel = WebUserRoleLevel.HEALTH_INSTITUTION;
                    break;
                    
            }
        }
        return webUserRoleLevel;
    }

    public String getLoginIPs() {
        return loginIPs;
    }

    public void setLoginIPs(String loginIPs) {
        this.loginIPs = loginIPs;
    }

   
    public String getAddress() {
        if (person != null) {
            return person.getAddress();
        } else {
            return null;
        }
    }

   
    public void setAddress(String address) {
        if (person != null) {
            person.setAddress(address);
        }
    }

   
    public String getFax() {
        if (person != null) {
            return person.getFax();
        } else {
            return null;
        }
    }

   
    public void setFax(String fax) {
        if (person != null) {
            person.setFax(fax);
        }
    }

   
    public String getPhone() {
        if (person != null) {
            return person.getPhone2();
        } else {
            return null;
        }
    }

   
    public void setPhone(String phone) {
        if (person != null) {
            person.setPhone2(phone);
        }
    }

  
    public String getMobile() {
        if (person != null) {
            return person.getPhone1();
        } else {
            return null;
        }
    }

  
    public void setMobile(String mobile) {
        if (person != null) {
            person.setPhone1(mobile);
        }
    }

   
    public String getVehicleNumber() {
        if (person != null) {
            return person.getTname();
        } else {
            return null;
        }
    }

    
    public void setVehicleNumber(String tname) {
        if (person != null) {
            person.setTname(tname);
        }
    }

   
    public String getVehicleMake() {
        if (person != null) {
            return person.getSname();
        } else {
            return null;
        }
    }

  
    public void setVehicleMake(String sname) {
        if (person != null) {
            person.setSname(sname);
        }
    }

    public String getWebUserPersonName() {
        if (this.person == null) {
            webUserPersonName = "";
        } else {
            webUserPersonName = this.person.getName();
        }
        return webUserPersonName;
    }

  
    public String getDisplayName() {
        if (this.person == null) {
            displayName = "";
        } else {
            displayName = this.person.getName();
        }
        return displayName;
    }

    public boolean isPubliclyListed() {
        return publiclyListed;
    }

    public void setPubliclyListed(boolean publiclyListed) {
        this.publiclyListed = publiclyListed;
    }

   
    public String getInsName() {
        if (institution != null) {
            if (institution.getDisplayName() != null) {
                insName = institution.getDisplayName();
            } else if (institution.getName() != null) {
                insName = institution.getName();
            }
        }
        return insName;
    }

   
    public void setInsName(String insName) {
        this.insName = insName;
    }

}
