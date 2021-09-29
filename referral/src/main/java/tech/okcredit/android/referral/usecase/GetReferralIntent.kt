package tech.okcredit.android.referral.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralHelper
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.models.ShareContent
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.utils.FileUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.brodcaste_receiver.ApplicationShareReceiver
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class GetReferralIntent @Inject constructor(
    private val getReferralLink: Lazy<GetReferralLink>,
    private val referralRepository: Lazy<ReferralRepository>,
    private val context: Lazy<Context>,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun getShareIntent(mobileNumber: String? = null): Observable<Result<IntentWithShareText>> {
        return UseCase.wrapSingle(
            referralIntentBuilder(mobileNumber = mobileNumber).flatMap { shareReferralIntent ->
                val shareText = shareReferralIntent.shareText
                getChooserIntent(shareReferralIntent.shareIntentBuilder).map {
                    IntentWithShareText(it, shareText)
                }
            }
        )
    }

    data class IntentWithShareText(
        val intent: Intent,
        val shareText: String = ""
    )

    private fun getChooserIntent(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        return communicationApi.get().goToSharableApp(shareIntentBuilder)
            .flatMap { intent ->
                getPendingIntent(intent)
            }
    }

    fun getWhatsAppIntent(targetedUserId: String = "", mobileNumber: String? = null): Observable<Result<Intent>> {
        return UseCase.wrapSingle(
            referralIntentBuilder(targetedUserId, mobileNumber).flatMap { responseShareReferralIntent ->
                communicationApi.get().goToWhatsApp(responseShareReferralIntent.shareIntentBuilder)
                    .onErrorResumeNext { e ->
                        if (e is IntentHelper.NoWhatsAppError) {
                            return@onErrorResumeNext getChooserIntent(responseShareReferralIntent.shareIntentBuilder)
                        } else {
                            return@onErrorResumeNext Single.error(e)
                        }
                    }
            }
        )
    }

    private fun referralIntentBuilder(
        targetedUserId: String? = null,
        mobileNumber: String? = null
    ): Single<ResponseShareReferralIntent> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            getReferralLink.get().execute().flatMap { referralLink ->
                rxSingle { referralRepository.get().getShareContent(targetedUserId, businessId) }.flatMap { content ->
                    if (targetedUserId.isNotNullOrBlank() && content.targetContent != null) {
                        Single.just(
                            ResponseShareReferralIntent(
                                newShareIntentBuilder(
                                    content.targetContent!!,
                                    referralLink,
                                    "$targetedUserId.jpg",
                                    mobileNumber
                                ),
                                "${content.targetContent!!.text} $referralLink"
                            )
                        )
                    } else {
                        Single.just(
                            ResponseShareReferralIntent(
                                newShareIntentBuilder(
                                    content.genericContent,
                                    referralLink,
                                    ReferralHelper.LOCAL_FILE_NAME,
                                    mobileNumber
                                ),
                                "${content.genericContent.text} $referralLink"
                            )
                        )
                    }
                }
            }
        }
    }

    data class ResponseShareReferralIntent(
        val shareIntentBuilder: ShareIntentBuilder,
        val shareText: String
    )

    private fun newShareIntentBuilder(
        content: ShareContent,
        referralLink: String,
        localFileName: String,
        mobileNumber: String? = null
    ): ShareIntentBuilder {
        val localFile = FileUtils.getLocalFile(
            context.get(),
            ReferralHelper.LOCAL_FOLDER_NAME, localFileName
        )
        return ShareIntentBuilder(
            shareText = content.text + " " + referralLink,
            imageFrom = ImagePath.ImageUriFromRemote(
                file = localFile,
                localFolderName = ReferralHelper.LOCAL_FOLDER_NAME,
                localFileName = localFileName,
                fileUrl = content.imageUrl
            ),
            phoneNumber = mobileNumber
        )
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
