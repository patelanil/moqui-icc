# Upgrade Steps

## HC OMS instance version
1. Check the HC instance version.
2. The minimum compatible HC version is v5.4.4
3. NOTE The changes as part of this release will work as expected if HC OMS version is updated to v5.4.4 or later version.

##  Fulfilled Order Items Feed
1. actualCarrierCode new field added in the OOTB JSON feed
    1. To support the scenarios where we have Shipping aggregators like EasyPost, AfterShip etc. the actual carrier information will be fetched from ShipmentRouteSegment.actualCarrierCode field.
    2. The Fulfilled Order Items Feed is enhanced to include this field in shipments list in the JSON. 
    3. NOTE The support of carrierPartyId is also available.
        1. carrierPartyId is from Shipment.
        2. actualCarrierCode is from ShipmentRouteSegment.
    4. Action Required
        1. Update transformation for HotWax to Shopify fulfillment. The spec should include the change to first check actualCarrierCode to be sent to Shopify, if null, carrierParty will be checked and sent.
        2. Check other transformations prepared from the Fulfilled Order Items Feed if configured and requires an update, actualCarrierCode should be used similar to the change in HotWax to Shopify fulfillment.
