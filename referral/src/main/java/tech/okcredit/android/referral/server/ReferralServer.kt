package tech.okcredit.android.referral.server

import `in`.okcredit.referral.contract.ReferralHelper
import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ReferralInfo
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils.api
import tech.okcredit.android.base.utils.ThreadUtils.worker
import tech.okcredit.android.referral.data.JourneyQualificationRequest
import tech.okcredit.android.referral.data.ReferralApiService
import tech.okcredit.android.referral.ui.rewards_on_signup.model.GetReferralTargetsApiRequest
import javax.inject.Inject

class ReferralServer @Inject constructor(
    private val referralApi: Lazy<ReferralApiService>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
) {

    fun getReferralInfo(businessId: String): Single<ReferralInfo> {
        return referralApi.get().getProfile(businessId, businessId)
            .onErrorResumeNext {
                Single.just(
                    ReferralInfo(
                        ReferralHelper.DEFAULT_REFERRAL_PRICE,
                        ReferralHelper.DEFAULT_REFERRAL_MAX_PRICE,
                        ReferralHelper.DEFAULT_STATUS
                    )
                )
            }.subscribeOn(schedulerProvider.get().io())
    }

    fun checkJourneyQualification(businessId: String): Single<Int?> {
        return referralApi.get().checkJourneyQualification(JourneyQualificationRequest(businessId), businessId).map {
            it.qualified
        }
    }

    suspend fun getTargetedUsers(businessId: String) = referralApi.get().getTargetedUsers(businessId).targetUsers

    suspend fun getShareContent(
        targetedUserId: String? = null,
        businessId: String,
    ): ReferralApiMessages.GetShareContentResponse {
        // API responds with just generic content if invalid target user id is sent
        val id = if (targetedUserId.isNullOrBlank()) {
            "null"
        } else {
            targetedUserId
        }
        return referralApi.get().getShareContent(id, businessId)
    }

    suspend fun getReferralTarget(businessId: String) =
        referralApi.get()
            .getReferralTarget(request = GetReferralTargetsApiRequest(businessId), businessId = businessId).rewards

    fun getReferralLink(businessId: String): Single<String> {
        return referralApi.get().getReferralLink(businessId)
            .subscribeOn(api())
            .observeOn(worker())
            .map { res ->
                if (res.isSuccessful) {
                    return@map res.body()?.referralLink
                } else {
                    throw Error.parse(res)
                }
            }
    }
}
