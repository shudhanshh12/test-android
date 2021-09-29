package `in`.okcredit.collection_ui.ui.set_up_collections

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.SetUpCollectionDialogListener
import `in`.okcredit.collection_ui.R
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.okcredit.android.base.animation.AnimationUtils

class SetUpCollectionDialog : DialogFragment() {

    private lateinit var listener: SetUpCollectionDialogListener
    private var cancelled: Boolean = true

    companion object {
        const val REWARD_MONEY = "reward_money"
        const val TAG = "CollectionAdoptionCTADialog"

        fun newInstance(rewardMoney: Long): SetUpCollectionDialog {

            val bundle = Bundle()
            bundle.putLong(REWARD_MONEY, rewardMoney)

            val collectionAdoptionCTADialog = SetUpCollectionDialog()
            collectionAdoptionCTADialog.arguments = bundle

            return collectionAdoptionCTADialog
        }
    }

    fun initialize(listener: SetUpCollectionDialogListener) {
        this.listener = listener
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.set_up_collection_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(view)
    }

    private fun render(view: View) {
        val rewardMoney = arguments?.getLong(REWARD_MONEY)
        view.findViewById<TextView>(R.id.tv_you_won).text =
            StringBuilder(getString(R.string.you_won, CurrencyUtil.formatV2(rewardMoney!!)))

        val ivBackIcon = view.findViewById<ImageView>(R.id.iv_back_icon)
        val setUpCollection = view.findViewById<CardView>(R.id.cv_setup_collection)
        val ivRightArrowIcon = view.findViewById<ImageView>(R.id.iv_right_arrow_icon)

        AnimationUtils.translate(ivRightArrowIcon)

        setUpCollection.setOnClickListener {
            cancelled = false
            listener.onSetUpCollectionClick()
            dismiss()
        }

        ivBackIcon.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (cancelled) {
            listener.onCancelled()
        }
    }
}
