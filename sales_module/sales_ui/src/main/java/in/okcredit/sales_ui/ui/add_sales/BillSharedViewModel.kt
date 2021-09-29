import `in`.okcredit.sales_sdk.models.BillModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BillSharedViewModel : ViewModel() {
    val billItems = MutableLiveData<List<BillModel.BillItem>>()

    fun setBilledItems(item: List<BillModel.BillItem>) {
        billItems.value = item
    }
}
