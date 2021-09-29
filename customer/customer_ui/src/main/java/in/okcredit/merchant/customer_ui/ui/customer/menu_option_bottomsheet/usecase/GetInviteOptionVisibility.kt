package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.usecase

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import tech.okcredit.android.referral.ui.referral_target_user_list.usecase.GetTargetUsers
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import javax.inject.Inject

class GetInviteOptionVisibility @Inject constructor(
    private val getTargetedUsers: Lazy<GetTargetUsers>,
    private val getReferralVersionImpl: Lazy<GetReferralVersionImpl>
) {
    fun execute(
        customer: Customer?
    ): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            Observable.combineLatest(
                getTargetedUserVersionPresent(), getTargetUserFound(customer).toObservable(),
                BiFunction { isTargetedUserVersionPresent: Boolean, isTargetUserFound: Boolean ->
                    return@BiFunction isTargetedUserVersionPresent && isTargetUserFound
                }
            )
        )
    }

    private fun getTargetUserFound(customer: Customer?): Single<Boolean> {
        return getTargetedUsers.get().execute().map {
            var found = false
            it.map {
                if (!found && customer != null)
                    found = customer.mobile == it.phoneNumber
            }
            found
        }
    }

    private fun getTargetedUserVersionPresent(): Observable<Boolean> {
        return getReferralVersionImpl.get().execute().map {
            return@map it == ReferralVersion.TARGETED_REFERRAL || it == ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION
        }
    }
}
