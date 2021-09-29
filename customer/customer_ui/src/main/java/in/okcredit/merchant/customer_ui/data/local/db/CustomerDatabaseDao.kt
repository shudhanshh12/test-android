package `in`.okcredit.merchant.customer_ui.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CustomerDatabaseDao {

    @Query("DELETE FROM CustomerAdditionalInfo WHERE businessId = :businessId")
    suspend fun clearInfo(businessId: String)

    @Query("DELETE FROM CustomerAdditionalInfo")
    suspend fun deleteAllInfo()

    @Query("select customer_id as customerId,txnIdForCollectionTrigger as txnId from CustomerAdditionalInfo where collectionContextualEnabled = 1 and businessId = :businessId")
    suspend fun findCustomersWithContextualTrigger(businessId: String): List<CollectionTriggeredCustomers>

    @Insert
    suspend fun insertCustomerAdditionalInfo(customerAdditionalInfo: CustomerAdditionalInfo)

    @Query("select * from CustomerAdditionalInfo where customer_id  = :customerId")
    suspend fun getCustomerAdditionalInfo(customerId: String): CustomerAdditionalInfo

    @Query("UPDATE CustomerAdditionalInfo set collectionContextualEnabled = 1,txnIdForCollectionTrigger = :txnId where customer_id  = :customerId")
    suspend fun enableCollectionTrigger(customerId: String, txnId: String): Int

    @Query("UPDATE CustomerAdditionalInfo set collectionContextualEnabled = 0 where customer_id  = :customerId")
    suspend fun disableCollectionTrigger(customerId: String): Int

    @Query("UPDATE CustomerAdditionalInfo set txnCountOnPaymentIntentTrigger = :txnCount where customer_id  = :customerId")
    suspend fun setTxnCountForPaymentIntent(customerId: String, txnCount: Int): Int

    @Query("select txnCountOnPaymentIntentTrigger from CustomerAdditionalInfo where customer_id = :customerId")
    suspend fun getTxnCountForPaymentIntentEnabled(customerId: String): Int?
}
