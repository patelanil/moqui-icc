
-- SQL queries to clean up obsolete System Message Type and Service Job data
delete from system_message_type where system_message_type_id='StoreFulfilledItemsFeed';
delete from service_job_parameter where job_name='generate_StoreFulfilledItemsFeed';
delete from service_job where job_name='generate_StoreFulfilledItemsFeed';

delete from system_message_type where system_message_type_id='StoreInventoryDeltaFeed';
delete from service_job_parameter where job_name='generate_StoreInventoryDeltaFeed';
delete from service_job where job_name='generate_StoreInventoryDeltaFeed';

delete from system_message_type where system_message_type_id='InventoryVarianceFeed';
delete from service_job_parameter where job_name='generate_DoNotHaveInventoryVarianceFeed';
delete from service_job where job_name='generate_DoNotHaveInventoryVarianceFeed';

delete from system_message_type where system_message_type_id='BrokeredOrderItemsFeed';
delete from service_job_parameter where job_name='generate_WHBrokeredOrderItemsFeed';
delete from service_job where job_name='generate_WHBrokeredOrderItemsFeed';

-- SQL query to remove the service job parameters from generate_ShopifyInventoryFeedFromHotWax as they are no longer needed.
delete from service_job_parameter where job_name='generate_ShopifyInventoryFeedFromHotWax' and parameter_name='includeTurnedOffFacility';
delete from service_job_parameter where job_name='generate_ShopifyInventoryFeedFromHotWax' and parameter_name='productStoreId';
