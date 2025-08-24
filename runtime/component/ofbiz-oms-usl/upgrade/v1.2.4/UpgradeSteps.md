## Upgrade Steps

### Update in Feed Template Jobs

1. The Template Jobs for Feeds are updated with below changes:
   1. Mapped with new Service Name added to create multiple feed files
   2. New parameter introduced for limiting the number of records in the feed files
2. The data load with ext-upgrade type will take care of the update in the Template Jobs.
3. For the Feed Service Jobs already in Production specific to all Live Projects, below changes are required
   1. The serviceName parameter needs to updated manually
   2. New limit parameter needs to be added manually

   Feed Name | New Service Name | limit Parameter Name
   --- | --- | ---
   Brokered Order Items Feed | co.hotwax.ofbiz.OrderServices.get#BrokeredOrders | brokeredOrdersCountPerFeed
