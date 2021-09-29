package tech.okcredit.home.ui.settings.dialogs

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.ConfirmSignoutBottomSheetBinding

class ConfirmSignoutScreen : ExpandedBottomSheetDialogFragment() {

    private val binding: ConfirmSignoutBottomSheetBinding by viewLifecycleScoped(ConfirmSignoutBottomSheetBinding::bind)
    private var mListener: ConfirmSignoutScreen.OnSignoutListener? = null

    fun initialise(mListener: ConfirmSignoutScreen.OnSignoutListener) {
        this.mListener = mListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = ConfirmSignoutBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onStart() {
        super.onStart()
        try {
            val dialog = dialog ?: return
            val bottomSheet = dialog.findViewById<View>(R.id.root_view)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val view = view ?: return
            view.post {
                val parent = view.parent as View
                val params = parent.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = params.behavior
                val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.peekHeight = view.measuredHeight
                    (binding.rootView.parent as View).setBackgroundColor(Color.TRANSPARENT)
                }

                bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        } catch (e: Exception) {
        }
    }

    fun initListeners() {
        binding.cancelBtn.setOnClickListener { dialog?.dismiss() }
        binding.tvSignout.setOnClickListener {
            dialog?.dismiss()
            mListener?.onSignoutClicked()
        }
    }

    interface OnSignoutListener {
        fun onSignoutClicked()
    }

    companion object {
        fun newInstance(): ConfirmSignoutScreen {
            return ConfirmSignoutScreen()
        }
    }
}
