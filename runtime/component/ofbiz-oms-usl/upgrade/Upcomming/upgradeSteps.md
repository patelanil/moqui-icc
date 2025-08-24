## Upgrade Steps

1. Pause the Service Jobs scheduled for the Inventory Variance Feed.
2. Load the upgrade data.
   1. Remove the productStoreId varibale from the send file name in the system message `InventoryItemVarianceFeed`.
3. Run the sql to remove the productIds parameter from the generate_InventoryVarianceFeed job and its cloned job (if any).