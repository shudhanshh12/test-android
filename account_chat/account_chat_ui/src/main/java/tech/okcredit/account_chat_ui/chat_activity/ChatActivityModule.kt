package tech.okcredit.account_chat_ui.chat_activity

import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.okcredit.account_chat_ui.chat_screen.ChatFragment
import tech.okcredit.account_chat_ui.chat_screen.ChatFragmentModule
import tech.okcredit.account_chat_ui.message_layout.SendMessageLayout
import tech.okcredit.account_chat_ui.message_list_layout.MessageListLayout
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class ChatActivityModule {

    @Binds
    abstract fun activity(activity: ChatActivity): AppCompatActivity

    @FragmentScope
    @ContributesAndroidInjector(modules = [ChatFragmentModule::class])
    abstract fun chatScreen(): ChatFragment

    @ContributesAndroidInjector
    abstract fun sendMessageLayout(): SendMessageLayout

    @ContributesAndroidInjector
    abstract fun messageListLayout(): MessageListLayout
}
