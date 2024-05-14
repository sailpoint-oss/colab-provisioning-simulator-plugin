package pro.jakubiak.provisioning.simulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.IntegrationConfig;
import sailpoint.object.ManagedResource;
import sailpoint.object.ProvisioningPlan;
import sailpoint.tools.GeneralException;

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
    private static final String CONFIG_NAME = "ProvisioningSimulator";
    private static final String APP_CONFIG_NAME = "appConfig";
    private static final String INT_CONFIG_NAME = "integrationConfig";
    private static final String FILTER_CONFIG_NAME = "filterConfig";
    private final SailPointContext context;
    private IntegrationConfig iiqConfig;
    private Map<String, Object> appConfig;
    private Map<String, Object> intConfig;
    private Map<String, ManagedResource> interceptedApplications;

    private Boolean disableProvisioning = false;
    private Boolean saveUnfilteredRecords = false;
    private Boolean enableWhitelist = false;
    private Boolean storeAdditionalId = false;
    private String additionalIdAttributeNames = "";

    /**
     * Instantiates a new Config helper.
     *
     * @param context the context
     * @throws GeneralException the general exception
     */
    ConfigHelper(SailPointContext context) throws GeneralException {
        this.context = context;
        try {
            iiqConfig = context.getObject(IntegrationConfig.class, CONFIG_NAME);
        } catch (GeneralException e) {
            logger.fatal("Error while reading integration config");
        }
        try {
            intConfig = (Map<String, Object>) iiqConfig.getAttributes().get(INT_CONFIG_NAME);
        } catch (Exception e) {
            logger.fatal("Error while reading integration config");
        }
        try {
            appConfig = (Map<String, Object>) iiqConfig.getAttributes().get(APP_CONFIG_NAME);
        } catch (Exception e) {
            logger.fatal("Error while reading app config");
        }
        if (intConfig != null) {
            disableProvisioning = (Boolean) intConfig.get("disableProvisioning");
            saveUnfilteredRecords = (Boolean) intConfig.get("saveUnfilteredRecords");
            enableWhitelist = (Boolean) intConfig.get("enableWhitelist");
            storeAdditionalId = (Boolean) intConfig.get("storeAdditionalId");
            additionalIdAttributeNames = (String) intConfig.get("additionalIdAttributeNames");
        }
    }

    /**
     * Gets disable provisioning.
     *
     * @return the disable provisioning
     */
    public Boolean getDisableProvisioning() {
        return disableProvisioning;
    }

    /**
     * Sets disable provisioning.
     *
     * @param disableProvisioning the disable provisioning
     */
    public void setDisableProvisioning(Boolean disableProvisioning) {
        this.disableProvisioning = disableProvisioning;
    }

    /**
     * Gets save unfiltered records.
     *
     * @return the save unfiltered records
     */
    public Boolean getSaveUnfilteredRecords() {
        return saveUnfilteredRecords;
    }

    /**
     * Sets save unfiltered records.
     *
     * @param saveUnfilteredRecords the save unfiltered records
     */
    public void setSaveUnfilteredRecords(Boolean saveUnfilteredRecords) {
        this.saveUnfilteredRecords = saveUnfilteredRecords;
    }

    /**
     * Gets enable whitelist.
     *
     * @return the enable whitelist
     */
    public Boolean getEnableWhitelist() {
        return enableWhitelist;
    }

    /**
     * Sets enable whitelist.
     *
     * @param enableWhitelist the enable whitelist
     */
    public void setEnableWhitelist(Boolean enableWhitelist) {
        this.enableWhitelist = enableWhitelist;
    }

    /**
     * Gets store additional id.
     *
     * @return the store additional id
     */
    public Boolean getStoreAdditionalId() {
        return storeAdditionalId;
    }

    /**
     * Sets store additional id.
     *
     * @param storeAdditionalId the store additional id
     */
    public void setStoreAdditionalId(Boolean storeAdditionalId) {
        this.storeAdditionalId = storeAdditionalId;
    }

    /**
     * Gets additional id attribute names.
     *
     * @return the additional id attribute names
     */
    public String getAdditionalIdAttributeNames() {
        return additionalIdAttributeNames;
    }

    /**
     * Sets additional id attribute names.
     *
     * @param additionalIdAttributeNames the additional id attribute names
     */
    public void setAdditionalIdAttributeNames(String additionalIdAttributeNames) {
        this.additionalIdAttributeNames = additionalIdAttributeNames;
    }

    /**
     * Is app configured boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public Boolean isAppConfigured(String appName) {
        return appConfig.containsKey(appName);
    }

    /**
     * Gets app config.
     *
     * @param appName the app name
     * @return the app config
     */
    public Map<String, Object> getAppConfig(String appName) {
        return (Map<String, Object>) appConfig.get(appName);
    }

    /**
     * Gets filter config.
     *
     * @param appName the app name
     * @return the filter config
     */
    public Map<String, Object> getFilterConfig(String appName) {
        return (Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME);
    }

    /**
     * Gets create filter config.
     *
     * @param appName the app name
     * @return the create filter config
     */
    public Map<String, Object> getCreateFilterConfig(String appName) {
        return (Map<String, Object>) getFilterConfig(appName).get(ProvisioningPlan.AccountRequest.Operation.Create.toString());
    }

    /**
     * Gets modify filter config.
     *
     * @param appName the app name
     * @return the modify filter config
     */
    public Map<String, Object> getModifyFilterConfig(String appName) {
        return (Map<String, Object>) getFilterConfig(appName).get(ProvisioningPlan.AccountRequest.Operation.Modify.toString());
    }

    /**
     * Gets operation filter config.
     *
     * @param appName   the app name
     * @param operation the operation
     * @return the operation filter config
     */
    public Map<String, Object> getOperationFilterConfig(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        return (Map<String, Object>) getFilterConfig(appName).get(operation.toString());
    }

    /**
     * Is create filter configured boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public Boolean isCreateFilterConfigured(String appName) {
        return isAppConfigured(appName) && ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).containsKey(ProvisioningPlan.AccountRequest.Operation.Create.toString());
    }

    /**
     * Is modify filter configured boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public Boolean isModifyFilterConfigured(String appName) {
        return isAppConfigured(appName) && ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).containsKey(ProvisioningPlan.AccountRequest.Operation.Modify.toString());
    }

    /**
     * Is operation allowed boolean.
     *
     * @param appName   the app name
     * @param operation the operation
     * @return the boolean
     */
    public Boolean isOperationAllowed(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        return isAppConfigured(appName) && ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).containsKey(operation.toString());
    }

    /**
     * Gets operation filter.
     *
     * @param appName   the app name
     * @param operation the operation
     * @return the operation filter
     */
    public List<String> getOperationFilter(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        return (List<String>) ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).get(operation.toString());
    }

    /**
     * Is intercepted application boolean.
     *
     * @param appName the app name
     * @return the boolean
     */
    public Boolean isInterceptedApplication(String appName) {
        return interceptedApplications.containsKey(appName);
    }

    /**
     * Gets intercepted applications.
     *
     * @return the intercepted applications
     */
    public List<String> getInterceptedApplications() {
        return (List<String>) interceptedApplications.keySet();
    }

    /**
     * Sets intercepted applications.
     *
     * @param interceptedApplications the intercepted applications
     */
    public void setInterceptedApplications(Map<String, ManagedResource> interceptedApplications) {
        this.interceptedApplications = interceptedApplications;
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
    public void saveConfig() throws GeneralException {
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
    public void saveConfig(Map<String, Object> intConfig, Map<String, Object> appConfig) throws GeneralException {
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
    public void setOperationFilter(String appName, ProvisioningPlan.AccountRequest.Operation operation, List<String> filter) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).put(operation.toString(), filter);
    }

    /**
     * Sets create filter.
     *
     * @param appName the app name
     * @param filter  the filter
     */
    public void setCreateFilter(String appName, List<String> filter) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).put(ProvisioningPlan.AccountRequest.Operation.Create.toString(), filter);
    }

    /**
     * Sets modify filter.
     *
     * @param appName the app name
     * @param filter  the filter
     */
    public void setModifyFilter(String appName, List<String> filter) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).put(ProvisioningPlan.AccountRequest.Operation.Modify.toString(), filter);
    }

    /**
     * Remove operation filter.
     *
     * @param appName   the app name
     * @param operation the operation
     */
    public void removeOperationFilter(String appName, ProvisioningPlan.AccountRequest.Operation operation) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).remove(operation.toString());
    }

    /**
     * Remove create filter.
     *
     * @param appName the app name
     */
    public void removeCreateFilter(String appName) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).remove(ProvisioningPlan.AccountRequest.Operation.Create.toString());
    }

    /**
     * Remove modify filter.
     *
     * @param appName the app name
     */
    public void removeModifyFilter(String appName) {
        ((Map<String, Object>) getAppConfig(appName).get(FILTER_CONFIG_NAME)).remove(ProvisioningPlan.AccountRequest.Operation.Modify.toString());
    }

    /**
     * Sets app config.
     *
     * @param appName   the app name
     * @param appConfig the app config
     */
    public void setAppConfig(String appName, Map<String, Object> appConfig) {
        this.appConfig.put(appName, appConfig);
    }

    /**
     * Remove app config.
     *
     * @param appName the app name
     */
    public void removeAppConfig(String appName) {
        appConfig.remove(appName);
    }

    /**
     * Sets filter config.
     *
     * @param appName      the app name
     * @param filterConfig the filter config
     */
    public void setFilterConfig(String appName, Map<String, Object> filterConfig) {
        getAppConfig(appName).put(FILTER_CONFIG_NAME, filterConfig);
    }

    /**
     * Remove filter config.
     *
     * @param appName the app name
     */
    public void removeFilterConfig(String appName) {
        getAppConfig(appName).remove(FILTER_CONFIG_NAME);
    }

    /**
     * Sets config.
     *
     * @param intConfig the int config
     */
    public void setConfig(Map<String, Object> intConfig) {
        this.intConfig = intConfig;
    }

    /**
     * Remove config.
     */
    public void removeConfig() {
        intConfig = null;
    }

    /**
     * Remove intercepted applications.
     */
    public void removeInterceptedApplications() {
        interceptedApplications = null;
    }

    /**
     * Sets intercepted application.
     *
     * @param appName         the app name
     * @param managedResource the managed resource
     */
    public void setInterceptedApplication(String appName, ManagedResource managedResource) {
        interceptedApplications.put(appName, managedResource);
    }

    /**
     * Remove intercepted application.
     *
     * @param appName the app name
     */
    public void removeInterceptedApplication(String appName) {
        interceptedApplications.remove(appName);
    }
}
