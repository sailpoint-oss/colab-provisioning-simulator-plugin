package pro.jakubiak.provisioning.simulator;

import sailpoint.api.SailPointContext;
import sailpoint.object.*;
import sailpoint.tools.GeneralException;

import java.util.List;

/**
 * The type Provisioning record.
 */
public class ProvisioningRecord extends SailPointObject {
    private static final long serialVersionUID = 1L;
    private Identity identity;
    private String identitySupplId;
    private String identityName;
    private String applicationName;
    private String nativeIdentity;
    private String operation;
    private String provisioningStatus;
    private Object oldValue;
    private Object newValue;
    private String attributeName;

    /**
     * Instantiates a new Provisioning record.
     */
    public ProvisioningRecord() {
    }

    /**
     * Instantiates a new Provisioning record.
     *
     * @param accountRequest   the account request
     * @param attributeRequest the attribute request
     * @param identity         the identity
     * @param status           the status
     * @param identitySupplId  the identity suppl id
     */
    public ProvisioningRecord(ProvisioningPlan.AccountRequest accountRequest, ProvisioningPlan.AttributeRequest attributeRequest, Identity identity, String status, String identitySupplId) {
        this.identitySupplId = (String) identity.getAttribute(identitySupplId);
        this.identity = identity;
        this.identityName = identity.getName();
        this.applicationName = accountRequest.getApplicationName();
        this.nativeIdentity = accountRequest.getNativeIdentity();
        this.operation = accountRequest.getOperation().toString();
        this.attributeName = attributeRequest.getName();
        this.provisioningStatus = status;
        List<Link> links = identity.getLinks();
        for (Link link : links) {
            if (link.getApplicationName().equals(accountRequest.getApplicationName())) {
                if (link.getNativeIdentity().equals(accountRequest.getNativeIdentity())) {
                    this.oldValue = link.getAttribute(attributeRequest.getName());
                    break;
                }
            }
        }

        this.newValue = attributeRequest.getValue();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setAction("update");
        auditEvent.setTarget(identity.getName());
        auditEvent.setAttributeName(attributeName);
        auditEvent.setAttribute("oldValue", oldValue);
        auditEvent.setAttribute("newValue", newValue);

    }

    /**
     * Instantiates a new Provisioning record.
     *
     * @param accountRequest   the account request
     * @param attributeRequest the attribute request
     * @param identity         the identity
     * @param status           the status
     */
    public ProvisioningRecord(ProvisioningPlan.AccountRequest accountRequest, ProvisioningPlan.AttributeRequest attributeRequest, Identity identity, String status) {
        this.identity = identity;
        this.identityName = identity.getName();
        this.applicationName = accountRequest.getApplicationName();
        this.nativeIdentity = accountRequest.getNativeIdentity();
        this.operation = accountRequest.getOperation().toString();
        this.attributeName = attributeRequest.getName();
        this.provisioningStatus = status;
        List<Link> links = identity.getLinks();
        for (Link link : links) {
            if (link.getApplicationName().equals(accountRequest.getApplicationName())) {
                if (link.getNativeIdentity().equals(accountRequest.getNativeIdentity())) {
                    this.oldValue = link.getAttribute(attributeRequest.getName());
                    break;
                }
            }
        }

        this.newValue = attributeRequest.getValue();
    }

    /**
     * Instantiates a new Provisioning record.
     *
     * @param identity        the identity
     * @param identitySupplId the identity suppl id
     * @param identityName    the identity name
     * @param applicationName the application name
     * @param nativeIdentity  the native identity
     * @param operation       the operation
     * @param oldValue        the old value
     * @param newValue        the new value
     */
    public ProvisioningRecord(Identity identity, String identitySupplId, String identityName, String applicationName, String nativeIdentity, String operation, String oldValue, String newValue) {
        this.identity = identity;
        this.identitySupplId = identitySupplId;
        this.identityName = identityName;
        this.applicationName = applicationName;
        this.nativeIdentity = nativeIdentity;
        this.operation = operation;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Instantiates a new Provisioning record.
     *
     * @param identity the identity
     */
    public ProvisioningRecord(Identity identity) {
        this.identity = identity;
    }

    /**
     * Gets provisioning status.
     *
     * @return the provisioning status
     */
    public String getProvisioningStatus() {
        return provisioningStatus;
    }

    /**
     * Sets provisioning status.
     *
     * @param provisioningStatus the provisioning status
     */
    public void setProvisioningStatus(String provisioningStatus) {
        this.provisioningStatus = provisioningStatus;
    }

    /**
     * Gets attribute name.
     *
     * @return the attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Sets attribute name.
     *
     * @param attributeName the attribute name
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Store transaction.
     *
     * @param context the context
     * @throws GeneralException the general exception
     */
    public void storeTransaction(SailPointContext context) throws GeneralException {
        // Store transaction in the database
        context.startTransaction();
        context.saveObject(this);
        context.commitTransaction();
    }

    /**
     * Gets identity.
     *
     * @return the identity
     */
    public Identity getIdentity() {
        return identity;
    }

    /**
     * Sets identity.
     *
     * @param identity the identity
     */
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    /**
     * Gets identity name.
     *
     * @return the identity name
     */
    public String getIdentityName() {
        return identityName;
    }

    /**
     * Sets identity name.
     *
     * @param identityName the identity name
     */
    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    /**
     * Gets application name.
     *
     * @return the application name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets application name.
     *
     * @param applicationName the application name
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Gets native identity.
     *
     * @return the native identity
     */
    public String getNativeIdentity() {
        return nativeIdentity;
    }

    /**
     * Sets native identity.
     *
     * @param nativeIdentity the native identity
     */
    public void setNativeIdentity(String nativeIdentity) {
        this.nativeIdentity = nativeIdentity;
    }

    /**
     * Gets operation.
     *
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets operation.
     *
     * @param operation the operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "ProvisioningRecord{" +
                "identity=" + identity +
                ", identitySupplId='" + identitySupplId + '\'' +
                ", identityName='" + identityName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", nativeIdentity='" + nativeIdentity + '\'' +
                ", operation='" + operation + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }

    /**
     * Gets identity suppl id.
     *
     * @return the identity suppl id
     */
    public String getIdentitySupplId() {
        return identitySupplId;
    }

    /**
     * Sets identity suppl id.
     *
     * @param identitySupplId the identity suppl id
     */
    public void setIdentitySupplId(String identitySupplId) {
        this.identitySupplId = identitySupplId;
    }

    /**
     * Gets old value.
     *
     * @return the old value
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Sets old value.
     *
     * @param oldValue the old value
     */
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * Sets old value.
     *
     * @param oldValue the old value
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * Gets new value.
     *
     * @return the new value
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Sets new value.
     *
     * @param newValue the new value
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * Sets new value.
     *
     * @param newValue the new value
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }


}
