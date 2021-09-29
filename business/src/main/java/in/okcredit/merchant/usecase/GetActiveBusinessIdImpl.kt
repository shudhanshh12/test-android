package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.store.sharedprefs.BusinessPreferences
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

@Reusable
class GetActiveBusinessIdImpl @Inject constructor(
    private val preferences: Lazy<BusinessPreferences>,
) : GetActiveBusinessId {

    companion object {
        private var businessIdCache: BehaviorSubject<String>? = null
        const val DEFAULT_BUSINESS_ID = "default_business_id"

        internal fun clearCache() {
            businessIdCache = null
        }

        // lock is used with synchronized {} to prevent multiple threads calling setupCache() simultaneously
        private val lock = Any()
    }

    /**
     * Returns default business id if user is logged in. If user is not logged in, returns a blank string
     */
    override fun execute(): Single<String> {
        if (businessIdCache == null) { // Cache miss
            synchronized(lock) {
                if (businessIdCache == null) { // Double check
                    setupCache()
                }
            }
        }
        return businessIdCache!!.firstOrError()
    }

    private fun setupCache() {
        businessIdCache = BehaviorSubject.create()
        getActiveBusinessIdFromPrefs()
            .subscribe(businessIdCache!!)
    }

    private fun getActiveBusinessIdFromPrefs(): Observable<String> {
        return preferences.get().getString(DEFAULT_BUSINESS_ID, Scope.Individual).asObservable()
    }

    /**
     * [thisOrActiveBusinessId] returns @param businessId if it is not null or else returns the default business id
     */
    override fun thisOrActiveBusinessId(businessId: String?): Single<String> {
        return if (businessId != null) {
            Single.just(businessId)
        } else {
            execute()
        }
    }
}
