package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class GetCollectionStaffLinkScreen @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    suspend fun execute(): StaffLinkScreen {
        // check if there is an active staff link present
        val businessId = getActiveBusinessId.get().execute().await()
        val activeDetail = customerRepositoryImpl.get().activeStaffLinkDetails(businessId)
        if (!activeDetail.linkId.isNullOrEmpty() &&
            !activeDetail.link.isNullOrEmpty() &&
            !activeDetail.accountIds.isNullOrEmpty()
        ) {
            customerRepositoryImpl.get().setStaffLinkEducation(true)
            return StaffLinkScreen.ActiveStaffLink(
                activeDetail.linkId,
                activeDetail.link,
                activeDetail.accountIds,
                activeDetail.createTime?.times(1000)
            )
        }

        // check if we should show the education, ie user is coming to this screen for the first time
        if (!customerRepositoryImpl.get().staffLinkEducationShown()) {
            customerRepositoryImpl.get().setStaffLinkEducation(true)
            return StaffLinkScreen.Education
        }

        // normal flow of directly moving to select customer screen
        return StaffLinkScreen.SelectCustomer
    }

    sealed class StaffLinkScreen {
        object Education : StaffLinkScreen()
        object SelectCustomer : StaffLinkScreen()
        data class ActiveStaffLink(
            val linkId: String,
            val link: String,
            val customerIds: List<String>,
            val createTime: Long?,
        ) : StaffLinkScreen()
    }
}
