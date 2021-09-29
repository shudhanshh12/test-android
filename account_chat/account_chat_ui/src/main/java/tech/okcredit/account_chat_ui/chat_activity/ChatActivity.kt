package tech.okcredit.account_chat_ui.chat_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import tech.okcredit.account_chat_contract.CHAT_INTENT_EXTRAS
import tech.okcredit.account_chat_sdk.ChatCore
import tech.okcredit.account_chat_ui.R
import javax.inject.Inject

class ChatActivity : AppCompatActivity(), HasAndroidInjector {

    var activeAccountId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        activeAccountId = intent?.getStringExtra(CHAT_INTENT_EXTRAS.ACCOUNT_ID)
    }

    override fun onStart() {
        super.onStart()
        ChatCore.setActiveAccountId(activeAccountId)
    }

    override fun onStop() {
        ChatCore.setActiveAccountId("")
        super.onStop()
    }

    companion object {

        fun getIntent(
            context: Context,
            accountId: String,
            role: String,
            unreadMessageCount: String,
            firstUnseenMessageId: String?
        ): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra(CHAT_INTENT_EXTRAS.ACCOUNT_ID, accountId)
                putExtra(CHAT_INTENT_EXTRAS.ROLE, role)
                putExtra(CHAT_INTENT_EXTRAS.UNREAD_MESSAGE_COUNT, unreadMessageCount)
                putExtra(CHAT_INTENT_EXTRAS.FIRST_UNSEEN_MESSAGE_ID, firstUnseenMessageId)
            }
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}
