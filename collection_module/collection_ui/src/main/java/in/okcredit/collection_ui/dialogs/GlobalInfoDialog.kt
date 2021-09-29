package `in`.okcredit.collection_ui.dialogs

import `in`.okcredit.collection_ui.R
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class GlobalInfoDialog {
    interface Listener {
        fun onSuccess()
        fun onCancel()
    }

    companion object {

        fun show(
            drawable: Drawable,
            titleString: String,
            headingString: String,
            descString: String,
            activity: Activity,
            listener: Listener?,
        ): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_info_global, null)
            builder.setView(dialogView)

            val image = dialogView.findViewById<ImageView>(R.id.image)
            val heading = dialogView.findViewById<TextView>(R.id.heading)
            val desc = dialogView.findViewById<TextView>(R.id.desc)
            val title = dialogView.findViewById<TextView>(R.id.title)
            val ok = dialogView.findViewById<Button>(R.id.ok)
            val calcel = dialogView.findViewById<Button>(R.id.cancel)

            image.setImageDrawable(drawable)
            title.text = titleString
            desc.text = descString
            heading.text = headingString

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            ok.setOnClickListener {
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onSuccess()
                }
            }

            calcel.setOnClickListener {
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onCancel()
                }
            }

            if (alertDialog.window != null) {
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            alertDialog.setCancelable(true)

            return alertDialog
        }
    }
}
