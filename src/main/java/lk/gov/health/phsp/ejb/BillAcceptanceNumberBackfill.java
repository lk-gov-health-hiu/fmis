package lk.gov.health.phsp.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import lk.gov.health.phsp.facade.BillFacade;

/**
 * On deployment, gives CPC acceptance numbers to bills that were accepted
 * before those numbers were introduced. After the first run there is nothing
 * left to number, so later deployments do nothing.
 */
@Singleton
@Startup
public class BillAcceptanceNumberBackfill {

    @EJB
    private BillFacade billFacade;

    @PostConstruct
    public void backfill() {
        try {
            int numbered = billFacade.backfillAcceptanceNumbers();
            if (numbered > 0) {
                Logger.getLogger(BillAcceptanceNumberBackfill.class.getName()).log(Level.INFO,
                        "Gave acceptance numbers to {0} previously accepted bills", numbered);
            }
        } catch (Exception e) {
            // Never block deployment over this - it will be retried on the next one
            Logger.getLogger(BillAcceptanceNumberBackfill.class.getName()).log(Level.SEVERE,
                    "Backfilling bill acceptance numbers failed", e);
        }
    }

}
