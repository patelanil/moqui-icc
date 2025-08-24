## Upgrade Steps

### Remove consumeServiceName field from Financial feeds SystemMessageType data
1. Generate feed service will now directly call queue#SystemMessage service instead of receive#IncomingSystemMessage service after
the feed is generated.
2. Thus, consumeServiceName is not required in SystemMessageType data.
3. Remove the consumeServiceName from the below systemMessageTypeIds used for Financial Feeds-
   1. ReturnsFinancialFeed
   2. SalesFinancialFeed
   3. AppeasementsFinancialFeed
   4. InStoreReturnsFinancialFeed
   5. EcomReturnsFinancialFeed
   
### Remove consumeServiceName field from Inventory feeds SystemMessageType data
1. Generate feed service will now directly call queue#SystemMessage service instead of receive#IncomingSystemMessage service after
   the feed is generated.
2. Thus, consumeServiceName is not required in SystemMessageType data.
3. Remove the consumeServiceName from the below systemMessageTypeIds used for Inventory Feeds-
    1. HotWaxShopifyInventoryFeed
    2. StoreBrokeredInventoryDeltaFeed
    3. DoNotHaveInventoryVarianceFeed
    4. StoreCancelledInventoryDeltaFeed

### Remove consumeServiceName field from Order feeds SystemMessageType data
1. Generate feed service will now directly call queue#SystemMessage service instead of receive#IncomingSystemMessage service after
   the feed is generated.
2. Thus, consumeServiceName is not required in SystemMessageType data.
3. Remove the consumeServiceName from the below systemMessageTypeIds used for Order Feeds-
   1. StoreFulfilledOrderItemsFeed
   2. WHBrokeredOrderItemsFeed
   3. FulfilledOrderItemsFeed
   4. BrokeredOrderItemsFeed
   5. WHFulfilledOrderItemsFeed

### Remove consumeServiceName field from Shipment feeds SystemMessageType data
1. Generate feed service will now directly call queue#SystemMessage service instead of receive#IncomingSystemMessage service after
   the feed is generated.
2. Thus, consumeServiceName is not required in SystemMessageType data.
3. Remove the consumeServiceName from the below systemMessageTypeIds used for Shipment Feeds-
   1. ReturnsShipmentFeed

### Fulfilled Order Items Feed
1. Improved handling for the scenario where no shipments are in shipped status for an order in the Fulfilled Order Items Feed. 
2. **NOTE:** 
   1. As per the updated handling, if an order item falls under the scenario of having no shipments in shipped status 
      then in the HC generated JSON, shipments[n] list will be prepared as empty, however the order level details will still be included in the feed.  
   2. Revisit the below transformation and update them if required to ensure they are consistent with the handling made for the shipment shipped status in the Fulfilled Order Items Feed.
      1. Store Fulfilled Order Items Feed HotWax to M3.
      2. Fulfilled Order Items Feed for Order To Invoice, HotWax to RetailPro.
      3. Fulfilled Order Items Feed for Manual Slip, HotWax to RetailPro.
      4. Store Fulfilled Order Items Feed, HotWax to BI/BW NEC.
      5. HotWax to Shopify Fulfillment Feed, for all running projects.
      6. Fulfilled Order Items Feed, HotWax to NetSuite.