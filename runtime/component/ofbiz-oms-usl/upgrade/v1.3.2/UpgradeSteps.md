### Returns Financial Feed
1. A new service job parameter, returnChannelEnumId is added to Template Job of Returns Financial Feed.
2. The data load with ext-upgrade type will take care of the update in the Template Job.
3. For the Feed Service Jobs already in Production specific to all Live Projects, below changes are required.
    1. New return Channel Enum Id parameter needs to be added manually.

   | Feed Name   | New Service Job Parameter | 
   |--------|---------|
   | Returns Financial Feed|returnChannelEnumId|
