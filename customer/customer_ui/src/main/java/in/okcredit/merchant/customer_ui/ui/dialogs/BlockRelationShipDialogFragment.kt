package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.BlockRelationshipDialogFragmentBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BlockRelationShipDialogFragment : BottomSheetDialogFragment() {

    private var mListener: Listener? = null

    private var screen: String? = null
    private var type: String? = null
    private lateinit var binding: BlockRelationshipDialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BlockRelationshipDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            screen = it.getString("screen", "customer")
            type = it.getString("type", "block")
            val name = it.getString("name", "")
            val profileImg = it.getString("profileImg", "")
            val number = it.getString("number", "")
            render(name, profileImg, number)
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    private fun render(name: String, profileImg: String?, number: String?) {
        if (screen == SCREEN_CUSTOMER && type == TYPE_BLOCK) {
            binding.title.text = getString(R.string.block_customer)
            binding.accept.text = getString(R.string.block)
            binding.description.text = getString(R.string.block_customer_description)
        } else if (screen == SCREEN_CUSTOMER && type == TYPE_UNBLOCK) {
            binding.title.text = getString(R.string.unblock_customer)
            binding.accept.text = getString(R.string.unblock)
            binding.description.text = getString(R.string.unblock_customer_description)
        } else if (screen == SCREEN_SUPPLIER && type == TYPE_BLOCK) {
            binding.title.text = getString(R.string.block_supplier)
            binding.accept.text = getString(R.string.block)
            binding.description.text = getString(R.string.block_supplier_description)
        } else {
            binding.title.text = getString(R.string.unblock_supplier)
            binding.accept.text = getString(R.string.unblock)
            binding.description.text = getString(R.string.unblock_supplier_description)
        }
        binding.name.text = name
        binding.number.text = number
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.accept.setOnClickListener {
            mListener?.onAction(binding.accept.text.toString())
            dismiss()
        }
        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                name.substring(0, 1).toUpperCase(),
                ColorGenerator.MATERIAL.getColor(name)
            )
        Glide.with(this)
            .load(profileImg)
            .placeholder(defaultPic)
            .into(binding.profileImage)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface Listener {
        fun onAction(action: String)
    }

    companion object {

        val TAG = "BlockRelationShipDialogFragment"
        val SCREEN_CUSTOMER = "customer"
        val SCREEN_SUPPLIER = "supplier"
        val TYPE_BLOCK = "block"
        val TYPE_UNBLOCK = "unblock"

        fun newInstance(
            screen: String,
            type: String,
            name: String?,
            profileImg: String?,
            number: String?,
        ): BlockRelationShipDialogFragment {
            val frag = BlockRelationShipDialogFragment()
            val args = Bundle()
            args.putString("screen", screen)
            args.putString("type", type)
            args.putString("name", name)
            args.putString("profileImg", profileImg)
            args.putString("number", number)
            frag.arguments = args
            return frag
        }
    }
}
