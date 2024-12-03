package pro.jakubiak.provisioning.simulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.tools.GeneralException;

import java.util.List;

/**
 * The type Provisioning simulator helper.
 */
public class ProvisioningSimulatorHelper {
    private static final Log logger = LogFactory.getLog(ProvisioningExecutor.class);

    /**
     * Store transactions.
     *
     * @param context the context
     * @param records the records
     * @throws GeneralException the general exception
     */

    protected static void storeTransactions(SailPointContext context, List<ProvisioningRecord> records) throws GeneralException {
        context.startTransaction();
        logger.debug("Storing provisioning records " + records.size());
        for (ProvisioningRecord record : records) {
            logger.debug("Storing provisioning record: " + record.toString());
            context.saveObject(record);
        }
        logger.debug("Committing transaction");
        context.commitTransaction();
    }
}
