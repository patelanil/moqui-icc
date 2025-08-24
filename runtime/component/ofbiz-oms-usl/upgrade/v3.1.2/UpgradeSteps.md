## Upgrade Steps

1. Pause the Service Jobs scheduled for the feeds.
2. Update the instance with the data load command to load the upgrade data only.
3. Run the first Upgrade SQl after loading the new data for Service Jobs.
4. Schedule the Service Jobs.

### Updated the parameters in the inventory variance feed
1. reasonEnumIds job parameter
   1. A service job parameter, varianceReasonIds already exists in Template Job of Inventory Variance Feed.
   2. This parameter will check whether the given variance reasons should be included or excluded in the feed.
   3. As per the change in ItemVarianceDetails view, Enumeration is used to fetch variance reason description in place of VarianceReason.
      Also, reasonEnumId is used to join InventoryItemVariance and Enumeration instead of varianceReasonId.
   4. Since, variance reason is now populated in reasonEnumId instead of varianceReasonId. So, we need to add a new parameter varianceReasonEnumIds and remove the
      existing parameter varianceReasonIds from the Service Job.
   5. Add varianceReasonEnumIds parameter and remove varianceReasonIds parameter to all client specific feed Service Jobs created from the template generate_InventoryVarianceFeed Service Job which are already in Production.
         

### Updated parameters in the template_generate_InventoryCycleCountVarianceFeed feed
1. Added the update data for the template service job
   1. Added `customParametersMap` parameter in the job.
2. Add this parameter to the cloned job if available in the respective instance.