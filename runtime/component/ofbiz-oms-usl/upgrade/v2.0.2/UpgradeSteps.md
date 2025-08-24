## Upgrade Steps
### 1. Brokered Order Items Feed 
1. facilityGroupIds parameter
   1. A new parameter, "facilityGroupIds" is added to the Template Job of Brokered Order Items Feed and its service. This is added to add the support to generate the feed for different facility Group Ids.
   2. Add this parameter to all client specific feed Service Jobs created from the template generate_BrokeredOrderItemsFeed Service Job which already in Production by following these steps -
      1. Open the maarg instance go to System Dashboard.
      2. Click on Service Jobs, search for the client specific feed Service Jobs.
      3. Open any one serivce job and click on 'Add parameter' button.
      4. Write the name of parameter which is 'facilityGroupIds' and corresponding value if needed and add it.
      5. Alternate way could be, run SQL query to update the parameter -
           ```sql
            INSERT INTO service_job_parameter (job_name, parameter_name, parameter_value)
            VALUES ('generate_BrokeredOrderItemsFeed', 'facilityGroupIds', '')
           ```                     
      6. Ensure clearing cache if you are updating parameter though SQL.