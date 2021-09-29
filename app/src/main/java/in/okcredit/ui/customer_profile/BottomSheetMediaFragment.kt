package `in`.okcredit.ui.customer_profile

import `in`.okcredit.databinding.MediaBottomSheetBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.RuntimeException

class BottomSheetMediaFragment : BottomSheetDialogFragment() {
    private var mListener: OnBottomSheetFragmentListener? = null
    private var binding: MediaBottomSheetBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MediaBottomSheetBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.addFromCamera.setOnClickListener { v: View? -> mListener!!.onClickCamera() }
        binding!!.addFromGallery.setOnClickListener { v: View? -> mListener!!.onClickGallery() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnBottomSheetFragmentListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnFeedInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnBottomSheetFragmentListener {
        fun onClickCamera()
        fun onClickGallery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): BottomSheetMediaFragment {
            return BottomSheetMediaFragment()
        }
    }
}
