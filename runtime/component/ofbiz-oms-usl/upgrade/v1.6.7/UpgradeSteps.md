## Upgrade Steps
1. Generic Steps for Upgrade data
    1. Pause the Service Jobs scheduled for the feeds if required for the upgrade.
    2. Update the instance with the data load command to load the upgrade data only.
    3. Follow the client specific manual if any.
    4. Check the Nifi flows if configured.

### Inventory Cycle Count Variance Feed
1. A new service job parameter inventoryCountItemStatusId,  is added to Template Job of Inventory Cycle Count Variance Feed.
2. The data load with ext-upgrade type will take care of the update in the Template Job.
3. The Inventory Cycle Count Variance Feed Service Jobs already in Production specific to all Live Projects, below changes are required.
    1. To incorporate the new "Inventory Count Item Status Id" parameter, utilize the migration service's "add#ParameterToInventoryCycleCountFeed" functionality.

   | Feed Name | New Service Job Parameter | 
   |--------|---------|
   | Inventory Cycle Count Variance Feed|inventoryCountItemStatusId|
