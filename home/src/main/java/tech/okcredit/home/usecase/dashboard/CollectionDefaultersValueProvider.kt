package tech.okcredit.home.usecase.dashboard

import `in`.okcredit.backend._offline.usecase.GetDefaulterCustomerList
import `in`.okcredit.backend.contract.Customer
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.home.R
import tech.okcredit.home.utils.UriUtils.replaceLastSegmentWithValue
import javax.inject.Inject
import kotlin.math.min

class CollectionDefaultersValueProvider @Inject constructor(
    private val context: Lazy<Context>,
    private val getDefaulterCustomerList: Lazy<GetDefaulterCustomerList>
) : DashboardValueProvider {

    companion object {
        const val COLLECTION_DEFAULTERS = "collection_defaulters"
        const val DEFAULT_NUMBER_OF_DEFAULTERS = 4
    }

    override fun getValue(request: DashboardValueProvider.Request?): Observable<DashboardValueProvider.Response> {
        return getDefaulterCustomerList.get().execute()
            .map {
                val defaulterList = it.asDefaulterList()
                CollectionDefaultersDashboardValue(
                    defaulters = defaulterList.subList(request?.input ?: DEFAULT_NUMBER_OF_DEFAULTERS),
                    string = getCtaLabelAppendString(defaulterList)
                )
            }
    }

    private fun List<Customer>.asDefaulterList(): List<Defaulter> {
        return this.map {
            val deeplink = context.get().getString(R.string.customer_profile_dialog_deeplink)
                .replaceLastSegmentWithValue(it.id)
            Defaulter(
                id = it.id,
                description = getFirstNameOrSubstring(it.description),
                balance = it.balanceV2,
                profileImage = it.profileImage,
                onClickDeeplink = deeplink
            )
        }
    }

    private fun getFirstNameOrSubstring(description: String?): String {
        return try {
            description?.trim()?.let { it.split(" ")[0] } ?: ""
        } catch (e: Exception) {
            RecordException.recordException(e)
            description ?: ""
        }
    }

    private fun List<Defaulter>.subList(maxSize: Int): List<Defaulter> = this.subList(0, min(this.size, maxSize))

    private fun getCtaLabelAppendString(defaulterList: List<Defaulter>): String {
        return context.get().getString(R.string.dashboard_collection_defaulters_title_append, defaulterList.size)
    }

    data class CollectionDefaultersDashboardValue(
        override val exclude: Boolean = false,
        val defaulters: List<Defaulter>? = null,
        val string: String? = null
    ) : DashboardValueProvider.Response(exclude)

    data class Defaulter(
        val id: String,
        val description: String,
        val balance: Long,
        val profileImage: String? = null,
        val onClickDeeplink: String? = null
    )
}
