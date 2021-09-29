package tech.okcredit.bill_management_ui.edit_notes

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.bill_management_ui.databinding.EditNoteScreenBinding
import tech.okcredit.bill_management_ui.edit_notes.EditNoteContract.*
import tech.okcredit.sdk.analytics.BillTracker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditNoteFragment : BaseBottomSheetWithViewEvents<State, ViewEvents, Intent>("EditNoteScreen") {

    private val binding: EditNoteScreenBinding by viewLifecycleScoped(EditNoteScreenBinding::bind)
    private val editedNote: PublishSubject<String> = PublishSubject.create()
    private val submitNumberPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val editTextFocusChangePublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var billTracker: Lazy<BillTracker>

    var firstFocus = true

    var addNoteStarted = false

    override fun onStart() {
        super.onStart()

        handleOutsideClick()
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                KeyboardVisibilityEvent.hideKeyboard(context, view)
            } else {
                dismiss()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return EditNoteScreenBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()

            disableDraggingInBottomSheet(behavior)
        }

        binding.doneButton.setOnClickListener {
            if (!binding.noteEditText.text.isNullOrBlank()) {
                billTracker.get()
                    .trackAddNoteCompleted("Edit Bill", "Fab", "Bill Management", binding.noteEditText.text.toString())
            }
            editedNote.onNext(binding.noteEditText.text.toString())
        }
        binding.noteEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && firstFocus) {
                firstFocus = false
                billTracker.get().trackAddNoteClicked("Edit Bill", "Fab", "Bill Management")
            }
        }

        binding.noteEditText.addTextChangedListener {
            if (addNoteStarted.not() && it?.toString()?.length ?: 0 > 0) {
                addNoteStarted = true
                billTracker.get().trackAddNoteStarted("Edit Bill", "Fab", "Bill Management")
            }
        }
        binding.noteEditText.postDelayed(
            { showSoftKeyboard(binding.noteEditText) }, 100
        )
    }

    internal fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    internal fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            editedNote
                .throttleLast(200, TimeUnit.MILLISECONDS)
                .map {
                    Intent.EditedNote(it)
                },

            submitNumberPublishSubject
                .map {
                    Intent.SubmitMobileNumber(it)
                },

            editTextFocusChangePublishSubject
                .map {
                    Intent.SetEditTextFocus(it)
                }
        )
    }

    override fun render(state: State) {
        state.note?.let {
            binding.noteEditText.setText(it)
            binding.noteEditText.setSelection(it.length)
        }
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {
            ViewEvents.GoBack -> findNavController().popBackStack()
        }
    }
}
