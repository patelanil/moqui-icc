1. Update the instance with the data load command to load the upgrade data only.
2. Follow the "Upgrade Steps" added below.
3. Follow the client specific manual if any.
4. Check the transformations and Nifi flows if configured and requires an update.
5. Follow the "Upgrade Steps" in mantle-shopify-connector component.

## Upgrade Steps
### 1. Returns Financial Feed
1. customParametersMap job parameter
    1. A new service job parameter, customParametersMap is added to Template Job of Returns Financial Feed.
    2. This is added to support custom conditions while preparing the feed based on the fields of the feed's view.
    3. Add this parameter to all client specific feed Service Jobs created from the template generate_ReturnsFinancialFeed Service Job which already in Production.
    4. **NOTE**:  If the shop ID is being passed in the customParametersMap field to facilitate the generation of feed shop wise, then update the sendPath and receivePath fields for the ReturnsFinancialFeed System Message types. This is important since each feed will be sent to its Shopify based on ShopifyConfig/SystemMessageRemote.
        - Sample Data
        ```xml
        <!-- System Message Type for Returns Financial Feed -->
        <moqui.service.message.SystemMessageType systemMessageTypeId="ReturnsFinancialFeed"
                description="Generate HotWax Returns Financial Feed"
                parentTypeId="LocalFeedFile"
                sendPath="/home/${sftpUsername}/hotwax/financial-feed/Returns/${shopId}-${productStoreId}-ReturnsFinancialFeed-${systemMessageId}-${dateTime}.json"
                sendServiceName="co.hotwax.ofbiz.SystemMessageServices.send#SystemMessageFileSftp"
                receivePath="${contentRoot}/hotwax/FinancialFeed/Returns/${shopId}-${productStoreId}-ReturnsFinancialFeed-${dateTime}.json"/>
        ```

### 2. Fulfilled Order Items Feed
1. customParametersMap job parameter
    1. A new service job parameter, customParametersMap is added to Template Job of Fulfilled Order Items Feed.
    2. This is added to support custom conditions while preparing the feed based on the fields of the feed's view.
    3. Add this parameter to all client specific feed Service Jobs created from the template generate_FulfilledOrderItemsFeed Service Job which already in Production.
    4. **NOTE**:  If the shop ID is being passed in the customParametersMap field to facilitate the generation of feed shop wise, then update the sendPath and receivePath fields for the FulfilledOrderItemsFeed System Message types. This is important since each feed will be sent to its Shopify based on ShopifyConfig/SystemMessageRemote.
        - Sample Data
        ```xml
        <moqui.service.message.SystemMessageType systemMessageTypeId="FulfilledOrderItemsFeed"
                    description="Generate HotWax Fulfilled Order Items Feed"
                    parentTypeId="LocalFeedFile"
                    sendPath="/home/${sftpUsername}/hotwax/FulfilledOrderItems/${shopId}-${productStoreId}-FulfilledOrderItemsFeed-${systemMessageId}-${dateTime}.json"
                    sendServiceName="co.hotwax.ofbiz.SystemMessageServices.send#SystemMessageFileSftp"
                    receivePath="${contentRoot}/hotwax/FulfilledOrderItemsFeed/${shopId}-${productStoreId}-HotWaxFulfilledOrderItemsFeed-${dateTime}.json"/>
        ```

### 3. Brokered Order Items Feed
1. skipOrderAdjHistoryCreation job parameter
    1. A new service job parameter, skipOrderAdjHistoryCreation is added to Template Job of Brokered Order Items Feed.
    2. This is added to configure if history creation is required or not for the order adjustments in the feed.
    3. Add this parameter to all client specific Service Jobs created from the template generate_BrokeredOrderItemsFeed Service Job which are already in Production.

2. billTo/shipTo fields of feed JSON
    1. Added phone map to billTo and shipTo fields in the feed.
    2. Update the custom transformations of this feed if any to to send brokered order items to external system to handle the scenario where billTo or shipTo is empty.
    3. With this change, the empty billTo/shipTo field representation is changed from
        ```
        {
            "billTo":null
        }
        ```
       to
        ```
        {
            "billTo": {
                "phone": "null"
            }
        }

        ```

### 4. Poll OMS Fulfillment Feed for HotWax to Shopify Fulfillment flow
1. Refer the upgrade steps in mantle-shopify-connector regarding HotWax to Shopify Fulfillment flow.
2. This is related to consumeSmrId parameter of poll OMS Fulfillment feed job; this parameter is removed and instead SystemMessageTypeParameter is used to store this value.
3. Pause jobs and add this parameter to all client specific feed Service Jobs created from the template poll_SystemMessageFileSftp_OMSFulfillmentFeed.
4. Resume the jobs after upgrade steps and verification.

### 5. New SequenceValueItem records for history entities
1. The package names for these entities are updated.
2. Due to this, new entry will be created with updated package names for all entities in `SequenceValueItem`, generating the sequence from an initial value.
3. If the records already exist for the initial sequences i.e. for the feeds which are already in Production and using these entities for history creation, the service jobs will start returning errors for duplicate primary key when trying to create the record in respective entity.
4. So new records for the entities with new package name needs to be created in `SequenceValueItem` entity.
5. For this the value to be set for will be the sequence last set in `SequenceValueItem` with old package names.
6. Below are the steps to be followed for creating and updating records for the required entities in `SequenceValueItem`.
    1. Pause the Service Jobs scheduled for the feeds.
    2. Load below data on the instance using `Data Import` in webtools.
    This will create the SequenceValueItem for the history entities.
        ```
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.order.return.ReturnAdjustmentHistory" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.financial.FinancialReturnHistory" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.financial.FinancialOrderHistory" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.order.OrderFulfillmentHistory" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.order.ExternalFulfillmentOrderItem" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.order.OrderAdjustmentHistory" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.order.FulfilledOrderAdjustmentHistory" seqId="" />
        <moqui.entity.SequenceValueItem seqName="co.hotwax.integration.shipment.ShipmentItemHistory" seqId="" />
        ```

    3. Update the sequence number for the history entities
        1. ReturnAdjustmentHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.common.ReturnAdjustmentHistory') AS src
        )
        WHERE seq_name = 'co.hotwax.integration.order.return.ReturnAdjustmentHistory';
        ```

        2. FinancialReturnHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.common.FinancialReturnHistory') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.financial.FinancialReturnHistory';
        ```

        3. FinancialOrderHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.common.FinancialOrderHistory') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.financial.FinancialOrderHistory';
        ```

        4. OrderFulfillmentHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.integration.OrderFulfillmentHistory') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.order.OrderFulfillmentHistory';
        ```

        5. ExternalFulfillmentOrderItem
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.warehouse.ExternalFulfillmentOrderItem') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.order.ExternalFulfillmentOrderItem';
        ```

        6. OrderAdjustmentHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.common.OrderAdjustmentHistory') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.order.OrderAdjustmentHistory';
        ```

        7. FulfilledOrderAdjustmentHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.common.FulfilledOrderAdjustmentHistory') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.order.FulfilledOrderAdjustmentHistory';
        ```

        8. ShipmentItemHistory
        ```
        UPDATE sequence_value_item
        SET seq_num = (
        SELECT src.seq_num
        FROM (SELECT seq_num FROM sequence_value_item WHERE seq_name = 'co.hotwax.common.ShipmentItemHistory') AS src
        ) 
        WHERE seq_name = 'co.hotwax.integration.shipment.ShipmentItemHistory';
        ```

    4. **IMP** Clear the cache of the instance to reflect the SequenceValueItem changes.
    5. Run jobs manually if required to verify the job runs.
    6. Unpause all the scheduled service jobs according to their respective frequencies.`
