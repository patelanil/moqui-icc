## Upgrade Steps

1. Generic Steps for Upgrade data
   1. Pause the Service Jobs scheduled for the feeds if required for the upgrade.
   2. Update the instance with the data load command to load the upgrade data only. 
   3. Follow the client specific manual if any.
   4. Check the Nifi flows if configured.
   
### Upgrade Steps
   1. Brokered Order Items Feed
        1. There is change in the sendPath of the feed, follow the below steps, if Nifi flows are configured.
           1. Check for existing files in the previous SFTP locations configured with the SystemMessageType record.
              If there are any files, then process them. 
           2. Stop the processors if running after processing the files if any.
           3. Create the new folder on SFTP.
           4. Update the required path variables value in the respective processors of the Nifi Flow.
           5. Start the processor.

   2. Fulfilled Order Items Feed
        1. There is a change in the sendPath of the feed, follow the steps same as Brokered Order Items Feed above, if any Nifi flows are configured.       
   
### Clean up obsolete Service Job Parameter

1. There are two ways to remove parameter from the Service Job.
   1. Using webtools
      1. From the webtools, click on the System -> Service Jobs.
      2. Search the Service Job name.
      3. To delete the  parameter, click on the cross sign on the right side of the parameter.

   2. Using SqlRunner
      1. Run the SQL added as part of UpgradeSQL.sql file.

2. The below Feed Jobs require the clean up.
   1. Fulfilled Order Items Feed 
      1. Job Name - **generate_FulfilledOrderItemsFeed**
      2. Parameters 
         1. **parentFacilityTypeId**
      
   2. Brokered Order Items Feed
      1. Job Name - **generate_BrokeredOrderItemsFeed**
      2. Parameters
          1. **parentFacilityTypeId**
      
### Below Feeds have a Deprecated service (old format) and new service added for updated format

1. Fulfilled Order Items Feed
   1. Old Service Name - generate#FulfilledOrderItemsFeedOld
   2. New Service Name - generate#FulfilledOrderItemsFeed
   
2. Brokered Orders Feed
   1. Old Service Name - generate#BrokeredOrderItemsFeedOld
   2. New Service Name - generate#BrokeredOrderItemsFeed
   
3. Returns Financial Feed
   1. Old Service Name - generate#ReturnsFinancialFeedOld
   2. New Service Name - generate#ReturnsFinancialFeed

**NOTE** 
1. The old services are updated to have suffix as 'Old'.
2. The Feed Template Jobs will get updated for any parameter change as part of the data load, no additional upgrade step required.
3. Follow the Client Specific Manual for other upgrade steps.

### Write access to the below entity
1. Provide write access to the OrderFulfillmentHistory(ORDER_FULFILLMENT_HISTORY) entity for the custom user of the OFBiz Transactional database. Contact System Admin team for this step.
   NOTE: The SQL query required for this is added in the ofbiz-oms-usl repository's ReadMe file.

