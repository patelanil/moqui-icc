## 2.0.0 (2024-07-05)

### Performance (4 changes)

- [Used try with resources to enclose Entity List Iterator objects in Inventory services](HC2/plugins/ofbiz-oms-usl@705b198a320de83e623c35b0fbdebaf0707b4c9f) ([merge request](HC2/plugins/ofbiz-oms-usl!389))
- [Used try with resources to enclose Entity List Iterator objects in Order services](HC2/plugins/ofbiz-oms-usl@fa60f66834236ee0759ed739b561847e69035f0e) ([merge request](HC2/plugins/ofbiz-oms-usl!388))
- [Used try with resources to enclose Entity List Iterator objects in Shipment services](HC2/plugins/ofbiz-oms-usl@b6ea15436ee12d562a962df6456437db717a4eb5) ([merge request](HC2/plugins/ofbiz-oms-usl!383))
- [Used try with resources to enclose Entity List Iterator objects in Financial Feed services](HC2/plugins/ofbiz-oms-usl@744ddc5d6389845e107a488bc62542e751c15373) ([merge request](HC2/plugins/ofbiz-oms-usl!382))

### Added (3 change)

- [Added support of Facility Group to generate Shopify Inventory Feed](HC2/plugins/ofbiz-oms-usl@9c656278178e99203b34050ac3711f3884cde270) ([merge request](HC2/plugins/ofbiz-oms-usl!435)) 
- [Added the inventoryCountItemStatusId parameter to the InventoryCycleCountVariance service.](HC2/plugins/ofbiz-oms-usl@2a5ecaabfb4e735b05d1f68d20856eb5ef3729f8) ([merge request](HC2/plugins/ofbiz-oms-usl!438))
- [Added support of status Id to generate Returns Financial Feed](HC2/plugins/ofbiz-oms-usl@eda696d209b3802810a039a689e65362f22ba59d) ([merge request](HC2/plugins/ofbiz-oms-usl!441))
- [Check BOPIS Inventory API](HC2/plugins/ofbiz-oms-usl@54b0f76cdeb40fe1ec7d5f694eeab21828798146) ([merge request](HC2/plugins/ofbiz-oms-usl!477))
- [New Fulfilled Order Items Feed for Shopify](HC2/plugins/ofbiz-oms-usl@77f6148dc3dba8a1c704b94c65e515e785400490) ([merge request](HC2/plugins/ofbiz-oms-usl!443))
- [New Fulfilled Order Items Feed for ERP](HC2/plugins/ofbiz-oms-usl@faa46259be55f075f5aabc55b41f5c0aed8030c2) ([merge request](HC2/plugins/ofbiz-oms-usl!449))

## 1.6.12 (2024-06-25)

### Added (2 change)

- [Added API endpoints to crate/manage product stores](HC2/plugins/ofbiz-oms-usl@3953e69591e7b525a768e59cbb8161cafb0726fe) ([merge request](HC2/plugins/ofbiz-oms-usl!460))
- [Added login and user profile related endpoints](HC2/plugins/ofbiz-oms-usl@984d210dd3a9a8fe9ea9708dd1b37534b83726b7) ([merge request](HC2/plugins/ofbiz-oms-usl!478))

## 1.6.11 (2024-06-05)

### Improved (1 change)

- [Get Order API to include RETURN_RECEIVED items also in the returned order items list](HC2/plugins/ofbiz-oms-usl@db41a6231e965ab7c1429b4b7a898323cf09096f) ([merge request](HC2/plugins/ofbiz-oms-usl!475))

## 1.6.10 (2024-05-31)

### Improved (1 change)

- [Get Order API improvements](HC2/plugins/ofbiz-oms-usl@ef10a62d6b9a45c7bafb5e2515d6ed239703c645) ([merge request](HC2/plugins/ofbiz-oms-usl!472))
    - Prepare new item for order items cancelled from PredictSpring in orderProductList
    - Include unitListPrice for order items to use in get order api response

## 1.6.9 (2024-05-17)

### Fixed (1 change)

- [Get and Search Orders API to prepare return item adjustments for scenarios where return item facility ID is different than order item facility ID](HC2/plugins/ofbiz-oms-usl@ef10a62d6b9a45c7bafb5e2515d6ed239703c645) ([merge request](HC2/plugins/ofbiz-oms-usl!467))


## 1.6.8 (2024-05-08)

### Added (2 change)

- [Get Orders API](HC2/plugins/ofbiz-oms-usl@25de758842706d720b115dc4b60b04c5a389befe) ([merge request](HC2/plugins/ofbiz-oms-usl!444))

- [Search Orders API](HC2/plugins/ofbiz-oms-usl@cff9d5096dd02f0aa968a47fcfd8b4201c502d7b) ([merge request](HC2/plugins/ofbiz-oms-usl!445))

## 1.6.7 (2024-04-25)

### Added (1 change)

- [Added the inventoryCountItemStatusId parameter to the InventoryCycleCountVariance service.](HC2/plugins/ofbiz-oms-usl@2a5ecaabfb4e735b05d1f68d20856eb5ef3729f8) ([merge request](HC2/plugins/ofbiz-oms-usl!438))

## 1.6.6 (2024-04-23)

### Improved (2 changes)

- [Shopify Inventory Feed logic to fetch the inventory details for the specific...](HC2/plugins/ofbiz-oms-usl@9c656278178e99203b34050ac3711f3884cde270) ([merge request](HC2/plugins/ofbiz-oms-usl!435))

## 1.6.5 (2024-03-17)

### Added (1 changes)

- [Feature to generate Transfer Order Fulfilled Order Items feed](HC2/plugins/ofbiz-oms-usl@f7340aff8e874a1348007d32afcdcdd7a77be309) ([merge request](HC2/plugins/ofbiz-oms-usl!415))

## 1.6.3 (2024-03-12)

### Improved (1 change)

- [Inventory Item Variance feed to include facility group member details so that...](HC2/plugins/ofbiz-oms-usl@e4a67332ec82dd355394228f131ed2550bfa5853) ([merge request](HC2/plugins/ofbiz-oms-usl!418))

### Added (2 changes)

- [Generic parameter for condition map for Inventory Item Variance Feed generation](HC2/plugins/ofbiz-oms-usl@5a86d0b30cf4179bc8b1da4b06405e57b9ca6e4c) ([merge request](HC2/plugins/ofbiz-oms-usl!416))
- [Added support for orderTypeId in fulfilled orders item feed.](HC2/plugins/ofbiz-oms-usl@03d68b8c03ef8c3efd41334c3b0175e7f654a902) ([merge request](HC2/plugins/ofbiz-oms-usl!414))

## 1.6.2 (2024-03-06)

### Improved (1 change)

- [Added generic parameter for condition map for Inventory Item Variance Feed generation](HC2/plugins/ofbiz-oms-usl@5a86d0b30cf4179bc8b1da4b06405e57b9ca6e4c) ([merge request](HC2/plugins/ofbiz-oms-usl!416))

## 1.6.1 (2024-02-23)

### Added (2 changes)

- [Included actualCarrierCode field and improved handling to preparing trackingURL using actualCarrierCode in Fulfilled Order Items Feed](HC2/plugins/ofbiz-oms-usl@c7bc90d3fa5eefcc8478436745df331eb5381520) ([merge request](HC2/plugins/ofbiz-oms-usl!409))
- [Shipment attributes and product identification lists to Transfer Orders Receipt feed](HC2/plugins/ofbiz-oms-usl@707c32c523a02679ed1c411b12c9a14f892a3cc2) ([merge request](HC2/plugins/ofbiz-oms-usl!408))

## 1.6.0 (2024-01-30)

### Added (5 changes)

- [The Shop Id support in Returns Financial Feed Service](HC2/plugins/ofbiz-oms-usl@da00d8eabd621bb0a2c305fffbc1d574d16383c2) ([merge request](HC2/plugins/ofbiz-oms-usl!400))
- [Generic parameter for condition map for Fulfilled Order Items feed generation](HC2/plugins/ofbiz-oms-usl@7c24610e95291fcde242c27cce9bb8f3c9588353) ([merge request](HC2/plugins/ofbiz-oms-usl!396))
- [Generic parameter for condition map for Returns Financial feed generation](HC2/plugins/ofbiz-oms-usl@f0ad6037cac98ee4d90ce046225c0ca4d9ac8fc5) ([merge request](HC2/plugins/ofbiz-oms-usl!394))
- [Product Features list in Brokered Order Items Feed](HC2/plugins/ofbiz-oms-usl@71345037f082a01e39c79e06a9aa9e588062f266) ([merge request](HC2/plugins/ofbiz-oms-usl!379))
- [Order Attributes list in Brokered Order Items Feed](HC2/plugins/ofbiz-oms-usl@4ba32593d74bd52baae2458005aa976e8a0f3380) ([merge request](HC2/plugins/ofbiz-oms-usl!380))

### Improved (2 changes)

- [Added phone details to billTo and shipTo maps in Brokered order items feed](HC2/plugins/ofbiz-oms-usl@040cf3940e5deb8901918e414ea223fe79cfc9bf) ([merge request](HC2/plugins/ofbiz-oms-usl!344))
- [Added support to skip the Order Adjustment History creation in Brokered Order...](HC2/plugins/ofbiz-oms-usl@cb601a78aa712920f1eaf89523107d0767e86fd3) ([merge request](HC2/plugins/ofbiz-oms-usl!283))

## 1.5.2 (2024-01-03)

### Fixed (1 change)

- [Brokered Order Items Feed to generate multiple files in new transaction for...](HC2/plugins/ofbiz-oms-usl@7c810999b6fa2960adda2b1c29643c613c8c9e4a) ([merge request](HC2/plugins/ofbiz-oms-usl!391))

## 1.5.0 (2023-11-24)

### Fixed (5 changes)

- [Removed: postalContactMechId and telecomContactMechId while fetching eligible...](HC2/plugins/ofbiz-oms-mantle@b5b9e0bd73c6c06a7545905404eb20e4c53aad1f) ([merge request](HC2/plugins/ofbiz-oms-mantle!361))
- [Fetch valid records for Good Identification and Order Identification to...](HC2/plugins/ofbiz-oms-mantle@5927f14c848bd14337c9d672391a22515840708b) ([merge request](HC2/plugins/ofbiz-oms-mantle!354))
- [Error in Feed for the scenario where items are completed but no shipments in shipped status, now such items will not be included and feed will be generated without any error](HC2/plugins/ofbiz-oms-mantle@57e50147cc534a930536b372e5def73c38a7d0ce) ([merge request](HC2/plugins/ofbiz-oms-mantle!352))
- [Included the telecom Contact Mech Id when preparing the shipmentItems in the FulfilledOrderItemsFeed service](HC2/plugins/ofbiz-oms-mantle@6919987432047119730c4870d49160dd92587d8c) ([merge request](HC2/plugins/ofbiz-oms-mantle!336))

### Improved (7 changes)

- [Used receivePath of SystemMessageType to prepare the path for creating the...](HC2/plugins/ofbiz-oms-mantle@62ecabe5c3176ca920eefddb650604a73486793d) ([merge request](HC2/plugins/ofbiz-oms-mantle!348))
- [Used receivePath of SystemMessageType to prepare the path for creating the...](HC2/plugins/ofbiz-oms-mantle@d471668768818685347302d62ba87c575dd132b3) ([merge request](HC2/plugins/ofbiz-oms-mantle!345))
- [Used receivePath of SystemMessageType to prepare the path for creating the...](HC2/plugins/ofbiz-oms-mantle@01b2772e5315f2af83d14102ac9591be56674c39) ([merge request](HC2/plugins/ofbiz-oms-mantle!346))
- [Used receivePath from SystemMessageType to prepare the path for creating the...](HC2/plugins/ofbiz-oms-mantle@497015edbcbf380966f745911d7087a2616b0681) ([merge request](HC2/plugins/ofbiz-oms-mantle!342))
- [Used sendPath from SystemMessageType to prepare the path for storing the file...](HC2/plugins/ofbiz-oms-mantle@e32e226c1c69664afdeaaa3469fccccc3bd05eb2) ([merge request](HC2/plugins/ofbiz-oms-mantle!343))
- [Brokered Order Items feed to include both order Item quantity and reserved quantity](HC2/plugins/ofbiz-oms-mantle@af071ad27a13a23d29620f255e57177e55ded569) ([merge request](HC2/plugins/ofbiz-oms-mantle!351))
- [Used try with resources in send and poll system message sftp services so that...](HC2/plugins/ofbiz-oms-mantle@9a47fa15f79e229317ea37a22cf77dffa9896d37) ([merge request](HC2/plugins/ofbiz-oms-mantle!349))

## 1.3.9 (2023-11-16)

### Fixed (1 change)

* [Error in Feed for the scenario where items are completed but no shipments in shipped status, now such items will not be included and feed will be generated without any error](/HC2/plugins/ofbiz-oms-mantle/-/commit/57e50147cc534a930536b372e5def73c38a7d0ce) ([merge request](/HC2/plugins/ofbiz-oms-mantle/-/merge_requests/352))

## 1.3.8 (2023-11-01)

### Improved (1 change)

- [Used try with resources in send and poll system message sftp services so that resources are automatically closed after the try statement is complete](HC2/plugins/ofbiz-oms-mantle@9a47fa15f79e229317ea37a22cf77dffa9896d37) ([merge request](HC2/plugins/ofbiz-oms-mantle!349))


## 1.3.7 (2023-10-20)

### Fixed (1 change)

- [Fetch valid records for Good Identification and Order Identification to...](HC2/plugins/ofbiz-oms-mantle@5927f14c848bd14337c9d672391a22515840708b) ([merge request](HC2/plugins/ofbiz-oms-mantle!354))

## 1.4.0 (2023-09-20)

### Added (2 changes)

- [Support for Order with Kit Product in Fulfilled Order Items Feed by adding handling to prepare a separate shipment group in HC JSON Feed for kit product...](HC2/plugins/ofbiz-oms-mantle@7a92e8cf4bf4934afb0a22835c67512269f022cf) ([merge request](HC2/plugins/ofbiz-oms-mantle!338))
- [Support for Order with Kit Product in Brokered Order Items Feed by adding fromOrderItemAssoc and toOrderItemAssoc List in the HC Feed JSON...](HC2/plugins/ofbiz-oms-mantle@834fd5401549826d0faed22feb6759d7313932e6) ([merge request](HC2/plugins/ofbiz-oms-mantle!337))

### Improved (1 change)

- [Inventory Variance Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@45f5934d2298e67d5cb17894b82ed5f0d5dd8352) ([merge request](HC2/plugins/ofbiz-oms-mantle!180))

## 1.3.5 (2023-08-31)

### Added (4 changes)

- [System message type Id records for POS Sales channel and Ecom Sales channel...](HC2/plugins/ofbiz-oms-mantle@52b7c5bda09860f8e6ea2edd024346e836af17e9) ([merge request](HC2/plugins/ofbiz-oms-mantle!333))
- [Support of Order Status in the Fulfillment Feed to fetch the eligible orders...](HC2/plugins/ofbiz-oms-mantle@9588c73213b1469ce0aa20213eaf59d9fa7788c8) ([merge request](HC2/plugins/ofbiz-oms-mantle!309))
- [Support of order ID in the Purchase Order Receipts Feed to fetch the records...](HC2/plugins/ofbiz-oms-mantle@c24b119187b3edbfeed0521faafbc2d58d1a83c8) ([merge request](HC2/plugins/ofbiz-oms-mantle!307))
- [Feature to generate the Inventory Cycle Count Variance Feed HotWax](HC2/plugins/ofbiz-oms-mantle@7c5dec069065f4b7ff8f87166c0cf43b555794a0) ([merge request](HC2/plugins/ofbiz-oms-mantle!296))

### Improved (5 changes)

- [In the Shipments Receipt Feed history record will create for the shipment...](HC2/plugins/ofbiz-oms-mantle@47b3d50caf2cd1017974ad521c87d296e3887a83) ([merge request](HC2/plugins/ofbiz-oms-mantle!326))
- [Brokered Order Items feed to have Facility Group support. Orders can be...](HC2/plugins/ofbiz-oms-mantle@cb9f293a012cc6b04290b0f26de02900f9b3d978) ([merge request](HC2/plugins/ofbiz-oms-mantle!310))
- [Fulfilled Order Items Feed to have sales channel support. Orders can be...](HC2/plugins/ofbiz-oms-mantle@da7d5ee46a67aa123de504f1d4452e52260b062f) ([merge request](HC2/plugins/ofbiz-oms-mantle!313))
- [Fulfilled Order Items Feed to have order attributes details. These details can...](HC2/plugins/ofbiz-oms-mantle@3f25cc530948b870bc72cbc45bcaa2dd9da58331) ([merge request](HC2/plugins/ofbiz-oms-mantle!311))
- [Fulfilled Order Items Feed to have phone and email for billTo and shipTo...](HC2/plugins/ofbiz-oms-mantle@cf3b1319236b6ecd3e22f0ad229e52561ab9792f) ([merge request](HC2/plugins/ofbiz-oms-mantle!312))


## 1.3.4 (2023-08-16)

### Added (1 change)

- [Included order external Id in Brokered Order Items Feed to fetch external...](HC2/plugins/ofbiz-oms-mantle@70e6caaabde5d03c2dfd8bcd18f5d60b8c28d849) ([merge request](HC2/plugins/ofbiz-oms-mantle!332))

### Improved (6 changes)

- [Used try with resources for Returns Financial Feed old service so that...](HC2/plugins/ofbiz-oms-mantle@d4f3a8082062858a8a665e418e9e1fc678b57c0f) ([merge request](HC2/plugins/ofbiz-oms-mantle!329))
- [Used try with resources for feed generation so that resources are...](HC2/plugins/ofbiz-oms-mantle@1c5f0047b0a914a943333d2fe2596bceff7efc38) ([merge request](HC2/plugins/ofbiz-oms-mantle!315))

### Fixed (1 change)

- [Condition to fetch eligible orders when Fulfilled Order Items Feed is...](HC2/plugins/ofbiz-oms-mantle@e14df90eee37da3900cd74ab9fedcb2e48d1e957) ([merge request](HC2/plugins/ofbiz-oms-mantle!314))

## 1.3.2 (2023-07-27)

### Added (1 change)

- [Support of return Channel Enum Id in Returns Financial Feed to fetch all...](HC2/plugins/ofbiz-oms-mantle@ea8901a72fc69506b3ade8c03b23e141df8e47df) ([merge request](HC2/plugins/ofbiz-oms-mantle!308))

## 1.3.1 (2023-06-12)

### Fixed (1 change)

- [Flow to retry System Message records with SmsgProduced status having error by...](HC2/plugins/ofbiz-oms-mantle@59bf2b5cc7a52de089d726491d7792a1f2518c5a) ([merge request](HC2/plugins/ofbiz-oms-mantle!300))

## 1.3.0 (2023-06-05)

### Performance (1 change)

- [Fulfilled Order Items Feed view broken down to have separate views for Item...](HC2/plugins/ofbiz-oms-mantle@f77536852db0bab0c67e4afe672aa848ae545e22) ([merge request](HC2/plugins/ofbiz-oms-mantle!274))

### Added (3 changes)

- [Feature to generate Purchase Orders Shipments Receipt Feed](HC2/plugins/ofbiz-oms-mantle@78a5a4a95dafff349c9cf6caebc99372789e3c61) ([merge request](HC2/plugins/ofbiz-oms-mantle!294))
- [Feature to generate Shipments Receipt Feed](HC2/plugins/ofbiz-oms-mantle@591231a0b223515e005faa90364d6e85a12f0e77) ([merge request](HC2/plugins/ofbiz-oms-mantle!295))
- [Creating Fulfilled Order Adjustment History in Fulfilled Order Items Feed so...](HC2/plugins/ofbiz-oms-mantle@58b96e9842c6c259f103c1c55d39b9daa2f721ce) ([merge request](HC2/plugins/ofbiz-oms-mantle!254))
- [Poll System Message service to save file path in message text instead of file data](HC2/plugins/ofbiz-oms-mantle@a43d5fc239991efea8fb5823bb1286f716dd932a) ([merge request](HC2/plugins/ofbiz-oms-mantle!272))

### Fixed (2 changes)

- [Completed Date Time added at Return Item level instead of Return level in Returns Financial Feed](HC2/plugins/ofbiz-oms-mantle@c7561e3612a5fd782653e55c6a8eaedd0babc92f) ([merge request](HC2/plugins/ofbiz-oms-mantle!252))

## 1.2.5 (2023-05-08)

### Fixed (1 change)

- [lastRunTime parameter update while fetching inventory updates in Shopify Inventory Feed](HC2/plugins/ofbiz-oms-mantle@cb1bb009d1291ba6533782be893ec411d749bf2a) ([merge request](HC2/plugins/ofbiz-oms-mantle!287))

## 1.2.4 (2023-04-26)

### Improved (1 change)

- [Generate Brokered Order Items Feed in multiple files based on brokeredOrdersCountPerFeed parameter](HC2/plugins/ofbiz-oms-mantle@7c24656bc16666be42675a0c17ba9d38703d003f) ([merge request](HC2/plugins/ofbiz-oms-mantle!267))

## 1.2.3 (2023-04-11)

### Improved (2 changes)

- [Added Date filter on Return Date in Returns Financial Feed](HC2/plugins/ofbiz-oms-mantle@0518cefe45b92ad37ca2e1d087bfe4595717eec6) ([merge request](HC2/plugins/ofbiz-oms-mantle!263))
- [Added Date Filter on Return Date in Appeasements Financial Feed](HC2/plugins/ofbiz-oms-mantle@b05becd83b652ba10343a3408f76b3b522f12568) ([merge request](HC2/plugins/ofbiz-oms-mantle!262))

## 1.2.2 (2023-04-05)

### Improved (1 change)

- [Saving feed file path instead of file text to generate and send feed files to SFTP](HC2/plugins/ofbiz-oms-mantle@475513440bc0815c502ed950afd3a9e8687f3005) ([merge request](HC2/plugins/ofbiz-oms-mantle!255))

## 1.2.0 (2023-03-23)

### Improved (2 changes)

- [History creation of items for Fulfilled Order Items Feed for the scenario of...](HC2/plugins/ofbiz-oms-mantle@17bf74563ae3b9c67665feb102bd47aad5be0ee3) ([merge request](HC2/plugins/ofbiz-oms-mantle!233))
- [Improved the history creation of items for Brokered Items Feed for the...](HC2/plugins/ofbiz-oms-mantle@aabc3a63680f4f87cf1a6583d449cd9a1d8be70d) ([merge request](HC2/plugins/ofbiz-oms-mantle!226))

### Added (4 changes)

- [Order Payment Preference list in Brokered Order Items Feed](HC2/plugins/ofbiz-oms-mantle@290eaa3728d3e1b9a39e0fc73d75918c3f158b43) ([merge request](HC2/plugins/ofbiz-oms-mantle!229))
- [Feature to generate Fulfilled Order Items Feed order wise with each order...](HC2/plugins/ofbiz-oms-mantle@2cb96ce8697a18fb4083456a58c1f974e2aece5d) ([merge request](HC2/plugins/ofbiz-oms-mantle!196))
- [Feature to generate Brokered Order Items Feed order wise and each order...](HC2/plugins/ofbiz-oms-mantle@05d8043af2ee268bbc45d942ee1eb64d2b345810) ([merge request](HC2/plugins/ofbiz-oms-mantle!227))
- [Feature to generate Returns Financial Feed Return wise and each Return...](HC2/plugins/ofbiz-oms-mantle@a1b8d40cd9658474670adca25bf43272f38d22f6) ([merge request](HC2/plugins/ofbiz-oms-mantle!228))

## 1.1.3 (2023-02-27)

### Fixed (1 change)

- [Shopify Inventory Feed to include isPreOrder and isBackOrder fields so they...](HC2/plugins/ofbiz-oms-mantle@daa11a21a0de99524ca283505e3ebdb011506692) ([merge request](HC2/plugins/ofbiz-oms-mantle!221))

## 1.1.2 (2023-02-20)

### Fixed (2 changes)

- [Fixed the handling for fetching the Payment Preference details in Appeasements...](HC2/plugins/ofbiz-oms-mantle@2af3ee96f41b6db221a5deb59c61a913beea1003) ([merge request](HC2/plugins/ofbiz-oms-mantle!214))
- [Fixed the handling for fetching the Payment Preference details in Returns...](HC2/plugins/ofbiz-oms-mantle@ed530266695c34e3fb37ba90ce7a8730cddce680) ([merge request](HC2/plugins/ofbiz-oms-mantle!215))

## 1.1.0 (2023-02-13)

### Added (5 changes)

- [Feature to generate Inventory Delta Feed for Store Cancelled Reservations](HC2/plugins/ofbiz-oms-mantle@3b279980e0230a6785c8a0499b25ebb7b0e3b493) ([merge request](HC2/plugins/ofbiz-oms-mantle!191))
- [Order and Item Level Adjustments list in Fulfilled Order Items Feed](HC2/plugins/ofbiz-oms-mantle@a6067246aabc6d57a6e96b643c7637169699234d) ([merge request](HC2/plugins/ofbiz-oms-mantle!190))
- [Order and Item Level Adjustments list in Brokered Order Items Feed](HC2/plugins/ofbiz-oms-mantle@9f7d1439fe3fdb608f3fffe975c421a052fd5911) ([merge request](HC2/plugins/ofbiz-oms-mantle!189))
- [Return and Return Item level Adjustments list in Returns FF](HC2/plugins/ofbiz-oms-mantle@042b7eef81bdbb017af8c040321ad085fc792da6) ([merge request](HC2/plugins/ofbiz-oms-mantle!138))
- [Order and Item Level Adjustments list in Sales FF](HC2/plugins/ofbiz-oms-mantle@80f16482f5d436206a6f4dbe7c68eef52ca0681c) ([merge request](HC2/plugins/ofbiz-oms-mantle!137))

### Improved (16 changes)

- [Inventory Delta Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@f7d7e2670d53a5f6a293e32a767edd922e53d1b3) ([merge request](HC2/plugins/ofbiz-oms-mantle!176))
- [Brokered Order items Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@4eab3b9aedb2751776eddfa0e3ad92c73acab744) ([merge request](HC2/plugins/ofbiz-oms-mantle!166))
- [Fulfill Order items Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@764fdb1ca326bbeae893a32f42ee57f6bebebd3e) ([merge request](HC2/plugins/ofbiz-oms-mantle!167))
- [Appeasements Financial Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@29d91e3703db8113cd85411f13147d1eb8c73964) ([merge request](HC2/plugins/ofbiz-oms-mantle!163))
- [Returns Financial Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@1dcf997bf180baada9c7cdf2bafb7ec0449ada9b) ([merge request](HC2/plugins/ofbiz-oms-mantle!162))
- [Sales Financial Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@4c7898be52ad0516327a66d83a2caf2111a86e98) ([merge request](HC2/plugins/ofbiz-oms-mantle!159))
- [Received Returns Shipment Feed to have support for Product Store Id so that brand...](HC2/plugins/ofbiz-oms-mantle@54de84475e03b82339278e7f18772e16afa86e7e) ([merge request](HC2/plugins/ofbiz-oms-mantle!171))
- [Returns Financial Feed to contain the Return Header and Return Item level...](HC2/plugins/ofbiz-oms-mantle@da1922a010b80fe59d6a1b086d2412f51ccb608c) ([merge request](HC2/plugins/ofbiz-oms-mantle!138))
- [Inventory Variance Feed to include daysBefore parameter to generate feed for...](HC2/plugins/ofbiz-oms-mantle@092b4aee1cb320e733aa2d3d681820996becffdf) ([merge request](HC2/plugins/ofbiz-oms-mantle!148))
- [Store Fulfilled Order Items Feed to contain all Party Identifications as list...](HC2/plugins/ofbiz-oms-mantle@155ef3c082f104c71c65a926065e82088dcf7fbe) ([merge request](HC2/plugins/ofbiz-oms-mantle!134))
- [Store Fulfilled Items Feed to include history creation for the Order Items included in the feed](HC2/plugins/ofbiz-oms-mantle@d7a9081ea612c33d7bdc709adf7cc21f84f01fe4) ([merge request](HC2/plugins/ofbiz-oms-mantle!135))
- [Warehouse Brokered Order Items Feed to include history creation for the Order...](HC2/plugins/ofbiz-oms-mantle@3b058bcbd697f87542a0b494ce2855504976e9d1) ([merge request](HC2/plugins/ofbiz-oms-mantle!136))
- [Appeasements Financial Feed to contain the list of all Order Payment...](HC2/plugins/ofbiz-oms-mantle@9466f41ecb673d447438d8e1d3be440754a59b4f) ([merge request](HC2/plugins/ofbiz-oms-mantle!145))
- [Returns Financial Feed to contain the list of all Order Payment...](HC2/plugins/ofbiz-oms-mantle@81c582661e2744330b44a343ad52ef9d421796dd) ([merge request](HC2/plugins/ofbiz-oms-mantle!146))
- [Sales Financial Feed to include zero dollar completed order items](HC2/plugins/ofbiz-oms-mantle@812e0df206ba80abc6e2218d963c854e520dfca5) ([merge request](HC2/plugins/ofbiz-oms-mantle!143))
- [Improved: Brokered Order Items Feed to contain all Party Identifications as...](HC2/plugins/ofbiz-oms-mantle@f72f87cd2cb6e9e3431baea009e6e6b2a3fc8fa4) ([merge request](HC2/plugins/ofbiz-oms-mantle!108))
- [Fulfilled Order Items Feed to contain all Order Identifications as list so...](HC2/plugins/ofbiz-oms-mantle@2f2fc2d6d4a434031ebc508cdc8de16d17d93fee) ([merge request](HC2/plugins/ofbiz-oms-mantle!93))
- [Brokered Order Items Feed to contain all Order Identifications as list so that...](HC2/plugins/ofbiz-oms-mantle@1df70a2ba4beb73f9cac3811a575d3060d5251e5) ([merge request](HC2/plugins/ofbiz-oms-mantle!92))
- [Brokered Order Items Feed to include Order Item Attributes and Residential Address Flag](HC2/plugins/ofbiz-oms-mantle@65278e227f4b06a7500bda70c84c64ebe5855403) ([merge request](HC2/plugins/ofbiz-oms-mantle!182))

## 1.0.0 (2022-10-19)

### Added (9 changes)

- [Feature to generate Sales Financial Feed](HC2/plugins/ofbiz-oms-mantle@9065fcd58c8a652c0b1170d0d469d4d1b81cbe78) ([merge request](HC2/plugins/ofbiz-oms-mantle!22))
- [Feature to generate Returns Financial Feed](HC2/plugins/ofbiz-oms-mantle@d4fb9c0aac330e2cc7b8b730ef0c85057416db95) ([merge request](HC2/plugins/ofbiz-oms-mantle!23))
- [Feature to generate Appeasements Financial Feed](HC2/plugins/ofbiz-oms-mantle@bfb0c49323c15c1be0bf65ea6c9b55dcc03305de) ([merge request](HC2/plugins/ofbiz-oms-mantle!20))
- [Feature to generate Shopify Inventory Feed](HC2/plugins/ofbiz-oms-mantle@d01f254b5951dd0c5763f1f40e4fbc5c47745563) ([merge request](HC2/plugins/ofbiz-oms-mantle!10))
- [Feature to generate Received Returns Shipment Feed](HC2/plugins/ofbiz-oms-mantle@2827e06027305ecfe0f2fa3ba4046cfccfe4d85b) ([merge request](HC2/plugins/ofbiz-oms-mantle!71))
- [Feature to generate Warehouse Brokered Order Items Feed](HC2/plugins/ofbiz-oms-mantle@1eb78ec9ed32861f1f0cb47c1d0abdb450edbc9f) ([merge request](HC2/plugins/ofbiz-oms-mantle!11))
- [Feature to generate Store Fulfilled Order Items Feed](HC2/plugins/ofbiz-oms-mantle@2635409ca8fc7722378d75a261681968600c8ffa) ([merge request](HC2/plugins/ofbiz-oms-mantle!13))
- [Feature to generate Inventory Delta Feed for Store Brokered Order Items](HC2/plugins/ofbiz-oms-mantle@ffdf37847c2844c99ec374773838f838bf3d506c) ([merge request](HC2/plugins/ofbiz-oms-mantle!31))
- [Feature to generate Inventory Item Variance Feed for NOT_IN_STOCK variance reason](HC2/plugins/ofbiz-oms-mantle@2cf72993a5eeb7b748759c62d1c2210781852b84) ([merge request](HC2/plugins/ofbiz-oms-mantle!96))
