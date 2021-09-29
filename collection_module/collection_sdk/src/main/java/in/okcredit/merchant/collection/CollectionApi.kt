package `in`.okcredit.merchant.collection

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionMerchantProfile

data class CollectionProfiles(
    val collectionMerchantProfile: CollectionMerchantProfile,
    val collectionCustomerProfiles: List<CollectionCustomerProfile>,
    val supplierCollectionProfiles: List<CollectionCustomerProfile>,
)
