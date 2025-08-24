### Inventory Item Variance Feed
1. The Template Job for Inventory Item Variance Feed is updated with below changes:
    1. Mapped with new Service Name which is added to prepare the date time to fetch the
       Inventory Item Variance as per the provided date time.
    2. In the service the below parameters are added:
        1. productStoreIds
            1. This parameter is added to give the support of productStoreId in the Inventory Item Variance Feed.
        2. varianceReasonIds
            1. This parameter is added to fetch the Inventory Item Variance for specific variance reason IDs.
        3. parentFacilityTypeIds
           1. This parameter is added to fetch the Inventory Item Variance for specific parent Facility Type Ids.
        4. jobName
            1. This parameter is added to keep the service job name which will be used to fetch
               the lastRunTime of the service job for the Inventory Item Variance Feed.
        5. lastRunTime
            1. This parameter is added to keep the record of lastRunTime of the service job
               for Inventory Item Variance Feed.
        6. skipLastRunTimeUpdate
            1. This parameter is added to mainly used while debugging for the Inventory Item Variance Feed.          
2. The data load with ext-upgrade type will take care of the update in the Template Jobs.
3. Remove the parameter varianceReasonId and parentFacilityTypeId from the generate_InventoryVarianceFeed Service Job.
4. **NOTE:** For the Feed Service Jobs already in Production specific to all Live Projects, below changes are required
    1. The serviceName parameter needs to updated manually in the service jobs

       | Feed Name | New Service Name |
       | --- | --- |
       | Inventory Item Variance Feed | co.hotwax.ofbiz.InventoryServices.get#InventoryItemVariance |

    2. New parameters needs to be added and existing parameter needs to be updated manually in the service jobs

       | Parameter Name | Parameter Value |
       |  --- |-----|
       | varianceReasonIds |     |
       | parentFacilityTypeIds |     |
       | productStoreIds |     |
       | jobName | generate_InventoryVarianceFeed |
       | lastRunTime |     |
       | skipLastRunTimeUpdate | false |
