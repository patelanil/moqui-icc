# ofbiz-oms-usl

Apache OFBiz data model as per Moqui entity definition

## Set Up

For the HotWax generated Feeds with ofbiz-oms-usl, a database user for the ofbiz_transactional datasource
group is required. This user should have below permissions.
1. Write access to the below entity tables:
   1. FinancialOrderHistory (Financial_Order_History)
   2. FinancialReturnHistory (Financial_Return_History)
   3. FinancialFeedErrorHistory (Financial_Feed_Error_History)
   4. OrderAdjustmentHistory (Order_Adjustment_History)
   5. ReturnAdjustmentHistory (Return_Adjustment_History)
   6. ShipmentItemHistory (Shipment_Item_History)
   7. OrderFulfillmentErrorHistory (Order_Fulfillment_Error_History)
   8. OrderCancelHistory (Order_Cancel_History)
   9. OrderCancelErrorHistory (Order_Cancel_Error_History)
   10. ExternalFulfillmentOrderItem (External_Fulfillment_Order_Item)
   11. OrderFulfillmentHistory (Order_Fulfillment_History)
   12. FulfilledOrderAdjustmentHistory (Fulfilled_Order_Adjustment_History)
2. Read access to all the other entity tables.
3. The write permission to the selected tables is required to maintain the History in various feed generation.


### Query to grant permissions to a database user

GRANT SELECT, INSERT, UPDATE ON <database_name>.<entity_name> TO 'username'@'%';

### Query to check permissions granted to a database user

SHOW GRANTS FOR 'username'@'%';

### Sample output for permissions granted to a database user

Here the database is smcanadauat and the user is smcanadauat-custom.

```
Grants for smcanadauat-custom@%
GRANT USAGE ON *.* TO 'smcanadauat-custom'@'%'
GRANT SELECT ON smcanadauat.* TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.order_fulfillment_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.shipment_item_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.order_fulfillment_error_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.financial_order_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.order_cancel_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.order_cancel_error_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.financial_return_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.financial_feed_error_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.return_adjustment_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.order_adjustment_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.external_fulfillment_order_item TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.order_fulfillment_history TO 'smcanadauat-custom'@'%'
GRANT SELECT, INSERT, UPDATE ON smcanadauat.fulfilled_order_adjustment_history TO 'smcanadauat-custom'@'%'
```

## HC OMS release compatibility

This section contains the information for the minimum compatible version of HC OMS release for theofbiz-oms-usl release.

TODO identify and update for earlier versions. 
NOTE Before updating maarg instance, identify the minimum compatible HC OMS release version esp. database changes.

   | ofbiz-oms-usl release | HC OMS release | Comments |
   | --- | --- | --- |
   | v1.5.3                |  | HotFix branch created from v1.5.2 for new deployment pattern change |
   | v1.6.1                | v5.4.4 | |
   | v1.6.2                | v5.4.4 | HotFix branch created from v1.6.1 for inventory variance feed custom parameters map |
   | v1.6.3                | v5.4.4 | |
   | v1.6.4                |  | HotFix branch created from v1.6.0 for new deployment pattern change |
   | v1.6.5                | v5.4.4 | |
   | v1.6.6                | v5.5.0 | |
   | v1.6.7                | v5.5.0 | |
   | v1.6.8                | 5.8.10 | HotFix branch created from v1.6.7 for Get and Search Orders features for PredictSpring |
   | v1.6.9                | 5.8.10 | HotFix branch created from v1.6.8 for return item adjustment view fix for facilty ID |
   | v1.6.12               | 5.8.10 | HotFix branch created from v1.6.11 for admin.rest.xml |
   | v1.7.0                | 5.8.10 | |
