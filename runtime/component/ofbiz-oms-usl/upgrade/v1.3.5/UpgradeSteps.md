### Fulfilled Order Items Feed
1. New parameters are added to template job of Fulfilled Order Items Feed.
2. The data load for the Template Job will be done as part of ext-upgrade type data load.
3. While, for the jobs being used in production specific to all live projects, below changes are required to be done manually.
    1. Add the orderStatusId parameter.
    2. Add the includeSalesChannel parameter.
    3. Add the excludeSalesChannel parameter.

   | Feed Name   | New Service Job Parameter |
      |--------|---------|    
   | Fulfilled Order Items Feed|orderStatusId|
   | Fulfilled Order Items Feed|includeSalesChannel|
   | Fulfilled Order Items Feed|excludeSalesChannel|