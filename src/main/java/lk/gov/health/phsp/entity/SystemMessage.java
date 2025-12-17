/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import lk.gov.health.phsp.enums.MessageType;
import lk.gov.health.phsp.enums.MessagePriority;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * Entity representing a system-wide message sent by administrators
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical Informatics)
 */
@Entity
@Table(name = "system_message")
public class SystemMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Message Content
    @Column(nullable = false, length = 500)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String messageBody;  // HTML content in English

    @Lob
    private String messageBodySinhala;  // Sinhala translation

    @Lob
    private String messageBodyTamil;    // Tamil translation

    // Targeting
    @Column(nullable = false, length = 1000)
    private String targetRoles;  // Comma-separated WebUserRole values

    // Message Properties
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessagePriority priority;

    // Expiry Settings
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date expiryDate;  // Message expires after this date

    private Integer autoExpireDays;  // Auto-expire after N days from creation

    // Acknowledgment Settings
    @Column(nullable = false)
    private boolean requiresAcknowledgment = false;

    // Attachments
    @OneToMany(mappedBy = "systemMessage", cascade = CascadeType.ALL)
    private List<MessageAttachment> attachments;

    // Statistics (denormalized for performance)
    private int totalRecipients = 0;
    private int acknowledgedCount = 0;
    private int readCount = 0;

    // Audit Fields
    @ManyToOne
    private WebUser creater;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;

    private boolean retired = false;

    @ManyToOne
    private WebUser retirer;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;

    // Constructors
    public SystemMessage() {
        this.attachments = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getMessageBodySinhala() {
        return messageBodySinhala;
    }

    public void setMessageBodySinhala(String messageBodySinhala) {
        this.messageBodySinhala = messageBodySinhala;
    }

    public String getMessageBodyTamil() {
        return messageBodyTamil;
    }

    public void setMessageBodyTamil(String messageBodyTamil) {
        this.messageBodyTamil = messageBodyTamil;
    }

    public String getTargetRoles() {
        return targetRoles;
    }

    public void setTargetRoles(String targetRoles) {
        this.targetRoles = targetRoles;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getAutoExpireDays() {
        return autoExpireDays;
    }

    public void setAutoExpireDays(Integer autoExpireDays) {
        this.autoExpireDays = autoExpireDays;
    }

    public boolean isRequiresAcknowledgment() {
        return requiresAcknowledgment;
    }

    public void setRequiresAcknowledgment(boolean requiresAcknowledgment) {
        this.requiresAcknowledgment = requiresAcknowledgment;
    }

    public List<MessageAttachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
    }

    public int getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(int totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public int getAcknowledgedCount() {
        return acknowledgedCount;
    }

    public void setAcknowledgedCount(int acknowledgedCount) {
        this.acknowledgedCount = acknowledgedCount;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SystemMessage)) {
            return false;
        }
        SystemMessage other = (SystemMessage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SystemMessage{" + "id=" + id + ", subject=" + subject + '}';
    }
}
