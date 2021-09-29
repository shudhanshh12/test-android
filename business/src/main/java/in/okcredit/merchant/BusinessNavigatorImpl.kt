package `in`.okcredit.merchant

import `in`.okcredit.merchant.contract.BusinessNavigator
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.BusinessTypeListener
import `in`.okcredit.merchant.profile.BusinessTypeBottomSheetDialog
import `in`.okcredit.merchant.ui.create_business.CreateBusinessDialog
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessDialog
import androidx.fragment.app.FragmentManager
import javax.inject.Inject

class BusinessNavigatorImpl @Inject constructor() : BusinessNavigator {

    override fun showBusinessTypeDialog(
        fragmentManager: FragmentManager,
        businessTypes: List<BusinessType>,
        listener: BusinessTypeListener?,
        selectedBusinessTypeId: String,
    ) {
        val businessTypeBottomSheet = BusinessTypeBottomSheetDialog.newInstance()
        listener?.let {
            businessTypeBottomSheet.initialise(it, selectedBusinessTypeId, businessTypes)
        }

        businessTypeBottomSheet.show(
            fragmentManager,
            BusinessTypeBottomSheetDialog.TAG
        )
    }

    override fun showSwitchBusinessDialog(fragmentManager: FragmentManager, source: String) {
        val switchBusinessBottomSheet = SwitchBusinessDialog.newInstance(source)
        switchBusinessBottomSheet.show(fragmentManager, SwitchBusinessDialog.TAG)
    }

    override fun showCreateBusinessDialog(fragmentManager: FragmentManager) {
        val createBusinessDialog = CreateBusinessDialog()
        createBusinessDialog.show(fragmentManager, CreateBusinessDialog.TAG)
    }
}
