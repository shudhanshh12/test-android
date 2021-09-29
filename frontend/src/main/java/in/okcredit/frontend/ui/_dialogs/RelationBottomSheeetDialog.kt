package `in`.okcredit.frontend.ui._dialogs

import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.databinding.RelationBottomSheetDialogBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator

class RelationBottomSheeetDialog : ExpandedBottomSheetDialogFragment() {

    private var mListener: RelationBottomSheetDialogListener? = null
    private lateinit var binding: RelationBottomSheetDialogBinding

    private var title: String = ""
    private var name: String = ""
    private var profileImg: String = ""
    private var number: String = ""
    private var description: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RelationBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            title = it.getString("title", "")
            name = it.getString("name", "")
            profileImg = it.getString("profileImg", "")
            number = it.getString("number", "")
            description = it.getString("description", "")
            render()
        }
    }

    fun setListener(listener: RelationBottomSheetDialogListener) {
        mListener = listener
    }

    private fun render() {
        binding.title.text = title
        binding.description.text = description
        binding.name.text = name
        binding.number.text = number
        binding.cancel.setOnClickListener {
            mListener?.onDismiss()
            dismiss()
        }
        binding.accept.setOnClickListener {
            mListener?.onConfirm(binding.accept.text.toString())
            dismiss()
        }
        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                name.substring(0, 1).toUpperCase(),
                ColorGenerator.MATERIAL.getColor(name)
            )
        imageLoader.context(this)
            .load(profileImg)
            .placeHolder(defaultPic)
            .scaleType(IImageLoader.CIRCLE_CROP)
            .into(binding.profileImage)
            .build()
    }

    companion object {
        private lateinit var imageLoader: IImageLoader

        val TAG = "RelationBottomSheeetDialog"

        fun newInstance(
            title: String,
            description: String,
            name: String?,
            profileImg: String?,
            number: String?,
            imageLoader: IImageLoader
        ): RelationBottomSheeetDialog {
            val frag = RelationBottomSheeetDialog()
            this.imageLoader = imageLoader
            val args = Bundle()
            args.putString("title", title)
            args.putString("description", description)
            args.putString("name", name)
            args.putString("profileImg", profileImg)
            args.putString("number", number)
            frag.arguments = args
            return frag
        }
    }

    interface RelationBottomSheetDialogListener {
        fun onConfirm(action: String)
        fun onDismiss()
    }
}
