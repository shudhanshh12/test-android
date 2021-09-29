package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ShouldShowTransactionSecurityEducation @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
) {

    fun execute(): Observable<TransactionSuccessScreenVariant> =
        abRepository.get().isExperimentEnabled(EXPERIMENT_NAME).switchMap { experimentEnabled ->
            if (!experimentEnabled) {
                return@switchMap Observable.just(TransactionSuccessScreenVariant.OLD_DESIGN)
            }

            return@switchMap abRepository.get().getExperimentVariant(EXPERIMENT_NAME).switchMap { variant ->
                return@switchMap when (variant) {
                    NEW_SCREEN -> Observable.just(TransactionSuccessScreenVariant.NEW_DESIGN)
                    else -> Observable.just(TransactionSuccessScreenVariant.OLD_DESIGN)
                }
            }
        }

    companion object {
        private const val EXPERIMENT_NAME = "postlogin_android-all-transaction_complete_screen"

        private const val NEW_SCREEN = "new_screen"
    }
}

enum class TransactionSuccessScreenVariant(val value: String) {
    NEW_DESIGN("new_screen"), OLD_DESIGN("old_screen")
}
