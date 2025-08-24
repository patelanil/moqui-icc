-- SQL query to remove the service job parameter from generate_BrokeredOrderItemsFeed as it is no longer needed.
delete from service_job_parameter where job_name= 'generate_BrokeredOrderItemsFeed' and parameter_name= 'parentFacilityTypeId';

-- SQL query to remove the service job parameter from generate_FulfilledOrderItemsFeed as it is no longer needed.
delete from service_job_parameter where job_name= 'generate_FulfilledOrderItemsFeed' and parameter_name= 'parentFacilityTypeId';
