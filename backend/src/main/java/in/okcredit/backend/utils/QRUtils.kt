package `in`.okcredit.backend.utils

import android.content.Context
import android.widget.ImageView
import io.reactivex.disposables.Disposable

fun ImageView.setQrCode(upiVpa: String, context: Context, width: Int): Disposable =
    QRCodeUtils.getBitmapObservable(upiVpa, context, width).subscribe {
        it?.let {
            this.setImageBitmap(it)
        }
    }
