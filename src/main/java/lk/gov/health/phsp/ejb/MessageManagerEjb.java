/*
 * Author : Dr. M H B Ariyaratne
 *
 * MO(Health Information), Department of Health Services, Southern Province
 * and
 * Email : buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import lk.gov.health.phsp.entity.SystemMessage;
import lk.gov.health.phsp.entity.UserMessageStatus;
import lk.gov.health.phsp.facade.SystemMessageFacade;
import lk.gov.health.phsp.facade.UserMessageStatusFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

/**
 * EJB for managing scheduled tasks related to system messages
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical Informatics)
 */
@Stateless
public class MessageManagerEjb {

    @EJB
    private SystemMessageFacade systemMessageFacade;

    @EJB
    private UserMessageStatusFacade userMessageStatusFacade;

    /**
     * Auto-retire expired messages Daily at 1:00 AM
     */
    @Schedule(hour = "1", minute = "0", persistent = false)
    public void retireExpiredMessages() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("now", new Date());
            params.put("retired", false);

            String jpql = "SELECT m FROM SystemMessage m "
                    + "WHERE m.retired = :retired "
                    + "AND m.expiryDate IS NOT NULL "
                    + "AND m.expiryDate < :now";

            List<SystemMessage> expiredMessages
                    = systemMessageFacade.findByJpql(jpql, params);

            if (expiredMessages != null && !expiredMessages.isEmpty()) {
                for (SystemMessage msg : expiredMessages) {
                    msg.setRetired(true);
                    msg.setRetiredAt(new Date());
                    systemMessageFacade.edit(msg);
                }

                System.out.println("MessageManagerEjb: Auto-retired "
                        + expiredMessages.size() + " expired messages");
            }
        } catch (Exception e) {
            System.out.println("MessageManagerEjb: Error retiring expired messages: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Auto-archive old read messages Daily at 2:00 AM Archives messages that
     * were read more than 30 days ago
     */
    @Schedule(hour = "2", minute = "0", persistent = false)
    public void archiveOldMessages() {
        try {
            int archiveDays = 30;  // Configurable

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -archiveDays);
            Date archiveDate = cal.getTime();

            Map<String, Object> params = new HashMap<>();
            params.put("archiveDate", archiveDate);
            params.put("archived", false);

            String jpql = "SELECT ums FROM UserMessageStatus ums "
                    + "WHERE ums.readAt IS NOT NULL "
                    + "AND ums.readAt < :archiveDate "
                    + "AND ums.archived = :archived";

            List<UserMessageStatus> oldMessages
                    = userMessageStatusFacade.findByJpql(jpql, params);

            if (oldMessages != null && !oldMessages.isEmpty()) {
                for (UserMessageStatus msg : oldMessages) {
                    msg.setArchived(true);
                    msg.setArchivedAt(new Date());
                    userMessageStatusFacade.edit(msg);
                }

                System.out.println("MessageManagerEjb: Auto-archived "
                        + oldMessages.size() + " old messages");
            }
        } catch (Exception e) {
            System.out.println("MessageManagerEjb: Error archiving old messages: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }
}
