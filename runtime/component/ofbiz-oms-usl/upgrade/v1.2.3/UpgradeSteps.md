## Upgrade Steps
        
### Clean up obsolete Service Job Parameter

1. There are two ways to remove parameter from the Service Job.
   1. Using webtools
      1. From the webtools, click on the System -> Service Jobs.
      2. Search the Service Job name.
      3. To delete the  parameter, click on the cross sign on the right side of the parameter.

   2. Using SqlRunner
      1. Run the SQL added as part of UpgradeSQL.sql file.



3. The below Feed Jobs require the clean up.
   1. Appeasements Financial Feed
      1. New parameters added - sinceEntryDate and sinceReturnDate
      2. Removed parameter - sinceDate
         1. Job Name - **generate_AppeasementsFinancialFeed**
         2. Parameters 
            1. **sinceDate**
            
   2. Returns Financial Feed
      1. New parameters added - sinceEntryDate, sinceReturnCompletedDate and sinceReturnDate
      2. Removed parameter - sinceDate
         1. Job Name - **generate_ReturnsFinancialFeed**
         2. Parameters
            1. **sinceDate**

