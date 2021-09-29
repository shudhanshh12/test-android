package tech.okcredit.account_chat_ui.message_list_layout

import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.SharedDrawableUtils
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.message_list_layout.view.*
import org.joda.time.DateTime
import tech.okcredit.account_chat_contract.FIELDS
import tech.okcredit.account_chat_sdk.ChatProvider
import tech.okcredit.account_chat_sdk.models.Message
import tech.okcredit.account_chat_sdk.utils.ChatUtils
import tech.okcredit.account_chat_ui.R
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.DimensionUtil
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class MessageListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseLayout<MessageListContract.State>(context, attrs, defStyleAttr), MessageListContract.Interactor {

    init {
        LayoutInflater.from(context).inflate(R.layout.message_list_layout, this, true)
        viewModel.setNavigation(this)
    }

    private lateinit var callback: MessageListContract.Callback
    private var unreadPosition: Int = 0
    private var canShowUnread: AtomicBoolean = AtomicBoolean(false)
    private var isChatListInitialised: AtomicBoolean = AtomicBoolean(false)
    private var registration: ListenerRegistration? = null
    private val sentSoundSubject = PublishSubject.create<Unit>()
    private val loadSubject = BehaviorSubject.create<MessageListContract.IntialData>()
    private lateinit var mFirestoreInstance: FirebaseFirestore

    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private var firebaseAdapter: FirestoreRecyclerAdapter<Message, MessageViewHolder>? = null

    override fun loadIntent(): UserIntent {
        return MessageListContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {

        return Observable.mergeArray(
            loadSubject.map {
                MessageListContract.Intent.LoadInitialData(it)
            }
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirestoreInstance = FirebaseFirestore.getInstance()
        firebaseAdapter?.startListening()
        sentSoundSubject.map {
            ChatUtils.playReceivedSound(context)
        }.subscribe().addTo(autoDisposable)
    }

    override fun onDetachedFromWindow() {
        registration?.remove()
        registration = null
        firebaseAdapter?.stopListening()
        super.onDetachedFromWindow()
    }

    override fun render(state: MessageListContract.State) {
        if (state.uid != null && state.merchatId != null && state.accountID != null && state.uid == state.merchatId &&
            isChatListInitialised.get().not()
        ) {
            executeMessages(state.firstUnreadMessageId, state.unreadMessageCount)
        }
        if (!state.unreadMessageCount.isNullOrEmpty() && (!state.firstUnreadMessageId.isNullOrEmpty()) &&
            canShowUnread.get()
                .not()
        ) {
            canShowUnread = AtomicBoolean(true)
        }
    }

    fun setData(
        cacheAccountId: String,
        unreadMessageCount: String?,
        firstUnreadMessageId: String?,
        callback: MessageListContract.Callback
    ) {
        this.callback = callback
        loadSubject.onNext(MessageListContract.IntialData(cacheAccountId, unreadMessageCount, firstUnreadMessageId))
    }

    private fun executeMessages(firstUnreadMessageId: String?, unreadMessageCount: String?) {

        val dbQuery: Query = ChatProvider.provideMessagesCollectionPath(getCurrentState().merchatId!!)
            .whereEqualTo(FIELDS.ACCOUNT_ID, getCurrentState().accountID!!)
            .orderBy(FIELDS.ORDER_FOR_ME, Query.Direction.ASCENDING)
        registration = dbQuery
            .addSnapshotListener(
                Executors.newCachedThreadPool(),
                EventListener { querySnapshot, firebaseFirestoreException ->
                    querySnapshot?.let {
                        var timeHacker = 0
                        for (i in querySnapshot.documentChanges) {
                            val message = i.document.toObject(Message::class.java)
                            val currentTime = DateTime.now().millis
                            if (message.first_seen_time == null && !message.sent_by_me) {
                                i.document.reference.update(
                                    FIELDS.FIRST_SEEN_TIME,
                                    (currentTime + timeHacker).toString()
                                )
                            }
                            if (message.server_create_time != null && message.metaInfo != null && message.metaInfo!!.sentSoundPlayed.not() && message.sent_by_me) {
                                i.document.reference.update(
                                    FIELDS.METAINFO_SENT_SOUND_PLAYED,
                                    true
                                )
                                ChatUtils.playSentSound(context)
                            }
                            timeHacker++
                        }
                    }
                }
            )
        val options = FirestoreRecyclerOptions.Builder<Message>().setQuery(dbQuery, Message::class.java).build()
        firebaseAdapter = object : FirestoreRecyclerAdapter<Message, MessageViewHolder>(options) {
            @SuppressLint("RtlHardcoded")
            override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int, friendlyMessage: Message) {
                if (friendlyMessage.message != null) {
                    val currentTime = friendlyMessage.order_for_me
                    if (position != 0) {
                        val previousMessage = firebaseAdapter?.getItem(position - 1)
                        val previousTime = previousMessage?.order_for_me
                        if (ChatUtils.canShowDate(
                                previousTime,
                                currentTime
                            )
                        ) {
                            ChatUtils.setDate(currentTime, viewHolder.date, viewHolder.dateContainer)
                        } else {
                            ChatUtils.disableChat(viewHolder.date, viewHolder.dateContainer)
                        }
                    } else if (position == 0) {
                        ChatUtils.setDate(currentTime, viewHolder.date, viewHolder.dateContainer)
                    } else {
                        ChatUtils.disableChat(viewHolder.date, viewHolder.dateContainer)
                    }
                    if (unreadMessageCount != null && firstUnreadMessageId != null) {
                        if (firstUnreadMessageId == friendlyMessage.message_id) {
                            val unreadPosition = position
                            if (canShowUnread.get()) {
                                android.os.Handler(Looper.getMainLooper()).postDelayed(
                                    {
                                        canShowUnread = AtomicBoolean(false)
                                        messageRecyclerView.scrollToPosition(unreadPosition)
                                    },
                                    1000
                                )
                            }
                            viewHolder.unreadMessageBand.visibility = View.VISIBLE
                            viewHolder.unreadMessageBand.text =
                                context.getString(R.string.unreadchat_messages, unreadMessageCount)
                        } else {
                            viewHolder.unreadMessageBand.visibility = View.GONE
                        }
                    } else {
                        viewHolder.unreadMessageBand.visibility = View.GONE
                    }
                    viewHolder.messageTextView.text = friendlyMessage.message
                    if (friendlyMessage.sent_by_me) {
                        val params = LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.END
                            setMargins(
                                DimensionUtil.dp2px(context, 16f).toInt(),
                                0,
                                DimensionUtil.dp2px(context, 16f).toInt(),
                                0
                            )
                        }
                        viewHolder.materialContainer.layoutParams = params
                        viewHolder.materialContainer.backgroundTintList =
                            ContextCompat.getColorStateList(context!!, R.color.green_lite_1)
                        viewHolder.sync.visibility = View.VISIBLE
                        when {
                            friendlyMessage.first_seen_time != null -> {
                                // show blue tick
                                viewHolder.sync.setImageDrawable(
                                    SharedDrawableUtils.getDrawableWithColor(
                                        context,
                                        R.drawable.ic_sync_ok,
                                        R.color.lang_curious_blue
                                    )
                                )
                            }
                            friendlyMessage.first_delivered_time != null -> {
                                // show double tick
                                viewHolder.sync.setImageDrawable(
                                    SharedDrawableUtils.getDrawableWithColor(
                                        context,
                                        R.drawable.ic_sync_ok,
                                        R.color.grey400
                                    )
                                )
                            }
                            friendlyMessage.server_create_time != null -> {
                                // show single tick
                                viewHolder.sync.setImageDrawable(
                                    SharedDrawableUtils.getDrawableWithColor(
                                        context,
                                        R.drawable.ic_chat_single,
                                        R.color.grey400
                                    )
                                )
                            }
                            friendlyMessage.server_create_time == null -> {
                                // show clock
                                viewHolder.sync.setImageDrawable(
                                    SharedDrawableUtils.getDrawableWithColor(
                                        context,
                                        R.drawable.ic_sync_pending,
                                        R.color.grey400
                                    )
                                )
                            }
                        }
                        viewHolder.tx_date.text =
                            DateTimeUtils.formatTimeOnly(DateTime(friendlyMessage.app_create_time!!.toLong()))
                    } else {
                        val params = LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.START
                            setMargins(
                                DimensionUtil.dp2px(context, 16f).toInt(),
                                0,
                                DimensionUtil.dp2px(context, 16f).toInt(),
                                0
                            )
                        }
                        viewHolder.materialContainer.backgroundTintList =
                            ContextCompat.getColorStateList(context!!, R.color.white)
                        viewHolder.materialContainer.layoutParams = params
                        if (friendlyMessage.first_delivered_time != null) {
                            viewHolder.sync.visibility = View.GONE
                            viewHolder.tx_date.text =
                                DateTimeUtils.formatTimeOnly(DateTime(friendlyMessage.first_delivered_time!!.toLong()))
                        }
                    }
                } else {
                    viewHolder.ll.visibility = View.GONE
                }
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MessageViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                return MessageViewHolder(
                    inflater.inflate(R.layout.item_message, viewGroup, false)
                )
            }
        }

        messageRecyclerView.adapter = firebaseAdapter
        mLinearLayoutManager = LinearLayoutManager(context)
        mLinearLayoutManager.stackFromEnd = true
        messageRecyclerView.layoutManager = mLinearLayoutManager

        firebaseAdapter?.startListening()

        firebaseAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                firebaseAdapter?.let {
                    val friendlyMessageCount: Int = it.itemCount
                    val lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                    Observable.fromCallable {
                        val item = firebaseAdapter?.getItem(positionStart)
                        item?.let {
                            if (unreadMessageCount != null && firstUnreadMessageId != null) {
                                if (firstUnreadMessageId == it.message_id) {

                                    unreadPosition = positionStart

                                    if (canShowUnread.get()) {
                                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                                            {
                                                canShowUnread = AtomicBoolean(false)
                                                messageRecyclerView.scrollToPosition(unreadPosition)
                                            },
                                            500
                                        )
                                    }
                                }
                            }
                        }
                    }.subscribeOn(Schedulers.io()).subscribe().addTo(autoDisposable)

                    if (lastVisiblePosition == -1 ||
                        positionStart >= friendlyMessageCount - 1 &&
                        lastVisiblePosition == positionStart - 1
                    ) {

                        messageRecyclerView.scrollToPosition(positionStart)
                    }
                }
            }
        })

        isChatListInitialised.set(true)
    }

    fun onMessageSent() {
        firebaseAdapter?.let {
            var count = it.itemCount - 1
            if (it.itemCount == 0) {
                count = 0
            }
            messageRecyclerView?.scrollToPosition(count)
        }
    }
}
