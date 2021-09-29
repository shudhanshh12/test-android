package tech.okcredit.android.referral.share.usecase

import `in`.okcredit.referral.contract.ReferralHelper
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.referral.contract.usecase.GetShareAppIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.utils.FileUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.brodcaste_receiver.ApplicationShareReceiver
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import javax.inject.Inject

class GetShareAppIntentImpl @Inject constructor(
    private val context: Lazy<Context>,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val getReferralLink: Lazy<GetReferralLink>,
    private val referralVersionImpl: Lazy<GetReferralVersionImpl>
) : GetShareAppIntent {

    override fun execute(): Single<Intent> {
        return getReferralLink.get().execute().flatMap { referralLink ->
            val shareIntentBuilder = getShareIntentBuilder(referralLink)
            return@flatMap getShareAppIntent(shareIntentBuilder)
        }
    }

    private fun getShareIntentBuilder(referralLink: String): ShareIntentBuilder {
        val referralText = context.get().getString(R.string.share_app_msg, referralLink)
        val referralImage = FileUtils.getLocalFile(
            context.get(),
            ReferralHelper.LOCAL_FOLDER_NAME,
            referralVersionImpl.get().getShareAppImageName()
        )
        return ShareIntentBuilder(
            shareText = referralText,
            imageFrom = ImagePath.ImageUriFromRemote(
                file = referralImage,
                localFolderName = ReferralHelper.LOCAL_FOLDER_NAME,
                fileUrl = referralVersionImpl.get().getShareAppImagePath(),
                localFileName = referralVersionImpl.get().getShareAppImageName()
            )
        )
    }

    private fun getShareAppIntent(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        return communicationApi.get().goToWhatsApp(shareIntentBuilder).flatMap { getPendingIntent(it) }
            .onErrorResumeNext { throwable ->
                if (throwable is IntentHelper.NoWhatsAppError) {
                    return@onErrorResumeNext communicationApi.get().goToSharableApp(shareIntentBuilder)
                        .flatMap { intent -> getPendingIntent(intent) }
                } else {
                    return@onErrorResumeNext Single.error(throwable)
                }
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
