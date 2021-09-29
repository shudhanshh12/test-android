package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class GetQrScreenEducation @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val collectionRepository: CollectionRepository,
) : UseCase<Unit, GetQrScreenEducation.Response> {

    companion object {
        const val EXPT_NAME = "postlogin_android-all-qr_first_education"
        const val VARIANT_TAP_TARGET = "tap_and_target"
        const val VARIANT_TOOLTIP = "tool_tip"
        const val SHOW_QR_MENU_EDUCATION = "show_qr_menu_education"
        const val SHOW_QR_ONLINE_COLLECTION_EDUCATION = "show_qr_online_collection_education"
        const val SHOW_QR_SAVE_SEND_EDUCATION = "show_qr_save_send_education"
    }

    data class Response(val education: QrCodeContract.Education, val educationType: QrCodeContract.EducationType)

    override fun execute(req: Unit): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            canShowQrEducation()
                .flatMapObservable {
                    if (it) {
                        isExpEnabled()
                            .flatMap {
                                if (it) {
                                    getVariant()
                                } else {
                                    Observable.just(getDefault())
                                }
                            }.doOnNext {
                                markShown(it.education)
                            }
                    } else {
                        Observable.just(getDefault())
                    }
                }
        )
    }

    fun getNextEducation(educationType: QrCodeContract.EducationType): Response {
        val education = getEducation()
        markShown(education)
        return Response(education, educationType)
    }

    private fun getVariant() = ab.get().getExperimentVariant(EXPT_NAME).flatMap { exp ->
        val response = when (exp) {
            VARIANT_TAP_TARGET -> Response(getEducation(), QrCodeContract.EducationType.TapTarget)
            VARIANT_TOOLTIP -> Response(getEducation(), QrCodeContract.EducationType.ToolTip)
            else -> getDefault()
        }
        Observable.just(response)
    }.onErrorReturn {
        RecordException.recordException(it)
        getDefault()
    }

    private fun getEducation() = when {
        isNotShown(SHOW_QR_SAVE_SEND_EDUCATION) -> {
            QrCodeContract.Education.SaveSend
        }
        isNotShown(SHOW_QR_ONLINE_COLLECTION_EDUCATION) -> {
            QrCodeContract.Education.OnlineCollection
        }
        isNotShown(SHOW_QR_MENU_EDUCATION) -> {
            QrCodeContract.Education.Menu
        }
        else -> {
            QrCodeContract.Education.None
        }
    }

    private fun isNotShown(string: String) = when (string) {
        SHOW_QR_ONLINE_COLLECTION_EDUCATION -> collectionRepository.isQrOnlineCollectionEducationShown().not()
        SHOW_QR_MENU_EDUCATION -> collectionRepository.isQrMenuEducationShown().not()
        SHOW_QR_SAVE_SEND_EDUCATION -> collectionRepository.isQrSaveSendEducationShown().not()
        else -> false
    }

    private fun markShown(education: QrCodeContract.Education) = when (education) {
        QrCodeContract.Education.Menu -> collectionRepository.setQrMenuEducationShown()
        QrCodeContract.Education.OnlineCollection -> collectionRepository.setQrOnlineCollectionEducationShown()
        QrCodeContract.Education.SaveSend -> collectionRepository.setQrSaveSendEducationShown()
        QrCodeContract.Education.None -> {
            // do nothing
        }
    }

    private fun canShowQrEducation() = collectionRepository.canShowQrEducation()

    private fun isExpEnabled() = ab.get().isExperimentEnabled(EXPT_NAME)

    private fun getDefault() = Response(QrCodeContract.Education.None, QrCodeContract.EducationType.TapTarget)
}
