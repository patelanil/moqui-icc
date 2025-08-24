1. After the instance update, load the data using the "By Data Types" option in Data Import in the maarg instance. The data type should be "ext-upgrade".
2. Follow the "Upgrade Steps" added below.
3. Follow the "Upgrade Steps" in ofbiz-oms-udm or mantle-shopify-connector component if any.
4. Follow the client specific manual if any.
    1. Check the transformations and Nifi flows if configured and requires an update.

## Upgrade Steps
### 1. Transfer Order Fulfilled Items Feed
1. A new service is added.
   1. A new service is created TransferOrderFulfilledItemsFeed to handle the partial fulfillment explode-off scenario.
   2. Follow the client specific manual to create and configure a new job for this from template job generate_TransferOrderFulfilledItemsFeed.