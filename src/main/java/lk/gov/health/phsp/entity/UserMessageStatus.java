/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * Entity tracking the delivery and read status of messages for individual users
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical Informatics)
 */
@Entity
@Table(name = "user_message_status")
public class UserMessageStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    private SystemMessage systemMessage;

    @ManyToOne(optional = false)
    private WebUser webUser;

    // Delivery Tracking
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date deliveredAt;  // When message was created for this user

    // Read Tracking
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date readAt;  // When user opened the message

    // Acknowledgment Tracking
    private boolean acknowledged = false;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date acknowledgedAt;

    // Dismiss Tracking
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dismissedAt;  // When user cleared from notification area

    // Auto-Archive
    private boolean archived = false;  // Auto-archived after N days

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date archivedAt;

    // User's preferred language when they read it
    @Column(length = 10)
    private String readInLanguage;  // "en", "si", "ta"

    // Constructors
    public UserMessageStatus() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SystemMessage getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(SystemMessage systemMessage) {
        this.systemMessage = systemMessage;
    }

    public WebUser getWebUser() {
        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
    }

    public Date getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Date deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Date getReadAt() {
        return readAt;
    }

    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Date getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Date acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Date getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(Date dismissedAt) {
        this.dismissedAt = dismissedAt;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Date getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Date archivedAt) {
        this.archivedAt = archivedAt;
    }

    public String getReadInLanguage() {
        return readInLanguage;
    }

    public void setReadInLanguage(String readInLanguage) {
        this.readInLanguage = readInLanguage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserMessageStatus)) {
            return false;
        }
        UserMessageStatus other = (UserMessageStatus) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserMessageStatus{" + "id=" + id + ", user=" + (webUser != null ? webUser.getName() : "null") + '}';
    }
}
