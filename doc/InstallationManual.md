
# Provisioning Simulator Integration Installation Guide 

This document provides the steps to install the Provisioning Simulator Integration tool.

## Prerequisites
Integration is tested with following setup:
- Database: MySQL
- IdentityIQ Version: 8.4
- Java Version: 22

## Installation Steps

1. Download the latest version of the Provisioning Simulator Integration tool from the [Releases]
2. Extract the zip file to a directory where your IdentityIQ is NOT installed
3. Copy the `provisioning-simulator-integration` directory to the IdentityIQ `$TOMCAT_HOME$/webapps/identityiq/WEB-INF/classes` directory
5. Import `IntegrationConfig-ProvisioningSimulator.xml` file in IdentityIQ.
6. Execute `$TOMCAT_HOME$/webapps/identityiq/WEB-INF/bin/iiq schema` script to generate new database files for the integration.
7. Go to `$TOMCAT_HOME$/webapps/identityiq/WEB-INF/bin/database`
8. Pick the database file for your database and execute it in your database. (if you did it already you may see a lot of "object already exists" errors but it's correct)
9. Restart your tomcat
10. Go to IdentityIQ and navigate to `debug`, find ObjectType `IntegrationConfig` and click on `ProvisioningSimulator` tab.
11. Configure based on your requirements integration config
12. Add entry for `ManagedResource` which you want to intercept in the `IntegrationConfig` object.

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<!-- CONTACT -->
## Discuss
[Click Here](https://developer.sailpoint.com/dicuss/tag/{tagName}) to discuss this tool with other users.
