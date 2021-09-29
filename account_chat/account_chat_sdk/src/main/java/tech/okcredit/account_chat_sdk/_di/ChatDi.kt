package tech.okcredit.account_chat_sdk._di

import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.account_chat_contract.SignOutFirebaseAndRemoveChatListener
import tech.okcredit.account_chat_sdk.*
import tech.okcredit.account_chat_sdk.use_cases.GetChatUnreadMessageCount
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils

@dagger.Module
abstract class ChatModule {

    @Binds
    @Reusable
    abstract fun server(server: AccountsChatRemoteSourceImpl): AccountsChatRemoteSource

    @Binds
    @Reusable
    abstract fun getChatUnreadMessageCount(getChatUnreadMessageCount: GetChatUnreadMessageCount): IGetChatUnreadMessageCount

    @Binds
    @Reusable
    abstract fun chatListener(listener: ChatListener): IChatListner

    @Binds
    @AppScope
    abstract fun signOutFireBaseUser(chatCore: ChatCore): SignOutFirebaseAndRemoveChatListener

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): AccountsApiClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
