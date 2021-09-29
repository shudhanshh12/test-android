package `in`.okcredit.collection_ui.utils

import `in`.okcredit.backend.utils.QRCodeUtils
import android.content.Context
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyControllerAdapter
import io.reactivex.disposables.Disposable

object CollectionUtils {

    fun isValidIFSC(ifsc: String): Boolean {
        val regExpIFSC: String = when {
            ifsc.isEmpty() -> {
                return true
            }
            ifsc.length <= 4 -> {
                "^[A-Z]{${ifsc.length}}$"
            }
            ifsc.length == 5 -> {
                "^[A-Z]{4}[0]$"
            }
            else -> {
                "^[A-Z]{4}[0][A-Z0-9]{${ifsc.length - 5}}$"
            }
        }

        return ifsc.matches(regExpIFSC.toRegex())
    }

    fun isInvalidBankDetails(accountNumber: String, ifsc: String, isValidIfsc: Boolean): Boolean {
        return (
            accountNumber.length < 9 || ifsc.isEmpty() ||
                ifsc.length != 11 || isValidIfsc.not()
            )
    }

    fun isUpiEmpty(upi: String): Boolean {
        return upi.isEmpty()
    }
}

fun ImageView.setQrCode(upiVpa: String, context: Context, width: Int): Disposable =
    QRCodeUtils.getBitmapObservable(upiVpa, context, width).subscribe {
        it?.let {
            this.setImageBitmap(it)
        }
    }

fun EpoxyControllerAdapter.scrollToTopOnItemInsertItem(recyclerView: RecyclerView) {
    this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            recyclerView.scrollToPosition(0)
        }
    })
}
