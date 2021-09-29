package tech.okcredit.home.dialogs

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.BACKPRESSED
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.AppShortcutHelper
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.AppShortcutAdder
import tech.okcredit.home.databinding.AddTransactionShortcutRequestBottomSheetBinding
import javax.inject.Inject

class AddTransactionShortcutRequestBottomSheet : ExpandedBottomSheetDialogFragment() {

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var shortcutHelper: Lazy<AppShortcutHelper>

    @Inject
    internal lateinit var appShortcutAdder: Lazy<AppShortcutAdder>

    @Inject
    internal lateinit var context: Lazy<Context>

    companion object {
        const val TAG = "AddTransactionShortcutRequestBottomSheet"
        fun show(fragmentManager: FragmentManager) {
            AddTransactionShortcutRequestBottomSheet().show(fragmentManager, TAG)
        }
    }

    private val binding: AddTransactionShortcutRequestBottomSheetBinding by viewLifecycleScoped(
        AddTransactionShortcutRequestBottomSheetBinding::bind
    )

    private var submitClicked = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = AddTransactionShortcutRequestBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appShortcutAdder.get().addAppShortcutIfNotAdded(AppShortcutAdder.Shortcut.ADD_TRANSACTION, context.get())
        binding.mbSubmit.setOnClickListener {
            trackClick()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                shortcutHelper.get().requestPinShortcut(AppShortcutAdder.Shortcut.ADD_TRANSACTION.id)
            }
            dismiss()
        }
    }

    private fun trackClick() {
        submitClicked = true
        tracker.get().trackInAppClickedV2(
            type = "add_transaction_shortcut",
            screen = PropertyValue.HOME_PAGE,
            focalArea = PropertyValue.FALSE,
            value = "yes"
        )
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (submitClicked.not()) {
            tracker.get().trackInAppClearedV1(type = "add_transaction_shortcut", method = BACKPRESSED)
        }
    }
}
