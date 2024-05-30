package pro.jakubiak.provisioning.simulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.*;
import sailpoint.server.InternalContext;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;
import sailpoint.workflow.WorkflowContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Config helper.
 */
public class ConfigHelper {
    /**
     * The constant logger.
     */
    public static final Log logger = LogFactory.getLog(ConfigHelper.class);
    private static final String CONFIG_NAME = "ProvisioningSimulation";
    private static final String APP_CONFIG_NAME = "appConfig";
    private static final String INT_CONFIG_NAME = "integrationConfig";
    private static final String FILTER_CONFIG_NAME = "filterConfig";
    public static final String DISABLE_PROVISIONING = "disableProvisioning";
    public static final String SAVE_UNFILTERED_RECORDS = "saveUnfilteredRecords";
    public static final String ENABLE_WHITELIST = "enableWhitelist";
    public static final String STORE_ADDITIONAL_ID = "storeAdditionalId";
    public static final String ADDITIONAL_ID_ATTRIBUTE_NAMES = "additionalIdAttributeNames";
    public static final String CREATE_FILTER = "createFilter";
    public static final String MODIFY_FILTER = "modifyFilter";
    public static final String REMOVE_APP_CONFIG = "removeAppConfig";
    public static final String ERROR_WHILE_SAVING_APPLICATION_CONFIGURATION = "Error while saving application configuration";
    private static SailPointContext context;
    private static IntegrationConfig iiqConfig;
    private static Map<String, Object> appConfig;
    private static Map<String, Object> intConfig;
    private static Map<String, ManagedResource> interceptedApplications;

    private static Boolean disableProvisioning = false;
    private static Boolean saveUnfilteredRecords = false;
    private static Boolean enableWhitelist = false;
    private static Boolean storeAdditionalId = false;
    private static String additionalIdAttributeNames = "";

    /**
     * Instantiates a new Config helper.
     *
     * @param internalContext the context
     * @throws GeneralException the general exception
     */
    public static void initContext(InternalContext internalContext) throws GeneralException {
        logger.debug("Initializing context with InternalContext");
       initContext(internalContext.getContext());
    }
    public static void initContext(SailPointContext contextInput) throws GeneralException {
        logger.debug("Initializing context with SailpointContext");
        context = contextInput;
        logger.debug("Reading integration config");
        try {
            logger.debug("Reading integration config");
            iiqConfig = context.getObject(IntegrationConfig.class, CONFIG_NAME);
        } catch (GeneralException e) {
            logger.fatal("Error while reading integration config");
        }
        logger.debug("Reading app config");
        try {
            intConfig = (Map<String, Object>) iiqConfig.getAttribute(INT_CONFIG_NAME);
        } catch (Exception e) {
            logger.fatal("Error while reading integration config");
        }
        logger.debug("Reading app config");
        try {
            appConfig = (Map<String, Object>) iiqConfig.getAttributes().get(APP_CONFIG_NAME);
        } catch (Exception e) {
            logger.fatal("Error while reading app config");
        }
        logger.debug("Reading global config");
        if (intConfig != null) {

            disableProvisioning = Util.otob(intConfig.get(DISABLE_PROVISIONING));
            logger.debug("disabling provisioning: " + disableProvisioning);
            saveUnfilteredRecords = Util.otob(intConfig.get(SAVE_UNFILTERED_RECORDS));
            logger.debug("save unfiltered records: " + saveUnfilteredRecords);
            enableWhitelist = Util.otob(intConfig.get(ENABLE_WHITELIST));
            logger.debug("enable whitelist: " + enableWhitelist);
            storeAdditionalId = Util.otob(intConfig.get(STORE_ADDITIONAL_ID));
            logger.debug("store additional id: " + storeAdditionalId);
            additionalIdAttributeNames = Util.otos(intConfig.get(ADDITIONAL_ID_ATTRIBUTE_NAMES));
            logger.debug("additional id attribute names: " + additionalIdAttributeNames);
        }
        if(appConfig!=null) {
            logger.debug("AppConfig is: " + appConfig.toString());
        }
        if(intConfig!=null) {
            logger.debug("IntConfig is: " + intConfig.toString());
        }
        logger.debug("Reading intercepted applications");
        try {
            interceptedApplications = new HashMap<>();
            List<ManagedResource> managedResources = iiqConfig.getResources();
            for (ManagedResource managedResource : managedResources) {
                interceptedApplications.put(managedResource.getApplication().getName(), managedResource);
            }
            } catch (Exception e) {
            logger.fatal("Error while reading intercepted applications");
        }

    }

    /**
     * Gets disable provisioning.
     *
     * @return to disable provisioning
     */
    public static Boolean getDisableProvisioning() {
        return disableProvisioning;
    }

    /**
     * Sets disable provisioning.
     *
     * @param disableProvisioningInput to disable provisioning
     */
    public static void setDisableProvisioning(Boolean disableProvisioningInput) {
        disableProvisioning = disableProvisioningInput;
    }

    public static void saveSimulatorConfig(WorkflowContext wfc) throws GeneralException {
        SailPointContext context = wfc.getSailPointContext();
        initContext(context);
        Attributes<String,Object> args = wfc.getArguments();
        Application application = ConfigHelper.context.getObject(Application.class,(String) args.get("application"));
        String appName = application.getName();
        logger.debug("Saving simulator configuration");
        logger.debug("Simulator configuration: " + args);

        logger.debug("Saving integration configuration");
        Map<String,Object> newIntegrationConfig = new HashMap<>();
        newIntegrationConfig.put(DISABLE_PROVISIONING, args.get(DISABLE_PROVISIONING));
        newIntegrationConfig.put(SAVE_UNFILTERED_RECORDS, args.get(SAVE_UNFILTERED_RECORDS));
        newIntegrationConfig.put(ENABLE_WHITELIST, args.get(ENABLE_WHITELIST));
        newIntegrationConfig.put(STORE_ADDITIONAL_ID, args.get(STORE_ADDITIONAL_ID));
        newIntegrationConfig.put(ADDITIONAL_ID_ATTRIBUTE_NAMES, args.get(ADDITIONAL_ID_ATTRIBUTE_NAMES));

        try {
            iiqConfig.getAttributes().put(INT_CONFIG_NAME, newIntegrationConfig);
            context.saveObject(iiqConfig);
            context.commitTransaction();
        } catch (GeneralException e) {
            logger.error("Error while saving integration configuration");
        }
        logger.debug("Integration configuration saved");
        logger.debug("Saving application configuration for application" + appName);
        Map<String,Object> newAppConfig = new HashMap<>();
        Map<String,Object> newFilterConfig = new HashMap<>();
        newFilterConfig.put(ProvisioningPlan.AccountRequest.Operation.Create.toString(), args.get(CREATE_FILTER));
        newFilterConfig.put(ProvisioningPlan.AccountRequest.Operation.Modify.toString(), args.get(MODIFY_FILTER));

        newAppConfig.put(FILTER_CONFIG_NAME, newFilterConfig);
        appConfig.put(appName, newAppConfig);
        iiqConfig.getAttributes().put(APP_CONFIG_NAME, appConfig);

        try {
            context.saveObject(iiqConfig);
            context.commitTransaction();
        } catch (GeneralException e) {
            logger.error(ERROR_WHILE_SAVING_APPLICATION_CONFIGURATION);
        }
        logger.debug("Checking if application is on the list of Managed Resources");
        if (interceptedApplications == null || !interceptedApplications.containsKey(appName)) {
            logger.debug("Application is not on the list of Managed Resources");
            logger.debug("Adding application to the list of Managed Resources");
            ManagedResource managedResource = new ManagedResource();
            managedResource.setApplication(application);
            iiqConfig.add(managedResource);
            logger.debug("Saving ManagedResources configuration");
            try {
                context.saveObject(iiqConfig);
                context.commitTransaction();
            } catch (GeneralException e) {
                logger.error(ERROR_WHILE_SAVING_APPLICATION_CONFIGURATION);
            }
        }
        logger.debug("Application configuration saved - checking if application needs to be removed");
        logger.debug("RemoveAppConfig: "+args.get(REMOVE_APP_CONFIG));
        logger.debug("RemoveAppConfig: "+Util.otob(args.get(REMOVE_APP_CONFIG)));

        if(Util.otob(args.get(REMOVE_APP_CONFIG)))
        {
            logger.debug("Removing application configuration for application" + appName);
            appConfig.remove(appName);
            iiqConfig.getAttributes().put(APP_CONFIG_NAME, appConfig);
            iiqConfig.removeManagedResource(application);
            try {
                context.saveObject(iiqConfig);
                context.commitTransaction();
            } catch (GeneralException e) {
                logger.error(ERROR_WHILE_SAVING_APPLICATION_CONFIGURATION);
            }
        }
        logger.debug("Application configuration saved");
        logger.debug("Simulator configuration: "+context.getObject(IntegrationConfig.class,CONFIG_NAME).toXml());
    }

    /**
     * Gets save unfiltered records.
     *
     * @return the save unfiltered records
     */
    public static Boolean getSaveUnfilteredRecords() {
        return saveUnfilteredRecords;
    }
    public static Boolean getBooleanConfigAttribute(String attributeName) {
        return Util.otob(intConfig.get(attributeName));
    }
    /**
     * Sets save unfiltered records.
     *
     * @param saveUnfilteredRecordsInput the save unfiltered records
     */
    public static void setSaveUnfilteredRecords(Boolean saveUnfilteredRecordsInput) {
        saveUnfilteredRecords = saveUnfilteredRecordsInput;
    }

    /**
     * Gets enable whitelist.
     *
     * @return the enable whitelist
     */
    public static Boolean getEnableWhitelist() {
        return enableWhitelist;
    }

    /**
     * Sets enable whitelist.
     *
     * @param enableWhitelistInput the enable whitelist
     */
    public static void setEnableWhitelist(Boolean enableWhitelistInput) {
        enableWhitelist = enableWhitelistInput;
    }

    /**
     * Gets store additional id.
     *
     * @return the store additional id
     */
    public static Boolean getStoreAdditionalId() {
        return storeAdditionalId;
    }

    /**
     * Sets store additional id.
     *
     * @param storeAdditionalIdInput the store additional id
     */
    public static void setStoreAdditionalId(Boolean storeAdditionalIdInput) {
        storeAdditionalId = storeAdditionalIdInput;
    }

    /**
     * Gets additional id attribute names.
     *
     * @return the additional id attribute names
     */

    /**
     * Sets additional id attribute names.
     *
     * @param additionalIdAttributeNamesInput the additional id attribute names
     */
    public static void setAdditionalIdAttributeNames(String additionalIdAttributeNamesInput) {
        additionalIdAttributeNames = additionalIdAttributeNamesInput;
    }

    /**
     * Is app configured boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public static Boolean isAppConfigured(String appName) {
        return appConfig.containsKey(appName);
    }

    /**
     * Gets app config.
     *
     * @param appName the app name
     * @return the app config
     */
    public static Map<String, Object> getAppConfig(String appName) {
        if(Boolean.FALSE.equals(isAppConfigured(appName))) {
            return new HashMap<>();
        }
        return (Map<String, Object>) appConfig.get(appName);
    }

    /**
     * Gets filter config.
     *
     * @param appName the app name
     * @return the filter config
     */
    public static Map<String, Object> getFilterConfig(String appName) {
        if(Boolean.FALSE.equals(isAppConfigured(appName))) {
            return new HashMap<>();
        }
        return (Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME);
    }

    /**
     * Gets create filter config.
     *
     * @param appName the app name
     * @return the create filter config
     */
    public static List<String> getCreateFilterConfig(String appName) {
        if (Boolean.FALSE.equals(isAppConfigured(appName))) {
            return new ArrayList<>();
        }
        if (!getFilterConfig(appName).containsKey(ProvisioningPlan.AccountRequest.Operation.Create.toString())) {
            return new ArrayList<>();
        }
        return (List<String>) getFilterConfig(appName).get(ProvisioningPlan.AccountRequest.Operation.Create.toString());
    }


    /**
     * Gets modify filter config.
     *
     * @param appName the app name
     * @return the modify filter config
     */
    public static List<String> getModifyFilterConfig(String appName) {
        if(Boolean.FALSE.equals(isAppConfigured(appName))) {
            return new ArrayList<>();
        }
        if(!getFilterConfig(appName).containsKey(ProvisioningPlan.AccountRequest.Operation.Modify.toString())) {
            return new ArrayList<>();
        }
        return (List<String>) getFilterConfig(appName).get(ProvisioningPlan.AccountRequest.Operation.Modify.toString());
    }

    /**
     * Gets operation filter config.
     *
     * @param appName   the app name
     * @param operation the operation
     * @return the operation filter config
     */
    public static List<String> getOperationFilterConfig(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        return (List<String>) getFilterConfig(appName).get(operation.toString());
    }

    /**
     * Is create filter configured boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public static Boolean isCreateFilterConfigured(String appName) {
        return isAppConfigured(appName) && ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).containsKey(ProvisioningPlan.AccountRequest.Operation.Create.toString());
    }

    /**
     * Is modify filter configured boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public static Boolean isModifyFilterConfigured(String appName) {
        return isAppConfigured(appName) && ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).containsKey(ProvisioningPlan.AccountRequest.Operation.Modify.toString());
    }

    /**
     * Is operation allowed boolean.
     *
     * @param appName   the app name
     * @param operation the operation
     * @return the boolean
     */
    public static Boolean isOperationAllowed(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        return isAppConfigured(appName) && ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).containsKey(operation.toString());
    }

    /**
     * Gets operation filter.
     *
     * @param appName   the app name
     * @param operation the operation
     * @return the operation filter
     */
    public static List<String> getOperationFilter(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        return (List<String>) ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).get(operation.toString());
    }

    /**
     * Is intercepted application boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public  static Boolean isInterceptedApplication(String appName) {
        return interceptedApplications.containsKey(appName);
    }
public static String getAdditionalIdAttributeNames() {
        return additionalIdAttributeNames;
    }
    /**
     * Gets intercepted applications.
     *
     * @return the intercepted applications
     */
    public static List<String> getInterceptedApplications() {
        return (List<String>) interceptedApplications.keySet();
    }

    /**
     * Sets intercepted applications.
     *
     * @param interceptedApplicationsInput the intercepted applications
     */
    public static void setInterceptedApplications(Map<String, ManagedResource> interceptedApplicationsInput) {
        interceptedApplications = interceptedApplicationsInput;
    }

    /**
     * Gets intercepted application.
     *
     * @param appName the app name
     * @return the intercepted application
     */
    public ManagedResource getInterceptedApplication(String appName) {
        return interceptedApplications.get(appName);
    }

    /**
     * Save config.
     *
     * @throws GeneralException the general exception
     */
    public static void saveConfig() throws GeneralException {
        iiqConfig.getAttributes().put(INT_CONFIG_NAME, intConfig);
        iiqConfig.getAttributes().put(APP_CONFIG_NAME, appConfig);
        context.saveObject(iiqConfig);
        context.commitTransaction();
    }

    /**
     * Save config.
     *
     * @param intConfig the int config
     * @param appConfig the app config
     * @throws GeneralException the general exception
     */
    public static void saveConfig(Map<String, Object> intConfig, Map<String, Object> appConfig) throws GeneralException {
        iiqConfig.getAttributes().put(INT_CONFIG_NAME, intConfig);
        iiqConfig.getAttributes().put(APP_CONFIG_NAME, appConfig);
        context.saveObject(iiqConfig);
        context.commitTransaction();
    }

    /**
     * Sets operation filter.
     *
     * @param appName   the app name
     * @param operation the operation
     * @param filter    the filter
     */
    public static void setOperationFilter(String appName, ProvisioningPlan.AccountRequest.Operation operation, List<String> filter) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).put(operation.toString(), filter);
    }

    /**
     * Sets create filter.
     *
     * @param appName the app name
     * @param filter  the filter
     */
    public static void setCreateFilter(String appName, List<String> filter) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).put(ProvisioningPlan.AccountRequest.Operation.Create.toString(), filter);
    }

    /**
     * Sets modify filter.
     *
     * @param appName the app name
     * @param filter  the filter
     */
    public static void setModifyFilter(String appName, List<String> filter) {
        if(getAppConfig(appName) != null) {
            ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).put(ProvisioningPlan.AccountRequest.Operation.Modify.toString(), filter);
        }
    }

    /**
     * Remove operation filter.
     *
     * @param appName   the app name
     * @param operation the operation
     */
    public static void removeOperationFilter(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).remove(operation.toString());
    }

    /**
     * Remove create filter.
     *
     * @param appName the app name
     */
    public static void removeCreateFilter(String appName) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).remove(ProvisioningPlan.AccountRequest.Operation.Create.toString());
    }

    /**
     * Remove modify filter.
     *
     * @param appName the app name
     */
    public static void removeModifyFilter(String appName) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).remove(ProvisioningPlan.AccountRequest.Operation.Modify.toString());
    }

    /**
     * Sets app config.
     *
     * @param appName   the app name
     * @param appConfig the app config
     */
    public static void setAppConfig(String appName, Map<String, Object> appConfig) {
        appConfig.put(appName, appConfig);
    }

    /**
     * Remove app config.
     *
     * @param appName the app name
     */
    public static void removeAppConfig(String appName) {
        appConfig.remove(appName);
    }

    /**
     * Sets filter config.
     *
     * @param appName      the app name
     * @param filterConfig the filter config
     */
    public static void setFilterConfig(String appName, Map<String, Object> filterConfig) {
        getAppConfig(appName).put(FILTER_CONFIG_NAME, filterConfig);
    }

    /**
     * Remove filter config.
     *
     * @param appName the app name
     */
    public static void removeFilterConfig(String appName) {
        getAppConfig(appName).remove(FILTER_CONFIG_NAME);
    }

    /**
     * Sets config.
     *
     * @param intConfigInput the int config
     */
    public static void setConfig(Map<String, Object> intConfigInput) {
        intConfig = intConfigInput;
    }

    /**
     * Remove config.
     */
    public static void removeConfig() {
        intConfig = null;
    }

    /**
     * Remove intercepted applications.
     */
    public static void removeInterceptedApplications() {
        interceptedApplications = null;
    }

    /**
     * Sets intercepted application.
     *
     * @param appName         the app name
     * @param managedResource the managed resource
     */
    public static void setInterceptedApplication(String appName, ManagedResource managedResource) {
        interceptedApplications.put(appName, managedResource);
    }

    /**
     * Remove intercepted application.
     *
     * @param appName the app name
     */
    public static void removeInterceptedApplication(String appName) {
        interceptedApplications.remove(appName);
    }
}
