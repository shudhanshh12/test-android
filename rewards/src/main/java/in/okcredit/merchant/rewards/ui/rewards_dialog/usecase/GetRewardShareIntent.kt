package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.referral.contract.ReferralHelper
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.brodcaste_receiver.ApplicationShareReceiver
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class GetRewardShareIntent @Inject constructor(
    private val getReferralLink: Lazy<GetReferralLink>,
    private val context: Lazy<Context>,
    private val communicationApi: Lazy<CommunicationRepository>,
) {

    fun execute(amount: Long): Observable<Result<Intent>> {
        return UseCase.wrapSingle(
            shareRewardIntentBuilder(amount).flatMap { shareIntentBuilder ->
                communicationApi.get().goToWhatsApp(shareIntentBuilder)
                    .onErrorResumeNext { e ->
                        RecordException.recordException(e)
                        if (e is IntentHelper.NoWhatsAppError) {
                            getChooserIntent(shareIntentBuilder)
                        } else {
                            Single.error(e)
                        }
                    }
            }
        )
    }

    private fun getChooserIntent(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        return communicationApi.get().goToSharableApp(shareIntentBuilder)
            .flatMap { intent ->
                getPendingIntent(intent)
            }
    }

    private fun shareRewardIntentBuilder(
        amount: Long
    ): Single<ShareIntentBuilder> {
        return getReferralLink.get().execute().flatMap { referralLink ->
            Single.just(
                ShareIntentBuilder(
                    shareText = context.get().getString(
                        R.string.share_reward_text_v2,
                        TempCurrencyUtil.formatV2(amount),
                        referralLink
                    )
                )
            )
        }.onErrorReturn {
            RecordException.recordException(it)
            ShareIntentBuilder(
                shareText = context.get().getString(
                    R.string.share_reward_text_v2,
                    TempCurrencyUtil.formatV2(amount),
                    ReferralHelper.REFERRAL_LINK
                )
            )
        }
    }

    private fun getPendingIntent(intent: Intent): Single<Intent> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val pendingIntent = communicationApi.get().getApplicationShareReceiverIntent(
                intent,
                ApplicationShareReceiver.Companion.ApplicationShareTypes.REFERRAL.value,
                ""
            )
            Single.just(Intent.createChooser(intent, null, pendingIntent.intentSender))
        } else {
            Single.just(Intent.createChooser(intent, null))
        }
    }
}
