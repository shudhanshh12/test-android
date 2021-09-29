// package `in`.okcredit.frontend.ui.supplier
//
// import `in`.okcredit.frontend.R
// import `in`.okcredit.frontend.ui.customer.ShareReportBottomSheetDialog
// import `in`.okcredit.frontend.usecase.GetReportShareIntent
// import android.os.Bundle
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import android.widget.LinearLayout
// import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//
// class SupplierShareReportBottomSheetDialog : BottomSheetDialogFragment() {
//
//    private var shareReportListener: ShareReport? = null
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.share_report_bottom_sheet_layout, container, false)
//        val miniStatement = view.findViewById<LinearLayout>(R.id.mini_statement)
//        val monthlyReport = view.findViewById<LinearLayout>(R.id.monthly_report)
//
//        miniStatement.setOnClickListener {
//            shareReportListener?.shareReport(GetReportShareIntent.TYPE_MINI)
//            dismiss()
//        }
//
//        monthlyReport.setOnClickListener {
//            shareReportListener?.shareReport(GetReportShareIntent.TYPE_MONTHLY)
//            dismiss()
//        }
//        return view
//    }
//
//    fun initialise(shareReportListener: ShareReport) {
//        this.shareReportListener = shareReportListener
//    }
//
//    /**
//     * ShareReport on WhatsApp
//     */
//    interface ShareReport {
//        fun shareReport(reportType: Int)
//    }
//
//    companion object {
//
//        val TAG: String? = ShareReportBottomSheetDialog::class.java.simpleName
//
//        fun newInstance(): ShareReportBottomSheetDialog {
//            return ShareReportBottomSheetDialog()
//        }
//    }
// }
