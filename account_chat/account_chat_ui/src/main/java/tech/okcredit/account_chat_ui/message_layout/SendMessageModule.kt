package tech.okcredit.account_chat_ui.message_layout

import `in`.okcredit.shared.base.IBaseLayoutViewModel
import dagger.Module
import dagger.Provides

@Module
abstract class SendMessageModule {

    companion object {

        @Provides
        fun initialState() = SendMessageContract.State()

        @Provides
        fun sendMessagePresenter(
            sendMessagePresenter: SendMessageViewModel
        ): IBaseLayoutViewModel<SendMessageContract.State> {
            return sendMessagePresenter
        }
    }
}
