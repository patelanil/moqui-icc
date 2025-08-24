## Check Bopis Inventory API

### End Point

URL: https://<maargBaseUrl>/rest/s1/ofbiz-oms-usl/checkBopisInventory

### Headers
Content-Type: application/json

### Request Body fields

1. productStoreId - The internal ID of the product store in HC OMS.
2. productIds - The list of product IDs. This is the internal ID in HC OMS.
3. internalNames - The list of internal names of the products in HC OMS.
4. facilityIds - The list of facility IDs. This is the internal ID in HC OMS.
5. inventoryGroupId - The ID of the Facility Group in HC OMS. This refers to the Channel for which inventory needs to be fetched.

**NOTE** One of productIds or internalNames is required for the API. All other fields are mandatory.

### Sample Request

1. Single Product and Facility using productId

Request Body
```json
{
    "productStoreId": "STORE",
    "productIds": ["10001"],
    "facilityIds": ["FAC_1"],
    "inventoryGroupId": "FAC_GRP"
}
```

Response
```json
{
    "resultList": [
        {
            "facilityId": "FAC_1",
            "safetyStock": 0.0,
            "productId": "10001",
            "computedAtp": 431,
            "atp": 431,
            "internalName": "11924714-700"
        }
    ]
}
```

2. Single Product and Facility using internalName

Request Body
```json
{
    "productStoreId": "STORE",
    "internalNames": ["11924714-700"],
    "facilityIds": ["FAC_1"],
    "inventoryGroupId": "FAC_GRP"
}
```

Response
```json
{
    "resultList": [
        {
            "facilityId": "FAC_1",
            "safetyStock": 0.0,
            "productId": "10001",
            "computedAtp": 431,
            "atp": 431,
            "internalName": "11924714-700"
        }
    ]
}
```

3. Multiple Products and Facilities using productId

Request Body
```json
{
    "productStoreId": "STORE",
    "productIds": ["10001", "10002"],
    "facilityIds": ["FAC_1", "FAC_2"],
    "inventoryGroupId": "FAC_GRP"
}
```

Response
```json
{
    "resultList": [
        {
            "facilityId": "FAC_1",
            "safetyStock": 0.0,
            "productId": "10001",
            "computedAtp": 431,
            "atp": 431,
            "internalName": "11924714-700"
        },
        {
            "decisionReasonDesc": "The facility FAC_2 does not have PickUp enabled. Add it to the PickUp facility group to enable pickup.",
            "facilityId": "FAC_2",
            "safetyStock": 0.0,
            "productId": "10001",
            "computedAtp": 0.0,
            "atp": 0.0,
            "decisionReason": "PickUpFacility",
            "internalName": "11924714-700"
        },
        {
            "decisionReasonDesc": "The facility FAC_1 where the product inventory is located is disabled for catering to BOPIS orders.",
            "facilityId": "FAC_1",
            "safetyStock": 0.0,
            "productId": "10002",
            "computedAtp": 0.0,
            "atp": 0.0,
            "decisionReason": "AllowPickupFacility",
            "internalName": "11924714-718"
        },
        {
            "decisionReasonDesc": "The facility FAC_2 does not have PickUp enabled. Add it to the PickUp facility group to enable pickup.",
            "facilityId": "FAC_2",
            "safetyStock": 0.0,
            "productId": "10002",
            "computedAtp": 0.0,
            "atp": 0.0,
            "decisionReason": "PickUpFacility",
            "internalName": "11924714-718"
        }
    ]
}
```

4. Multiple Products and Facilities using internalName

Request Body
```json
{
    "productStoreId": "STORE",
    "internalNames": ["11924714-700", "11924714-718"],
    "facilityIds": ["FAC_1", "FAC_2"],
    "inventoryGroupId": "FAC_GRP"
}
```

Response
```json
{
    "resultList": [
        {
            "facilityId": "FAC_1",
            "safetyStock": 0.0,
            "productId": "10001",
            "computedAtp": 431,
            "atp": 431,
            "internalName": "11924714-700"
        },
        {
            "decisionReasonDesc": "The facility FAC_2 does not have PickUp enabled. Add it to the PickUp facility group to enable pickup.",
            "facilityId": "FAC_2",
            "safetyStock": 0.0,
            "productId": "10001",
            "computedAtp": 0.0,
            "atp": 0.0,
            "decisionReason": "PickUpFacility",
            "internalName": "11924714-700"
        },
        {
            "decisionReasonDesc": "The facility FAC_1 where the product inventory is located is disabled for catering to BOPIS orders.",
            "facilityId": "FAC_1",
            "safetyStock": 0.0,
            "productId": "10002",
            "computedAtp": 0.0,
            "atp": 0.0,
            "decisionReason": "AllowPickupFacility",
            "internalName": "11924714-718"
        },
        {
            "decisionReasonDesc": "The facility FAC_2 does not have PickUp enabled. Add it to the PickUp facility group to enable pickup.",
            "facilityId": "FAC_2",
            "safetyStock": 0.0,
            "productId": "10002",
            "computedAtp": 0.0,
            "atp": 0.0,
            "decisionReason": "PickUpFacility",
            "internalName": "11924714-718"
        }
    ]
}
```