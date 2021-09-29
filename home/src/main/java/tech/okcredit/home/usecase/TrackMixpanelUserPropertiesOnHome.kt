package tech.okcredit.home.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.analytics.UserProperties.MERCHANT_CATEGORY
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusiness
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.home.BuildConfig
import timber.log.Timber
import javax.inject.Inject

class TrackMixpanelUserPropertiesOnHome @Inject constructor(
    private val getActiveBusiness: GetActiveBusiness,
    private val customerRepo: CustomerRepo,
    private val tracker: Tracker,
    private val localeManager: Lazy<LocaleManager>
) {

    fun execute(): Completable {
        Timber.d("TrackMixpanelUserPropertiesOnHome STARTED")

        return getActiveBusiness.execute().map { business ->
            customerRepo.listActiveCustomers(business.id).map { customers ->
                val totalTxnCount = customers.sumBy { if (it.lastActivity == null) 0 else it.transactionCount.toInt() }
                val totalCustomerCount = customers.size

                tracker.setUserProperties(
                    business.id,
                    if (business.isNameSet()) business.name else null,
                    localeManager.get().getLanguage(),
                    LocaleManager.getDeviceLanguage()
                )

                if (!business.category?.name.isNullOrEmpty()) {
                    tracker.setUserProperty(MERCHANT_CATEGORY, business.category?.name!!)
                }

                tracker.setVersionSuperProperty(BuildConfig.VERSION_NAME)
                tracker.registerSuperPropertiesForCustomersAndTransactionsCount(totalTxnCount, totalCustomerCount)
                ExceptionUtils.setUserIdentifier(business.id)
            }
        }.firstOrError().ignoreElement()
    }
}
