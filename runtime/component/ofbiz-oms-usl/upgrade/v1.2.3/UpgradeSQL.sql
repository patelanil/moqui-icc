-- SQL query to remove the service job parameter from generate_AppeasementsFinancialFeed as it is no longer needed.
delete from service_job_parameter where job_name= 'generate_AppeasementsFinancialFeed' and parameter_name= 'sinceDate';

-- SQL query to remove the service job parameter from generate_ReturnsFinancialFeed as it is no longer needed.
delete from service_job_parameter where job_name= 'generate_ReturnsFinancialFeed' and parameter_name= 'sinceDate';