package pro.jakubiak.provisioning.simulator;

import sailpoint.api.SailPointContext;
import sailpoint.tools.GeneralException;

import java.util.List;

/**
 * The type Provisioning simulator helper.
 */
public class ProvisioningSimulatorHelper {

    /**
     * Store transactions.
     *
     * @param context the context
     * @param records the records
     * @throws GeneralException the general exception
     */
    public static void storeTransactions(SailPointContext context, List<ProvisioningRecord> records) throws GeneralException {
        context.startTransaction();
        for (ProvisioningRecord record : records) {
            context.saveObject(record);
        }
        context.commitTransaction();
    }
}
