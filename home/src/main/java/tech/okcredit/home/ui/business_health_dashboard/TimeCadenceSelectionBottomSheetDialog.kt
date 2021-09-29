package tech.okcredit.home.ui.business_health_dashboard

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import tech.okcredit.android.base.extensions.fromJson
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.home.R

class TimeCadenceSelectionBottomSheetDialog : ExpandedBottomSheetDialogFragment() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var timeCadenceTitleList: List<String>
    private lateinit var selectedTimeCadenceTitle: String
    private var timeCadenceSelectionListener: TimeCadenceSelectionListener? = null

    fun init(timeCadenceSelectionListener: TimeCadenceSelectionListener) {
        this.timeCadenceSelectionListener = timeCadenceSelectionListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val gson = GsonUtils.gson()
            timeCadenceTitleList = requireNotNull(
                gson.fromJson<List<String>>(requireNotNull(it.getString(ARG_TIME_CADENCE_LIST)))
            )
            selectedTimeCadenceTitle = requireNotNull(it.getString(ARG_SELECTED_TIME_CADENCE))
            render(timeCadenceTitleList, selectedTimeCadenceTitle)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.time_cadence_selection_bottom_sheet, container, false)

        radioGroup = view.findViewById(R.id.time_cadence_radio_group)
        return view
    }

    fun render(timeCadenceTitleList: List<String>, selectedTimeCadenceTitle: String) {
        timeCadenceTitleList.forEachIndexed { position, _ ->
            val timeCadenceTitle = timeCadenceTitleList[position]
            val radioButton = layoutInflater.inflate(
                R.layout.item_time_cadence_radio_button, radioGroup, false
            ) as RadioButton

            radioButton.text = timeCadenceTitle
            radioButton.id = position
            radioGroup.addView(radioButton)
            radioButton.isChecked = selectedTimeCadenceTitle == timeCadenceTitle
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val newlySelectedTimeCadenceTitle = group.findViewById<RadioButton>(checkedId).text.toString()
            timeCadenceSelectionListener?.onTimeCadenceSelected(newlySelectedTimeCadenceTitle)
            dismiss()
        }
    }

    companion object {
        val TAG: String = TimeCadenceSelectionBottomSheetDialog::class.java.simpleName

        const val ARG_TIME_CADENCE_LIST = "time_cadence_list"
        const val ARG_SELECTED_TIME_CADENCE = "selected_time_cadence"

        fun newInstance(
            timeCadenceTitleList: List<String>,
            selectedTimeCadenceTitle: String,
        ): TimeCadenceSelectionBottomSheetDialog {
            val gson = GsonUtils.gson()

            val args = Bundle()
            args.putString(ARG_TIME_CADENCE_LIST, gson.toJson(timeCadenceTitleList))
            args.putString(ARG_SELECTED_TIME_CADENCE, selectedTimeCadenceTitle)

            return TimeCadenceSelectionBottomSheetDialog().apply { arguments = args }
        }
    }

    interface TimeCadenceSelectionListener {
        fun onTimeCadenceSelected(timeCadenceTitle: String)
    }
}
