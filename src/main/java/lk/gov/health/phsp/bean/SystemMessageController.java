/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.MessageAttachment;
import lk.gov.health.phsp.entity.SystemMessage;
import lk.gov.health.phsp.entity.UserMessageStatus;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.MessagePriority;
import lk.gov.health.phsp.enums.MessageType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.MessageAttachmentFacade;
import lk.gov.health.phsp.facade.SystemMessageFacade;
import lk.gov.health.phsp.facade.UserMessageStatusFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * Controller for managing system messages and user notifications
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical Informatics)
 */
@Named("systemMessageController")
@SessionScoped
public class SystemMessageController implements Serializable {

    @EJB
    private SystemMessageFacade systemMessageFacade;

    @EJB
    private UserMessageStatusFacade userMessageStatusFacade;

    @EJB
    private MessageAttachmentFacade messageAttachmentFacade;

    @EJB
    private WebUserFacade webUserFacade;

    @Inject
    private WebUserController webUserController;

    // For message creation
    private SystemMessage currentMessage;
    private List<WebUserRole> selectedRoles;
    private List<WebUserRole> availableRoles;

    // Multi-language support
    private String currentLanguage = "si";  // en, si, ta (default: Sinhala)

    // File upload
    private List<MessageAttachment> tempAttachments;

    // For user notification display
    private List<UserMessageStatus> unreadMessages;
    private int unreadCount;
    private UserMessageStatus selectedMessageStatus;

    // For admin message management
    private List<SystemMessage> sentMessages;
    private SystemMessage selectedMessage;
    private Date fromDate;
    private Date toDate;

    // Read messages for user
    private List<UserMessageStatus> readMessages;

    // Upload directory configuration
    private static final String UPLOAD_DIR = "D:\\fmis_uploads\\message_attachments\\";

    /**
     * Constructor
     */
    public SystemMessageController() {
    }

    /**
     * Initialize the controller
     */
    public void init() {
        if (currentMessage == null) {
            currentMessage = new SystemMessage();
        }
        if (tempAttachments == null) {
            tempAttachments = new ArrayList<>();
        }
        loadAvailableRoles();
        refreshUnreadMessages();
    }

    // ========== MESSAGE CREATION ==========

    /**
     * Prepare to create a new message
     */
    public String prepareCreateMessage() {
        if (!canSendMessages()) {
            JsfUtil.addErrorMessage("Unauthorized access. Only System Administrators and Super Users can send messages.");
            return null;
        }
        currentMessage = new SystemMessage();
        currentMessage.setPriority(MessagePriority.MEDIUM);
        currentMessage.setMessageType(MessageType.GENERAL);
        selectedRoles = new ArrayList<>();
        tempAttachments = new ArrayList<>();
        return "/systemAdmin/create_system_message";
    }

    /**
     * Send the message to selected user roles
     */
    public void sendMessage() {
        if (!canSendMessages()) {
            JsfUtil.addErrorMessage("Unauthorized access");
            return;
        }

        if (selectedRoles == null || selectedRoles.isEmpty()) {
            JsfUtil.addErrorMessage("Please select at least one target role");
            return;
        }

        // Debug logging
        System.out.println("=== Sending Message ===");
        System.out.println("Selected roles count: " + selectedRoles.size());
        System.out.println("Selected roles: " + selectedRoles);

        if (currentMessage.getSubject() == null || currentMessage.getSubject().trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please enter a subject");
            return;
        }

        // Check if at least one language message is provided
        boolean hasEnglish = currentMessage.getMessageBody() != null && !currentMessage.getMessageBody().trim().isEmpty();
        boolean hasSinhala = currentMessage.getMessageBodySinhala() != null && !currentMessage.getMessageBodySinhala().trim().isEmpty();
        boolean hasTamil = currentMessage.getMessageBodyTamil() != null && !currentMessage.getMessageBodyTamil().trim().isEmpty();

        if (!hasEnglish && !hasSinhala && !hasTamil) {
            JsfUtil.addErrorMessage("Please enter message content in at least one language (Sinhala, Tamil, or English)");
            return;
        }

        try {
            // Set sender
            currentMessage.setCreater(webUserController.getLoggedUser());
            currentMessage.setCreatedAt(new Date());

            // Set target roles
            String rolesCsv = selectedRoles.stream()
                    .map(WebUserRole::name)
                    .collect(Collectors.joining(","));
            currentMessage.setTargetRoles(rolesCsv);
            System.out.println("Target roles CSV: " + rolesCsv);

            // Calculate expiry date if autoExpireDays is set
            if (currentMessage.getAutoExpireDays() != null
                    && currentMessage.getAutoExpireDays() > 0) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, currentMessage.getAutoExpireDays());
                currentMessage.setExpiryDate(cal.getTime());
            }

            // Save message
            systemMessageFacade.create(currentMessage);

            // Handle attachments
            if (!tempAttachments.isEmpty()) {
                for (MessageAttachment attachment : tempAttachments) {
                    attachment.setSystemMessage(currentMessage);
                    messageAttachmentFacade.create(attachment);
                }
            }

            // Create UserMessageStatus for all targeted users
            int recipientCount = createUserMessageStatuses(currentMessage, selectedRoles);

            // Update statistics
            currentMessage.setTotalRecipients(recipientCount);
            systemMessageFacade.edit(currentMessage);

            JsfUtil.addSuccessMessage("Message sent successfully to " + recipientCount + " users");

            // Reset form
            currentMessage = new SystemMessage();
            currentMessage.setPriority(MessagePriority.MEDIUM);
            currentMessage.setMessageType(MessageType.GENERAL);
            selectedRoles = new ArrayList<>();
            tempAttachments = new ArrayList<>();

        } catch (Exception e) {
            JsfUtil.addErrorMessage("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create UserMessageStatus records for all users with selected roles
     */
    private int createUserMessageStatuses(SystemMessage message, List<WebUserRole> roles) {
        // Debug logging
        System.out.println("=== Creating UserMessageStatus records ===");
        System.out.println("Number of selected roles: " + (roles != null ? roles.size() : "null"));
        if (roles != null) {
            System.out.println("Selected roles: " + roles);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("roles", roles);
        params.put("retired", false);

        String jpql = "SELECT u FROM WebUser u "
                + "WHERE u.retired = :retired "
                + "AND u.webUserRole IN :roles";

        System.out.println("JPQL Query: " + jpql);
        System.out.println("Query parameters: retired=" + params.get("retired")
                + ", roles=" + params.get("roles"));

        List<WebUser> targetUsers = webUserFacade.findByJpql(jpql, params);

        // Additional diagnostic queries
        try {
            String countAllJpql = "SELECT u FROM WebUser u";
            List<WebUser> allUsers = webUserFacade.findByJpql(countAllJpql);
            System.out.println("Total users in database: " + (allUsers != null ? allUsers.size() : 0));

            String countActiveJpql = "SELECT u FROM WebUser u WHERE u.retired = false";
            List<WebUser> activeUsers = webUserFacade.findByJpql(countActiveJpql);
            System.out.println("Active (non-retired) users: " + (activeUsers != null ? activeUsers.size() : 0));
        } catch (Exception e) {
            System.out.println("Could not get user counts: " + e.getMessage());
        }

        System.out.println("Number of users found: " + (targetUsers != null ? targetUsers.size() : "null"));
        if (targetUsers != null && !targetUsers.isEmpty()) {
            System.out.println("Target users:");
            for (WebUser user : targetUsers) {
                System.out.println("  - " + user.getName() + " (" + user.getWebUserRole() + ")");
            }
        }

        Date now = new Date();
        for (WebUser user : targetUsers) {
            UserMessageStatus status = new UserMessageStatus();
            status.setSystemMessage(message);
            status.setWebUser(user);
            status.setDeliveredAt(now);
            status.setArchived(false);
            userMessageStatusFacade.create(status);
        }

        System.out.println("=== UserMessageStatus creation completed ===");
        return targetUsers.size();
    }

    /**
     * Handle file upload
     */
    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();

            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + file.getFileName();
            String filePath = UPLOAD_DIR + fileName;

            // Save file
            try (InputStream input = file.getInputStream();
                    FileOutputStream output = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
            }

            // Create attachment record
            MessageAttachment attachment = new MessageAttachment();
            attachment.setFileName(file.getFileName());
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setMimeType(file.getContentType());
            attachment.setUploadedAt(new Date());
            attachment.setUploadedBy(webUserController.getLoggedUser());

            tempAttachments.add(attachment);

            JsfUtil.addSuccessMessage("File uploaded: " + file.getFileName());

        } catch (Exception e) {
            JsfUtil.addErrorMessage("File upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove an attachment from temp list
     */
    public void removeAttachment(MessageAttachment attachment) {
        tempAttachments.remove(attachment);
        // Also delete physical file
        try {
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.out.println("Failed to delete file: " + e.getMessage());
        }
    }

    // ========== USER NOTIFICATIONS ==========

    /**
     * Refresh unread messages for the logged user
     */
    public void refreshUnreadMessages() {
        if (webUserController.getLoggedUser() == null) {
            unreadMessages = new ArrayList<>();
            unreadCount = 0;
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("user", webUserController.getLoggedUser());
            params.put("now", new Date());

            String jpql = "SELECT ums FROM UserMessageStatus ums "
                    + "WHERE ums.webUser = :user "
                    + "AND ums.readAt IS NULL "
                    + "AND ums.archived = false "
                    + "AND ums.systemMessage.retired = false "
                    + "AND (ums.systemMessage.expiryDate IS NULL OR ums.systemMessage.expiryDate > :now) "
                    + "ORDER BY ums.systemMessage.priority DESC, ums.deliveredAt DESC";

            unreadMessages = userMessageStatusFacade.findByJpql(jpql, params);

            if (unreadMessages == null) {
                unreadMessages = new ArrayList<>();
            }

            unreadCount = unreadMessages.size();
        } catch (Exception e) {
            unreadMessages = new ArrayList<>();
            unreadCount = 0;
            System.out.println("Error refreshing unread messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * View a message (marks as read if first time)
     */
    public String viewMessage(UserMessageStatus messageStatus) {
        this.selectedMessageStatus = messageStatus;

        // Mark as read if first time
        if (messageStatus.getReadAt() == null) {
            messageStatus.setReadAt(new Date());
            messageStatus.setReadInLanguage(currentLanguage);
            userMessageStatusFacade.edit(messageStatus);

            // Update statistics
            SystemMessage msg = messageStatus.getSystemMessage();
            msg.setReadCount(msg.getReadCount() + 1);
            systemMessageFacade.edit(msg);

            refreshUnreadMessages();
        }

        return "/webUser/view_message_detail";
    }

    /**
     * Acknowledge a message
     */
    public void acknowledgeMessage(UserMessageStatus messageStatus) {
        if (messageStatus.getSystemMessage().isRequiresAcknowledgment()
                && !messageStatus.isAcknowledged()) {

            messageStatus.setAcknowledged(true);
            messageStatus.setAcknowledgedAt(new Date());
            userMessageStatusFacade.edit(messageStatus);

            // Update statistics
            SystemMessage msg = messageStatus.getSystemMessage();
            msg.setAcknowledgedCount(msg.getAcknowledgedCount() + 1);
            systemMessageFacade.edit(msg);

            JsfUtil.addSuccessMessage("Message acknowledged");
            refreshUnreadMessages();
        }
    }

    /**
     * Dismiss a notification
     */
    public void dismissMessage(UserMessageStatus messageStatus) {
        messageStatus.setDismissedAt(new Date());
        userMessageStatusFacade.edit(messageStatus);
        refreshUnreadMessages();
        JsfUtil.addSuccessMessage("Notification dismissed");
    }

    /**
     * Get message content in current language with fallback
     */
    public String getMessageContent(SystemMessage message) {
        if (message == null) {
            return "";
        }

        String content = null;

        switch (currentLanguage) {
            case "si":
                // Try Sinhala first, then Tamil, then English
                content = message.getMessageBodySinhala();
                if (content == null || content.trim().isEmpty()) {
                    content = message.getMessageBodyTamil();
                }
                if (content == null || content.trim().isEmpty()) {
                    content = message.getMessageBody();
                }
                break;
            case "ta":
                // Try Tamil first, then Sinhala, then English
                content = message.getMessageBodyTamil();
                if (content == null || content.trim().isEmpty()) {
                    content = message.getMessageBodySinhala();
                }
                if (content == null || content.trim().isEmpty()) {
                    content = message.getMessageBody();
                }
                break;
            default:
                // Try English first, then Sinhala, then Tamil
                content = message.getMessageBody();
                if (content == null || content.trim().isEmpty()) {
                    content = message.getMessageBodySinhala();
                }
                if (content == null || content.trim().isEmpty()) {
                    content = message.getMessageBodyTamil();
                }
                break;
        }

        return content != null ? content : "";
    }

    /**
     * Set current language
     */
    public void setLanguage(String language) {
        this.currentLanguage = language;
    }

    /**
     * Load read messages for the user
     */
    public void loadReadMessages() {
        if (webUserController.getLoggedUser() == null) {
            readMessages = new ArrayList<>();
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("user", webUserController.getLoggedUser());

            String jpql = "SELECT ums FROM UserMessageStatus ums "
                    + "WHERE ums.webUser = :user "
                    + "AND ums.readAt IS NOT NULL "
                    + "AND ums.archived = false "
                    + "AND ums.systemMessage.retired = false "
                    + "ORDER BY ums.readAt DESC";

            readMessages = userMessageStatusFacade.findByJpql(jpql, params);

            if (readMessages == null) {
                readMessages = new ArrayList<>();
            }
        } catch (Exception e) {
            readMessages = new ArrayList<>();
            System.out.println("Error loading read messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== ADMIN MANAGEMENT ==========

    /**
     * Load sent messages for the admin
     */
    public void loadSentMessages() {
        if (!canSendMessages()) {
            sentMessages = new ArrayList<>();
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("creater", webUserController.getLoggedUser());
            params.put("retired", false);

            String jpql = "SELECT m FROM SystemMessage m "
                    + "WHERE m.creater = :creater "
                    + "AND m.retired = :retired "
                    + "ORDER BY m.createdAt DESC";

            sentMessages = systemMessageFacade.findByJpql(jpql, params);

            if (sentMessages == null) {
                sentMessages = new ArrayList<>();
            }
        } catch (Exception e) {
            sentMessages = new ArrayList<>();
            System.out.println("Error loading sent messages: " + e.getMessage());
        }
    }

    /**
     * Retire a message
     */
    public void retireMessage(SystemMessage message) {
        message.setRetired(true);
        message.setRetirer(webUserController.getLoggedUser());
        message.setRetiredAt(new Date());
        systemMessageFacade.edit(message);
        JsfUtil.addSuccessMessage("Message retired successfully");
        loadSentMessages();
    }

    /**
     * View a sent message
     */
    public void viewSentMessage(SystemMessage message) {
        this.selectedMessage = message;
    }

    // ========== AUTHORIZATION ==========

    /**
     * Check if user can send messages
     */
    private boolean canSendMessages() {
        if (webUserController.getLoggedUser() == null) {
            return false;
        }
        return webUserController.getLoggedUser().isSystemAdministrator()
                || webUserController.getLoggedUser().isSuperUser();
    }

    /**
     * Load available roles for selection
     */
    private void loadAvailableRoles() {
        availableRoles = Arrays.asList(WebUserRole.values());
    }

    /**
     * Get all message types
     */
    public MessageType[] getMessageTypes() {
        return MessageType.values();
    }

    /**
     * Get all priorities
     */
    public MessagePriority[] getPriorities() {
        return MessagePriority.values();
    }

    // ========== GETTERS AND SETTERS ==========

    public SystemMessage getCurrentMessage() {
        if (currentMessage == null) {
            currentMessage = new SystemMessage();
        }
        return currentMessage;
    }

    public void setCurrentMessage(SystemMessage currentMessage) {
        this.currentMessage = currentMessage;
    }

    public List<WebUserRole> getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(List<WebUserRole> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public List<WebUserRole> getAvailableRoles() {
        if (availableRoles == null) {
            loadAvailableRoles();
        }
        return availableRoles;
    }

    public void setAvailableRoles(List<WebUserRole> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(String currentLanguage) {
        this.currentLanguage = currentLanguage;
    }

    public List<MessageAttachment> getTempAttachments() {
        if (tempAttachments == null) {
            tempAttachments = new ArrayList<>();
        }
        return tempAttachments;
    }

    public void setTempAttachments(List<MessageAttachment> tempAttachments) {
        this.tempAttachments = tempAttachments;
    }

    public List<UserMessageStatus> getUnreadMessages() {
        if (unreadMessages == null) {
            refreshUnreadMessages();
        }
        return unreadMessages;
    }

    public void setUnreadMessages(List<UserMessageStatus> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public UserMessageStatus getSelectedMessageStatus() {
        return selectedMessageStatus;
    }

    public void setSelectedMessageStatus(UserMessageStatus selectedMessageStatus) {
        this.selectedMessageStatus = selectedMessageStatus;
    }

    public List<SystemMessage> getSentMessages() {
        if (sentMessages == null) {
            loadSentMessages();
        }
        return sentMessages;
    }

    public void setSentMessages(List<SystemMessage> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public SystemMessage getSelectedMessage() {
        return selectedMessage;
    }

    public void setSelectedMessage(SystemMessage selectedMessage) {
        this.selectedMessage = selectedMessage;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public List<UserMessageStatus> getReadMessages() {
        if (readMessages == null) {
            loadReadMessages();
        }
        return readMessages;
    }

    public void setReadMessages(List<UserMessageStatus> readMessages) {
        this.readMessages = readMessages;
    }
}
