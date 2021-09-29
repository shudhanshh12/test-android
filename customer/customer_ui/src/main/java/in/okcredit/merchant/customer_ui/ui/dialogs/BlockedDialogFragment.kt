package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.DialogFragmentBlockedBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_blocked.*

class BlockedDialogFragment : BottomSheetDialogFragment() {

    private var mListener: BlockedListener? = null

    var screen: String? = null
    var type: String? = null
    var action = ACTION_CALL

    private lateinit var binding: DialogFragmentBlockedBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogFragmentBlockedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            screen = it.getString("screen", "customer")
            type = it.getString("type", "block")
            render()
        }
    }

    fun setListener(listener: BlockedListener) {
        mListener = listener
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun render() {
        if (screen == SCREEN_CUSTOMER && type == TYPE_BLOCKED) {
            binding.title.text = getString(R.string.blocked_customer)
            binding.actionText?.text = getString(R.string.unblock)
            binding.description.text = getString(R.string.blocked_customer_description)
            binding.actionImg.setImageResource(R.drawable.ic_unblock)
            binding.actionImg.setColorFilter(resources.getColor(R.color.primary))
            action = ACTION_UNBLOCK
        } else if (screen == SCREEN_CUSTOMER && type == TYPE_BLOCKED_BY) {
            binding.title.text = getString(R.string.blocked_by_customer)
            binding.actionText.text = getString(R.string.call_customer)
            binding.description.text = getString(R.string.blocked_by_customer_description)
            binding.actionImg.setImageResource(R.drawable.ic_call)
            action = ACTION_CALL
        } else if (screen == SCREEN_SUPPLIER && type == TYPE_BLOCKED) {
            binding.title.text = getString(R.string.blocked_supplier)
            binding.actionText.text = getString(R.string.unblock)
            binding.description.text = getString(R.string.blocked_supplier_description)
            binding.actionImg.setImageResource(R.drawable.ic_unblock)
            binding.actionImg.setColorFilter(resources.getColor(R.color.primary))
            action = ACTION_UNBLOCK
        } else {
            binding.title.text = getString(R.string.blocked_by_supplier)
            binding.actionText.text = getString(R.string.call_supplier)
            binding.description.text = getString(R.string.blocked_by_supplier_description)
            binding.actionImg.setImageResource(R.drawable.ic_call)
            action = ACTION_CALL
        }
        binding.actionText.setOnClickListener {
            mListener?.onBlockListenerAction(action)
        }
    }

    interface BlockedListener {
        fun onBlockListenerAction(action: Int)
    }

    companion object {

        val TAG = "BlockedDialogFragment"
        val ACTION_CALL = 0
        val ACTION_UNBLOCK = 1
        val SCREEN_CUSTOMER = "customer"
        val SCREEN_SUPPLIER = "supplier"
        val TYPE_BLOCKED_BY = "blockedBy"
        val TYPE_BLOCKED = "blocked"

        fun newInstance(screen: String, type: String): BlockedDialogFragment {
            val frag = BlockedDialogFragment()
            val args = Bundle()
            args.putString("screen", screen)
            args.putString("type", type)
            frag.arguments = args
            return frag
        }
    }
}
