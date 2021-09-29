package tech.okcredit.account_chat_ui.message_list_layout

import `in`.okcredit.shared.base.IBaseLayoutViewModel
import dagger.Module
import dagger.Provides

@Module
abstract class MessageListModule {

    companion object {

        @Provides
        fun initialState() = MessageListContract.State()

        @Provides
        fun messageListPresenter(
            messageListPresenter: MessageListViewModel
        ): IBaseLayoutViewModel<MessageListContract.State> {
            return messageListPresenter
        }
    }
}
