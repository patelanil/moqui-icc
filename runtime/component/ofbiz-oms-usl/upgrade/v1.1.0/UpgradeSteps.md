## Upgrade Steps

1. Pause the Service Jobs scheduled for the feeds.
2. Update the instance with the data load command to load the upgrade data only.
3. Provide write access to the ExternalFulfillmentOrderItem(EXTERNAL_FULFILLMENT_ORDER_ITEM) entity for the custom user of the OFBiz Transactional database. Contact System Admin team for this step.
   NOTE: The SQL query required for this is added in the ofbiz-oms-usl repository's ReadMe file.
4. Follow the client specific manual if any. 
    1. Check the Nifi flows if configured.
    2. The Service Jobs paused in step 1 are the template Service Jobs. For brand specific feeds, clone the required Service Job from the template Service Job. 
   
    Note: The template Service Jobs will always be in the paused status, the cloned Service Jobs will be scheduled to generate brand wise feeds. 

5. Once the changes are done and verified, unpause/schedule the required Service Jobs.

### Clean up obsolete System Message Types and Service Jobs

1. Run the SQLs added as part of UpgradeSQL.sql file
