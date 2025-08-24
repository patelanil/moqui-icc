1. After the instance update, load the data using the "By Data Types" option in Data Import in the maarg instance. The data type should be "ext-upgrade".
2. Follow the "Upgrade Steps" added below.
3. Follow the "Upgrade Steps" in ofbiz-oms-udm or mantle-shopify-connector component if any.
4. Follow the client specific manual if any.
    1. Check the transformations and Nifi flows if configured and requires an update.

## Upgrade Steps
### 1. Fulfilled Order Items Feed
1. orderTypeId parameter
    1. A new parameter, "orderTypeId" is added to the Template Job of Fulfilled Order Items Feed and its service. This is added to add the support to generate the feed for different order types.
    2. Add this parameter to all client specific feed Service Jobs created from the template generate_FulfilledOrderItemsFeed Service Job which already in Production by following these steps -
       1. Open the maarg instance go to System Dashboard.
       2. Click on Service Jobs, search for the client specific feed Service Jobs.
       3. Open any one serivce job and click on 'Add parameter' button.
       4. Write the name of parameter which is 'orderTypeId' and corresponding value if needed and add it.
       5. Alternate way could be, run SQL query to update the parameter -
            ```sql
             INSERT INTO service_job_parameter (job_name, parameter_name, parameter_value)
             VALUES ('generate_FulfilledOrderItemsFeed', 'orderTypeId', '')
            ```                     
       6. Ensure clearing cache if you are updating parameter though SQL.
    
### 2. Inventory Item Variance Feed
1. customParametersMap job parameter
   1. A new service job parameter, customParametersMap is added to Template Job of Inventory Item Variance Feed.
   2. This is added to support custom conditions while preparing the feed based on the fields of the feed's view.
   3. Add this parameter to all client specific feed Service Jobs created from the template generate_InventoryVarianceFeed Service Job which already in Production.

2. Facility group members list
    1. A new list for Facility group member has been added to the Inventory Item Variance Feed.
    2. Nifi flows need to be checked if some handling is required in Jolt transformation for any scenario.
       For eg- exclude a certain facility group member like NETSUITE_FULFILLMENT in Inventory Item Variance feed HotWax to NetSuite.
