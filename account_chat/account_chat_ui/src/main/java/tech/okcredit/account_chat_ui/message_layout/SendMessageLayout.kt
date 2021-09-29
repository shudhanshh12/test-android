package tech.okcredit.account_chat_ui.message_layout

import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.send_message_layout.view.*
import tech.okcredit.account_chat_ui.R
import java.util.concurrent.TimeUnit

class SendMessageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseLayout<SendMessageContract.State>(context, attrs, defStyleAttr), SendMessageContract.Interactor {
    init {
        LayoutInflater.from(context).inflate(R.layout.send_message_layout, this, true)
        viewModel.setNavigation(this)
    }

    private var callback: SendMessageContract.Callback? = null
    private val sendMessageSubject = PublishSubject.create<SendMessageContract.PreMessageState>()
    private val trackMessageStartSubject = PublishSubject.create<Unit>()
    private val messageSubject = PublishSubject.create<String>()
    private val loadSubject = BehaviorSubject.create<SendMessageContract.IntialData>()

    override fun loadIntent(): UserIntent {
        return SendMessageContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {

        return Observable.mergeArray(
            loadSubject.map {
                SendMessageContract.Intent.LoadInitialData(it)
            },
            sendMessageSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { SendMessageContract.Intent.SendMessage(it) },
            messageSubject
                .map { SendMessageContract.Intent.Message(it) },
            trackMessageStartSubject
                .map { SendMessageContract.Intent.TrackMessageStart }
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                charSequence.let {
                    if (it.isEmpty()) {
                        trackMessageStartSubject.onNext(Unit)
                    }
                }
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                messageSubject.onNext(charSequence.toString().trim { it <= ' ' })
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        sendButton.setOnClickListener {
            sendMessageSubject.onNext(
                SendMessageContract.PreMessageState(
                    messageEditText.text.toString(),
                    getCurrentState().accountID,
                    getCurrentState().role,
                    getCurrentState().accountName,
                    getCurrentState().receiverRole
                )
            )
        }
    }

    override fun render(state: SendMessageContract.State) {
        when (state.editTextState) {
            is SendMessageContract.EditTextState.Empty -> messageEditText.text.clear()
        }
        when (state.sendButtonState) {
            is SendMessageContract.SendButtonState.Inactive -> {
                sendButton.isEnabled = false
                sendButton.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(sendButton.context, R.color.grey300))
            }
            is SendMessageContract.SendButtonState.Active -> {
                sendButton.isEnabled = true
                sendButton.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(sendButton.context, R.color.green_primary))
            }
        }
    }

    override fun postMessageSentActions() {
        callback?.onMessageSent()
    }

    fun setData(
        cacheAccountId: String,
        role: String?,
        accountName: String?,
        recevierRole: String?,
        callback: SendMessageContract.Callback
    ) {
        this.callback = callback
        loadSubject.onNext(SendMessageContract.IntialData(cacheAccountId, role, accountName, recevierRole))
    }
}
