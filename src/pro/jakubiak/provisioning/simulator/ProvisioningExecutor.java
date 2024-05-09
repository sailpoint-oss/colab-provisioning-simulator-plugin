package pro.jakubiak.provisioning.simulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.connector.Connector;
import sailpoint.connector.ConnectorFactory;
import sailpoint.integration.AbstractIntegrationExecutor;
import sailpoint.integration.RequestResult;
import sailpoint.object.*;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;
import sailpoint.tools.Util;

import java.util.*;

/**
 * The type Provisioning executor.
 */
public class ProvisioningExecutor extends AbstractIntegrationExecutor {
    /**
     * The constant logger.
     */
    public static final Log logger = LogFactory.getLog(ProvisioningExecutor.class);

    private static final String INTEGRATION_CONFIG = "integrationConfig";
    private static final String APP_CONFIG = "appConfig";
    private static final String FILTER_CONFIG = "filterConfig";
    private static final String WHITELISTING_ENABLED = "enableWhitelisting";
    private static final String TRACE_ENABLED = "saveUnfilteredRecords";
    private static final String DEBUG_ENABLED = "disableProvisioning";
    private static final String STORE_ADDITIONAL_ID = "storeAdditionalId";
    private static final String OPERATION_PROVISIONED = "Provisioned";
    private static final String OPERATION_FILTERED = "Filtered";
    private static Map<String, Object> appConfig;
    private static Boolean whitelistingEnabled = false;
    private static Map<String, List<String>> filterConfig;
    private static Boolean trace = false;
    private static Boolean debug = false;
    private static Boolean storeAdditionalId = false;
    private static String additionalIdAttributeName;

    /**
     * Is whitelisting enabled boolean.
     *
     * @return the boolean
     */
    public static Boolean isWhitelistingEnabled() {
        return whitelistingEnabled;
    }

    /**
     * Is log everything enabled boolean.
     *
     * @return the boolean
     */
    public static Boolean isLogEverythingEnabled() {
        return trace;
    }

    private void logDebug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    private void logError(String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
    }

    private void logCritical(String message) {
        if (logger.isFatalEnabled()) {
            logger.fatal(message);
        }
    }

    @Override
    public void configure(SailPointContext context, IntegrationConfig config) throws Exception {
        super.configure(context, config);

        Map<String, Object> integrationConfig = (Map<String, Object>) config.getAttribute(INTEGRATION_CONFIG);
        appConfig = (Map<String, Object>) config.getAttribute(APP_CONFIG);

        whitelistingEnabled = Util.otob(integrationConfig.get(WHITELISTING_ENABLED));
        trace = Util.otob(integrationConfig.get(TRACE_ENABLED));
        debug = Util.otob(integrationConfig.get(DEBUG_ENABLED));
        storeAdditionalId = Util.otob(integrationConfig.get(STORE_ADDITIONAL_ID));
        if (storeAdditionalId) {
            additionalIdAttributeName = (String) integrationConfig.get("additionalIdAttributeName");
            if (additionalIdAttributeName == null) {
                throw new GeneralException("Required parameter: additionalIdAttributeName was not defined.");
            }
        }

        if (Util.isEmpty(INTEGRATION_CONFIG)) {
            throw new GeneralException("Required parameter: " + INTEGRATION_CONFIG + " was not defined.");
        }
        if (Util.isEmpty(APP_CONFIG)) {
            throw new GeneralException("Required parameter: " + APP_CONFIG + " was not defined.");
        }
    }

    private ProvisioningPlan processWhitelistingProvisioningPlan(ProvisioningPlan oldPlan, SailPointContext context) throws GeneralException {
        logDebug("Started whitelisting processing");
        ProvisioningPlan plan = new ProvisioningPlan(oldPlan);
        List<AttributeRequest> newAttributeRequests = new ArrayList<>();
        List<AccountRequest> newAccountRequests = new ArrayList<>();
        List<AccountRequest> accountRequests = plan.getAccountRequests();
        logDebug("Plan contains " + accountRequests.size() + " account requests");
        Identity identity = plan.getIdentity();
        logDebug("Identity is: " + identity.getName());
        List<ProvisioningRecord> records;
        records = new ArrayList<>();
        boolean atLeastOneAttributeUnfiltered = false;
        logDebug("Processing account requests");
        for (AccountRequest accountRequest : accountRequests) {
            logDebug("Processing account request: " + accountRequest.toXml());
            List<AttributeRequest> attributeRequests = accountRequest.getAttributeRequests();
            logDebug("Account request contains " + attributeRequests.size() + " attribute requests");
            for (AttributeRequest attributeRequest : attributeRequests) {
                logDebug("Processing attribute request: " + attributeRequest.toXml());
                logDebug("Checking if attribute is on the list - " + isAttributeOnTheList(attributeRequest, accountRequest.getOperation()));
                if (isAttributeOnTheList(attributeRequest, accountRequest.getOperation())) {
                    logDebug("Attribute is on the list");
                    newAttributeRequests.add(attributeRequest);
                    logDebug("Checking if isLogEverythingEnabled");
                    if (isLogEverythingEnabled()) {
                        logDebug("LogEverything is enabled");
                        logDebug("Adding record to the list");
                        if (!storeAdditionalId) {
                            records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_PROVISIONED));
                        } else {
                            records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_PROVISIONED, additionalIdAttributeName));
                        }
                        logDebug("Record added");
                    }
                    logDebug("Setting atLeastOneAttributeUnfiltered to true");
                    atLeastOneAttributeUnfiltered = true;
                } else {
                    logDebug("Attribute is not on the list");
                    logDebug("Adding record to the list");
                    if (!storeAdditionalId) {
                        records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_FILTERED));
                    } else {
                        records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_FILTERED, additionalIdAttributeName));
                    }
                    logDebug("Record added");
                    logDebug("Removing attribute from the account request");
                }
            }
            accountRequest.setAttributeRequests(newAttributeRequests);
            logDebug("AccountRequest after removal - " + accountRequest.toXml());

            logDebug("Checking if isCreationFiltered");
            if (isCreationFiltered() && !atLeastOneAttributeUnfiltered) {
                logDebug("Creation is filtered and no attribute is unfiltered");
            } else {
                logDebug("Creation is not filtered or at least one attribute is unfiltered");
                newAccountRequests.add(accountRequest);
            }
        }
        plan.setAccountRequests(newAccountRequests);
        logDebug("Plan after processing account requests: " + plan.toXml());
        logDebug("Checking if records list is not empty");
        if (!records.isEmpty()) {
            logDebug("Records list is not empty");
            logDebug("Storing transactions");
            ProvisioningSimulatorHelper.storeTransactions(context, records);
        } else {
            logDebug("Records list is empty");
        }
        logDebug("Returning plan");
        return plan;
    }

    private ProvisioningPlan processBlacklistingProvisioningPlan(ProvisioningPlan oldPlan, SailPointContext context) throws GeneralException {
        logDebug("Started blacklisting processing");
        ProvisioningPlan plan = new ProvisioningPlan(oldPlan);
        List<AttributeRequest> newAttributeRequests = new ArrayList<>();
        List<AccountRequest> newAccountRequests = new ArrayList<>();
        List<AccountRequest> accountRequests = plan.getAccountRequests();
        logDebug("Plan contains " + accountRequests.size() + " account requests");
        Identity identity = plan.getIdentity();
        logDebug("Identity is: " + identity.getName());
        List<ProvisioningRecord> records = new ArrayList<>();
        boolean atLeastOneAttributeUnfiltered = false;
        logDebug("Processing account requests");
        for (AccountRequest accountRequest : accountRequests) {
            logDebug("Processing account request: " + accountRequest.toXml());
            List<AttributeRequest> attributeRequests = accountRequest.getAttributeRequests();
            logDebug("Account request contains " + attributeRequests.size() + " attribute requests");
            for (AttributeRequest attributeRequest : attributeRequests) {
                logDebug("Processing attribute request: " + attributeRequest.toXml());
                logDebug("Checking if attribute is on the list - " + isAttributeOnTheList(attributeRequest, accountRequest.getOperation()));
                if (!isAttributeOnTheList(attributeRequest, accountRequest.getOperation())) {
                    logDebug("Attribute is not on the list");
                    newAttributeRequests.add(attributeRequest);
                    logDebug("Checking if isLogEverythingEnabled");
                    if (isLogEverythingEnabled()) {
                        logDebug("LogEverything is enabled");
                        logDebug("Adding record to the list");
                        if (!storeAdditionalId) {
                            records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_PROVISIONED));
                        } else {
                            records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_PROVISIONED, additionalIdAttributeName));
                        }
                        logDebug("Record added");
                    }
                    logDebug("Setting atLeastOneAttributeUnfiltered to true");
                    atLeastOneAttributeUnfiltered = true;
                } else {
                    logDebug("Attribute is on the list");
                    logDebug("Adding record to the list");
                    if (!storeAdditionalId) {
                        records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_FILTERED));
                    } else {
                        records.add(new ProvisioningRecord(accountRequest, attributeRequest, identity, OPERATION_FILTERED, additionalIdAttributeName));
                    }
                    logDebug("Record added");
                    logDebug("Removing attribute from the account request");
                }
            }
            accountRequest.setAttributeRequests(newAttributeRequests);
            logDebug("AccountRequest after removal - " + accountRequest.toXml());

            logDebug("Checking if isCreationFiltered");
            if (isCreationFiltered() && !atLeastOneAttributeUnfiltered) {
                logDebug("Creation is filtered and no attribute is unfiltered");
            } else {
                logDebug("Creation is not filtered or at least one attribute is unfiltered");
                newAccountRequests.add(accountRequest);
            }
        }
        plan.setAccountRequests(newAccountRequests);
        logDebug("Plan after processing account requests: " + plan.toXml());
        logDebug("Checking if records list is not empty");
        if (!records.isEmpty()) {
            logDebug("Records list is not empty");
            logDebug("Storing transactions");
            ProvisioningSimulatorHelper.storeTransactions(context, records);
        } else {
            logDebug("Records list is empty");
        }
        logDebug("Returning plan");
        return plan;
    }

    private Boolean isAttributeOnTheList(ProvisioningPlan.AttributeRequest attributeRequest, ProvisioningPlan.AccountRequest.Operation operation) {
        List<String> filterAttributes = filterConfig.get(operation.toString());
        String attributeName = attributeRequest.getName();
        return filterAttributes.contains(attributeName);
    }

    private Boolean isCreationFiltered() {
        Boolean createConfigPresent = filterConfig.get("Create") != null;
        if (isWhitelistingEnabled()) {
            return !createConfigPresent;
        }
        return createConfigPresent;
    }

    @Override
    public ProvisioningResult provision(ProvisioningPlan plan) throws Exception {
        if (plan == null) {
            throw new GeneralException("Cannnot process empty provisioning plan");
        }
        ProvisioningPlan newPlan;
        // Make any necessary modifications to the plan, here.
        Map<String, List<String>> selectedAppConfig;
        Identity identity = plan.getIdentity();
        logDebug("Identity is: " + identity.getName());
        logDebug("Processing provisioning plan: " + plan.toXml());
        // filter out all attributes
        logDebug("Plan contains " + plan.getAccountRequests().size() + " account requests");
        String applicationName = plan.getAccountRequests().get(0).getApplicationName();
        logDebug("Application name: " + applicationName);
        logDebug("Initilizing filterConfig");
        try {
            selectedAppConfig = (Map<String, List<String>>) appConfig.get(applicationName);
        } catch (Exception e) {
            throw new GeneralException("ApplicationFilter config is not correct");
        }
        try {
            filterConfig = (Map<String, List<String>>) selectedAppConfig.get(FILTER_CONFIG);
        } catch (Exception e) {
            throw new GeneralException("ApplicationFilter config is not correct");
        }
        logDebug("FilterConfig: " + filterConfig.toString());

        if (whitelistingEnabled) {
            logDebug("Whitelisting enabled");
            newPlan = processWhitelistingProvisioningPlan(plan, _context);
            logDebug("After whitelisting provisioning plan: " + newPlan.toXml());
        } else {
            logDebug("Whitelisting disabled");
            newPlan = processBlacklistingProvisioningPlan(plan, _context);
            logDebug("After blacklisting provisioning plan: " + newPlan.toXml());
        }
        // execute provisioning
        if (!debug) {
            logDebug("Integration works in PRODUCTION mode");
            List<ProvisioningPlan.AccountRequest> accountRequests = newPlan.getAccountRequests();
            for (ProvisioningPlan.AccountRequest accountRequest : accountRequests) {
                logDebug("Before connector provision call");
                Connector conn = ConnectorFactory.getConnector(_context.getObject(Application.class, accountRequest.getApplicationName()), null);
                conn.provision(newPlan);
                logDebug("After connector provision call");
            }
        } else {
            logDebug("Integration works in DEBUG mode - provisioning filtered out");
        }
        logDebug("After connector provision call");

        logDebug("Provisioning plan: " + newPlan.toXml());

        ProvisioningResult result = null;
        result = new ProvisioningResult();
        result.setStatus(ProvisioningResult.STATUS_QUEUED);
        logDebug("Returning ProvisioningResult");
        logDebug("ProvisioningResult: " + result.toXml());
        return result;
    }

    @Override
    public void configure(@SuppressWarnings("rawtypes") Map args) throws Exception {
        super.configure(args);
    }

    @Override
    public String ping() throws Exception {
        return super.ping();
    }

    @Override
    public RequestResult getRequestStatus(String requestID) throws Exception {
        if (Util.isEmpty(requestID)) {
            throw new GeneralException("Request ID cannot be null");
        }
        RequestResult requestResult = new RequestResult();
        requestResult.setRequestID(requestID);
        requestResult.setStatus(RequestResult.STATUS_IN_PROCESS);

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add(Filter.eq("id", requestID));
        queryOptions.setDistinct(true);
        Iterator<Object[]> completionStatusArray = _context.search(TaskResult.class, queryOptions, Arrays.asList("completionStatus", "messages"));
        if (completionStatusArray.hasNext()) {
            Object[] next = completionStatusArray.next();
            TaskResult.CompletionStatus completionStatus = (TaskResult.CompletionStatus) next[0];
            @SuppressWarnings("unchecked")
            List<Message> messages = (List<Message>) next[1];
            if (TaskResult.CompletionStatus.Success.equals(completionStatus) || TaskResult.CompletionStatus.Warning.equals(completionStatus)) {
                requestResult.setStatus(RequestResult.STATUS_COMMITTED);
                requestResult.setWarnings(messages);
            } else if (TaskResult.CompletionStatus.Error.equals(completionStatus) || TaskResult.CompletionStatus.Terminated.equals(completionStatus)) {
                requestResult.setStatus(RequestResult.STATUS_FAILURE);
                requestResult.setErrors(messages);
            } else {
                requestResult.setWarnings(messages);
            }
        }


        return requestResult;
    }

    @Override
    public ProvisioningResult checkStatus(String requestId) throws Exception {
        return super.checkStatus(requestId);
    }

    @Override
    public boolean shouldRetry(Exception ex, List<String> retryErrorList) {
        return super.shouldRetry(ex, retryErrorList);
    }
}
