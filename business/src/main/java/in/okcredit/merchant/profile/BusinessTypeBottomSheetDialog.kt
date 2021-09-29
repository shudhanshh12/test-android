package `in`.okcredit.merchant.profile

import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.BusinessTypeListener
import `in`.okcredit.merchant.merchant.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyRecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BusinessTypeBottomSheetDialog : BottomSheetDialogFragment() {

    private var businessTypeListener: BusinessTypeListener? = null
    private var selectedBusinessTypeId: String = ""
    private var businessTypes = listOf<BusinessType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // val binding = BusinessTypeBottomSheetLayoutBinding.inflate(LayoutInflater.from(context), container, false)
        val view = inflater.inflate(R.layout.business_type_bottom_sheet_layout, container, false)

        val recyclerView = view.findViewById<EpoxyRecyclerView>(R.id.recycler_view)
        recyclerView.withModels {
            businessTypes.forEach {
                businessTypeItemView {
                    id(it.id)
                    businessType(it)
                    checked(selectedBusinessTypeId == it.id)
                    businessTypeImage(it.image_url)
                    listener(object : BusinessTypeItemView.SelectBusinessTypesListener {
                        override fun onSelectBusinessType(type: BusinessType) {
                            selectedBusinessTypeId = type.id
                            dismiss()
                            businessTypeListener?.onSelectBusinessType(type)
                            recyclerView.requestModelBuild()
                        }
                    })
                }
            }
        }

        recyclerView.requestModelBuild()

        return view
    }

    fun initialise(
        businessTypeListener: BusinessTypeListener,
        selectedBusinessTypeId: String,
        businessTypes: List<BusinessType>
    ) {
        this.businessTypeListener = businessTypeListener
        this.selectedBusinessTypeId = selectedBusinessTypeId
        this.businessTypes = businessTypes
    }

    companion object {

        val TAG: String? = BusinessTypeBottomSheetDialog::class.java.simpleName

        fun newInstance(): BusinessTypeBottomSheetDialog {
            return BusinessTypeBottomSheetDialog()
        }
    }
}
