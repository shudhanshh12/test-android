package tech.okcredit.account_chat_sdk

import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.account_chat_sdk.models.FireBaseToken
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class AccountsChatRemoteSourceImpl @Inject constructor(
    private val accountsApiClient: Lazy<AccountsApiClient>,
) : AccountsChatRemoteSource {

    override fun getToken(businessId: String): Single<FireBaseToken> {
        return accountsApiClient.get().getToken(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }
}
