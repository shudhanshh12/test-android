package merchant.okcredit.accounting.usecases

import dagger.Lazy
import io.reactivex.Single
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetCustomerSupportTypeImpl @Inject constructor(private val abRepository: Lazy<AbRepository>) :
    GetCustomerSupportType {

    override fun execute(): Single<SupportType> {
        return Single.zip(
            abRepository.get().isFeatureEnabled(PAYMENT_SUPPORT_CALL_FEATURE).firstOrError(),
            abRepository.get().isFeatureEnabled(PAYMENT_SUPPORT_CHAT_FEATURE).firstOrError(),
            { isCallEnabled, isChatEnabled ->
                when {
                    isCallEnabled -> SupportType.CALL
                    isChatEnabled -> SupportType.CHAT
                    else -> SupportType.NONE
                }
            }
        )
    }

    companion object {
        private const val PAYMENT_SUPPORT_CALL_FEATURE = "call_support_payment"
        private const val PAYMENT_SUPPORT_CHAT_FEATURE = "chat_support_payment"
    }
}
