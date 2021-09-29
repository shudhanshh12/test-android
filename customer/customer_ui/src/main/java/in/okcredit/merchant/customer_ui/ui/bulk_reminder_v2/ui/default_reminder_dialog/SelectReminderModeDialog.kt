package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog

import `in`.okcredit.merchant.customer_ui.databinding.SelectReminderDialogBinding
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderMode
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.analytics.BulkReminderAnalyticsImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeContract.*
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import java.io.Serializable
import javax.inject.Inject

class SelectReminderModeDialog : BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>(
    "SelectReminderModeDialog",
) {

    companion object {
        const val TAG = "SelectReminderModeDialog"
        const val ARG_REMINDER_MODE = "arg_reminder_mode"

        fun showDialog(
            fragmentManager: FragmentManager,
            argReminderMode: ArgReminderMode,
        ) {
            val bundle = Bundle().apply {
                putSerializable(ARG_REMINDER_MODE, argReminderMode)
            }

            SelectReminderModeDialog().apply {
                arguments = bundle
            }.show(fragmentManager, TAG)
        }
    }

    data class ArgReminderMode(
        val customerId: String,
        val reminderMode: ReminderMode,
    ) : Serializable

    private lateinit var binding: SelectReminderDialogBinding
    private var listener: DefaultReminderModeListener? = null

    @Inject
    lateinit var tracker: Lazy<BulkReminderAnalyticsImpl>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SelectReminderDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as? DefaultReminderModeListener
    }

    private fun setClickListeners() {
        binding.apply {
            whatsappBtn.setOnClickListener {
                whatsappTick.visible()
                smsTick.gone()
                pushIntent(Intent.SetReminderMode(ReminderMode.WHATSAPP))
            }
            smsBtn.setOnClickListener {
                whatsappTick.gone()
                smsTick.visible()
                pushIntent(Intent.SetReminderMode(ReminderMode.SMS))
            }
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun render(state: State) {
        binding.apply {
            when (state.reminderMode) {
                ReminderMode.WHATSAPP -> {
                    smsTick.gone()
                    whatsappTick.visible()
                }
                ReminderMode.SMS -> {
                    whatsappTick.gone()
                    smsTick.visible()
                }
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.SetResult -> setDefaultReminderMode(event.customerId, event.mode)
        }
    }

    private fun setDefaultReminderMode(customerId: String?, mode: ReminderMode) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            tracker.get().trackSelectReminderSettings(customerId ?: "", mode)
            customerId?.also {
                listener?.setDefaultReminderMode(customerId, mode)
            }
            delay(500L)
            dismiss()
        }
    }

    interface DefaultReminderModeListener {
        fun setDefaultReminderMode(customerId: String, reminderMode: ReminderMode)
    }
}
