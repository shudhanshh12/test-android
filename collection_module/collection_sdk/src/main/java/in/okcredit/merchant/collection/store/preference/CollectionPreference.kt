package `in`.okcredit.merchant.collection.store.preference

import `in`.okcredit.merchant.collection.store.CollectionLocalSourceImpl
import `in`.okcredit.merchant.contract.GetBusinessIdList
import android.content.Context
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class CollectionPreference @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        const val VERSION = 1
        private const val SHARED_PREF_NAME = "CollectionPref"

        const val TARGETED_REFERRAL_EDUCATION_SHOWN = "targeted_referral_education_shown"
        const val PREF_CASHBACK_BANNER_CLOSED_IDS = "cash_back_banner_closed_ids"
    }

    class Migrations @Inject constructor(
        private val getBusinessIdList: Lazy<GetBusinessIdList>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            SharedPreferencesMigration.changeKeyListScopeFromIndividualToBusinessScope(
                prefs,
                listOf(
                    CollectionLocalSourceImpl.KEY_COLLECTION_LAST_SYNC_EVERYTHING_TIME,
                    CollectionLocalSourceImpl.KEY_LAST_SYNC_CUSTOMER_COLLECTIONS,
                    CollectionLocalSourceImpl.KEY_LAST_SYNC_SUPPLIER_COLLECTIONS
                ),
                getBusinessIdList.get().execute().first()
            )
        }
    }

    fun clearCollectionPref(): Completable {
        return rxCompletable { clear() }
            .subscribeOn(Schedulers.io())
    }

    fun setCashbackBannerClosed(customerId: String, businessId: String): Completable {
        return rxCompletable {
            val value = getString(PREF_CASHBACK_BANNER_CLOSED_IDS, Scope.Business(businessId), "").first()
            set(PREF_CASHBACK_BANNER_CLOSED_IDS, value.plus(",$customerId"), Scope.Business(businessId))
        }
    }

    fun getCashbackBannerClosed(customerId: String, businessId: String): Single<Boolean> {
        return getString(PREF_CASHBACK_BANNER_CLOSED_IDS, Scope.Business(businessId), "").asObservable()
            .map {
                it.contains(customerId)
            }.firstOrError()
    }
}
