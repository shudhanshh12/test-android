package tech.okcredit.account_chat_sdk

import io.reactivex.Single
import tech.okcredit.account_chat_sdk.models.FireBaseToken

interface AccountsChatRemoteSource {
    fun getToken(businessId: String): Single<FireBaseToken>
}
