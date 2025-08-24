## Upgrade Steps
1. Generic Steps for Upgrade data
    1. Pause the Service Jobs scheduled for the feeds if required for the upgrade.
    2. Update the instance with the data load command to load the upgrade data only.
    3. Follow the client specific manual if any.
    4. Check the Nifi flows if configured.

### Returns Financial Feed
1. A new service job parameter statusIds, is added to Template Job of Returns Financial Feed.
2. The data load with ext-upgrade type will take care of the update in the Template Job.
3. The Returns Financial Feed Service Jobs already in Production specific to all Live Projects, below changes are required.
    1. To incorporate the new "Status IDs" parameter, utilize the migration service's "migrate#ReturnsFinancialFeed" functionality.

   | Feed Name              | New Service Job Parameter |
   |------------------------|---------------------------|
   | Returns Financial Feed | statusIds                 |

### Fulfilled Order Items Feed
1. Fulfilled Order Items Feed is refactored to have 2 versions, 1 for ERP and 1 for Shopify.
2. New Templates and System Message Types will be loaded as part of upgrade data load.
3. For instances where Fulfilled Order Items Feed is being sent to ERP, new history entity will be used now which is 
   OrderFulfillmentErpHistory. 
   **NOTE**
   1. Check the HC OMS version to include this entity, v5.10.x is expected for this change.
   2. It is important to set the _sinceDate_ field to send the correct order items to the ERP, else all old orders 
   will also get sent to ERP which is not expected.
   3. The OrderAdjustmentHistory will be used in the feed being sent to ERP.
5. For instances where Fulfilled Order Items Feed is being sent to Shopify, existing history entity will be used which is
   OrderFulfillmentHistory. Here the only change will be stopping existing jobs and set up new job by cloning from 
   the new template of Fulfilled Order Items Feed of Shopify.

#### Fulfilled Order Items Feed for Shopify
1. A new service is added to send the fulfilled order items feed to shopify.
   1. A new service is created FulfilledOrderItemsFeedForShopify. This feed will be generated when the eligible order item has the status "ITEM_COMPLETED".
   2. Follow the client specific manual to create and configure a new job for this from template job generate_FulfilledOrderItemsFeedForShopify.

2. New Template job is added to generate Fulfilled Order Items Feed for Shopify. 
   
   | Feed Name                             | Added template job                         |
   |---------------------------------------|--------------------------------------------|
   | Fulfilled Order Item Feed for Shopify | generate_FulfilledOrderItemsFeedForShopify |


#### Fulfilled Order Items Feed for Erp
1. A new service is added to send the fulfilled orders feed for Erp systems.
   1. A new service is created FulfilledOrderItemsFeedForErp. This feed will be generated when the order has the status "ORDER_COMPLETED".
   2. Follow the client specific manual to create and configure a new job for this from template job generate_FulfilledOrderItemsFeedForErp.

2. New Template job is added to generate Fulfilled Order Items Feed for Erp.
   
   | Feed Name                         | Template job                     |
   |-----------------------------------|----------------------------------------|
   | Fulfilled Order Item Feed for Erp | generate_FulfilledOrderItemsFeedForErp |

3. New system message type is added for Fulfilled Order Items Feed for Erp 
   
   | Feed Name                         | System Message Type ID |
   |-----------------------------------|------------------------------|
   | Fulfilled Order Item Feed for Erp | ErpFulfilledOrderItemsFeed   |
  | Ecom Store Fulfilled Order Item Feed for Erp | ErpEcomStoreFulfilledOrderItemsFeed |
   | POS Store Fulfilled Order Item Feed for Erp  | ErpPosStoreFulfilledOrderItemsFeed |