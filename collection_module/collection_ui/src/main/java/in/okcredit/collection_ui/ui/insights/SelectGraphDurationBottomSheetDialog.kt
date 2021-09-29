package `in`.okcredit.collection_ui.ui.insights

import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphDuration
import `in`.okcredit.collection_ui.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.graph_duration_layout.*

class SelectGraphDurationBottomSheetDialog : BottomSheetDialogFragment() {

    private var selectGraphDurationListener: SelectGraphDurationListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.graph_duration_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rl_today.setOnClickListener {
            selectGraphDurationListener?.selectGraphDuration(GraphDuration.TODAY)
            dismiss()
        }

        rl_yesterday.setOnClickListener {
            selectGraphDurationListener?.selectGraphDuration(GraphDuration.YESTERDAY)
            dismiss()
        }

        rl_week.setOnClickListener {
            selectGraphDurationListener?.selectGraphDuration(GraphDuration.WEEK)
            dismiss()
        }

        rl_month.setOnClickListener {
            selectGraphDurationListener?.selectGraphDuration(GraphDuration.MONTH)
            dismiss()
        }
    }

    fun initialise(selectGraphDurationListener: SelectGraphDurationListener) {
        this.selectGraphDurationListener = selectGraphDurationListener
    }

    interface SelectGraphDurationListener {
        fun selectGraphDuration(selectedGraphDuration: GraphDuration)
    }

    companion object {

        val TAG: String? = SelectGraphDurationBottomSheetDialog::class.java.simpleName

        fun newInstance(): SelectGraphDurationBottomSheetDialog {
            return SelectGraphDurationBottomSheetDialog()
        }
    }
}
