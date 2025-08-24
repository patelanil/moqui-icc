## Upgrade Steps

### 1. Shopify Inventory Feed
1. facilityGroupId job parameter
   1. A new service job parameter, facilityGroupId is added to Template Job of Shopify Inventory Feed.
   2. This parameter will fetch and prepare the Shopify Inventory Feed for the specific Facility Group of Shopify channel.
   3. This change is done to support the change of Multichannel Inventory setup in OMS. Each channel will be set up as a Facility Group with Facility Group Type as CHANNEL_FAC_GROUP.
   4. So to fetch the inventory for the specific channel i.e. Shopify, its Facility Group ID is required to set the value of the facilityGroupId parameter.
   5. Add this parameter to all client specific feed Service Jobs created from the template generate_ShopifyInventoryFeedFromHotWax Service Job which already in Production.
   6. In the client setup, identify the corresponding Facility Group ID of Shopify channel and set the value in the newly added
      job parameter.