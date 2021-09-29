package tech.okcredit.account_chat_ui.chat_screen

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.account_chat_contract.CHAT_INTENT_EXTRAS
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class ChatFragmentModule {

    companion object {
        @Provides
        fun initialState(): ChatContract.State =
            ChatContract.State()

        @Provides
        @ViewModelParam(CHAT_INTENT_EXTRAS.ACCOUNT_ID)
        fun getAccountId(chatFragment: ChatFragment): String? {
            return chatFragment.activity?.intent?.getStringExtra(CHAT_INTENT_EXTRAS.ACCOUNT_ID)
        }

        @Provides
        @ViewModelParam(CHAT_INTENT_EXTRAS.ROLE)
        fun getRole(chatFragment: ChatFragment): String? {
            return chatFragment.activity?.intent?.getStringExtra(CHAT_INTENT_EXTRAS.ROLE)
        }

        @Provides
        @ViewModelParam(CHAT_INTENT_EXTRAS.UNREAD_MESSAGE_COUNT)
        fun getUnreadMessageCount(chatFragment: ChatFragment): String? {
            return chatFragment.activity?.intent?.getStringExtra(CHAT_INTENT_EXTRAS.UNREAD_MESSAGE_COUNT)
        }

        @Provides
        @ViewModelParam(CHAT_INTENT_EXTRAS.FIRST_UNSEEN_MESSAGE_ID)
        fun getFirstUnseenMessageId(chatFragment: ChatFragment): String? {
            return chatFragment.activity?.intent?.getStringExtra(CHAT_INTENT_EXTRAS.FIRST_UNSEEN_MESSAGE_ID)
        }

        @Provides
        fun viewModel(
            fragment: ChatFragment,
            viewModelProvider: Provider<ChatViewModel>
        ): MviViewModel<ChatContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
