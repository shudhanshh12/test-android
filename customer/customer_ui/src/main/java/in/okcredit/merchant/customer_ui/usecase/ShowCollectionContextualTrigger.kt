package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.asObservable
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject

class ShowCollectionContextualTrigger @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
    private val collectionActivationStatus: Lazy<GetCollectionActivationStatus>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerId: String) = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        combine(
            abRepository.get().isFeatureEnabled(FEATURE_COLLECTION_CONTEXTUAL_TRIGGER).asFlow().take(1),
            collectionActivationStatus.get().execute().asFlow().distinctUntilChanged(),
            transactionRepo.get().listTransactions(customerId, businessId).asFlow()
                .distinctUntilChanged { old, new -> old.size == new.size }
        ) { featureEnabled, activated, transactions ->
            checkForContextualMessaging(customerId, featureEnabled, activated, transactions, businessId)
        }.asObservable()
    }

    private suspend fun checkForContextualMessaging(
        customerId: String,
        featureEnabled: Boolean,
        activated: Boolean,
        transactions: List<Transaction>,
        businessId: String
    ): Pair<CollectionTriggerVariant, String> {
        if (!featureEnabled || transactions.isEmpty()) return CollectionTriggerVariant.NONE to ""
        val lastTransaction = transactions.maxByOrNull { it.createdAt.millis }!!
        val matchedKeyword = checkIfNoteContainsCollectionKeywords(lastTransaction.note)
        val showTrigger = !matchedKeyword.isNullOrEmpty() &&
            checkForCustomerCountAndLastTriggered(customerId, lastTransaction.id, businessId)

        if (!showTrigger) {
            return CollectionTriggerVariant.NONE to ""
        }

        return if (activated) {
            if (lastTransaction.type == Transaction.CREDIT) {
                CollectionTriggerVariant.COLLECT_CREDIT_ONLINE to (matchedKeyword ?: "")
            } else {
                CollectionTriggerVariant.COLLECT_PAYMENT_ONLINE to (matchedKeyword ?: "")
            }
        } else {
            if (lastTransaction.type == Transaction.CREDIT) {
                CollectionTriggerVariant.SETUP_CREDIT_COLLECTION to (matchedKeyword ?: "")
            } else {
                CollectionTriggerVariant.SETUP_PAYMENT_COLLECTION to (matchedKeyword ?: "")
            }
        }
    }

    private suspend fun checkForCustomerCountAndLastTriggered(
        customerId: String,
        txnId: String,
        businessId: String
    ): Boolean {
        // if not customer added till now then add customer and return true
        val existingCustomerTriggerList = customerRepositoryImpl.get().getCustomerWithCollectionContextualMessage(businessId)
        if (existingCustomerTriggerList.isNullOrEmpty()) {
            customerRepositoryImpl.get().setLastContextualTriggerTimestamp(DateTimeUtils.currentDateTime().millis)
            customerRepositoryImpl.get().setCustomerWithCollectionContextualMessage(customerId, txnId, businessId)
            return true
        }

        // if customer enabled once for customer then check if this is the same transaction that was created before
        val exitingCustomer = existingCustomerTriggerList.find { it.customerId == customerId }
        if (exitingCustomer != null) {
            return exitingCustomer.txnId == txnId
        }

        // if map has less than 5 customers then check for last trigger and add new one if last one is not on same day
        if (existingCustomerTriggerList.size < MAX_CUSTOMER_COUNT) {
            val lastTrigger = customerRepositoryImpl.get().getLastContextualTriggerTimestamp().first()
            if (lastTrigger > 0 && !DateTimeUtils.isCurrentDate(DateTime(lastTrigger))) {
                customerRepositoryImpl.get().setLastContextualTriggerTimestamp(DateTimeUtils.currentDateTime().millis)
                customerRepositoryImpl.get().setCustomerWithCollectionContextualMessage(customerId, txnId, businessId)
                return true
            }
        }

        return false
    }

    private fun checkIfNoteContainsCollectionKeywords(notes: String?): String? {
        if (notes.isNullOrEmpty()) return null

        val keywords = firebaseRemoteConfig.get().getString(CONTEXTUAL_NOTE_KEYWORDS).split(",")
        keywords.forEach {
            val keywordPresent = notes.contains(it, true)
            if (keywordPresent) {
                return it
            }
        }
        return null
    }

    companion object {
        const val MAX_CUSTOMER_COUNT = 5
        const val CONTEXTUAL_NOTE_KEYWORDS = "contextual_note_keywords"
        const val FEATURE_COLLECTION_CONTEXTUAL_TRIGGER = "collection_contextual_trigger"
    }
}

enum class CollectionTriggerVariant(val value: Int) {
    SETUP_CREDIT_COLLECTION(0),
    COLLECT_CREDIT_ONLINE(1),
    SETUP_PAYMENT_COLLECTION(2),
    COLLECT_PAYMENT_ONLINE(3),
    NONE(-1)
}
