package tech.okcredit.home.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import tech.okcredit.home.R

class GlobalInfoWithOneButtonDialog {
    interface Listener {
        fun onSuccess()
        fun onCancel()
    }

    companion object {

        public fun show(
            drawable: Drawable,
            titleString: String,
            descString: String,
            buttonTitle: String,
            buttonIcon: Drawable,
            activity: Activity,
            listener: Listener?
        ): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_info_one_button_global, null)
            builder.setView(dialogView)

            val image = dialogView.findViewById<ImageView>(R.id.image)
            val desc = dialogView.findViewById<TextView>(R.id.desc)
            val title = dialogView.findViewById<TextView>(R.id.title)

            val buttonText = dialogView.findViewById<TextView>(R.id.bottom_text)
            val buttonImage = dialogView.findViewById<ImageView>(R.id.bottom_image)
            val button = dialogView.findViewById<CardView>(R.id.button)
            val cancelButton = dialogView.findViewById<ImageView>(R.id.cancel)

            image.setImageDrawable(drawable)
            title.text = titleString
            desc.text = descString
            buttonImage.setImageDrawable(buttonIcon)
            buttonText.text = buttonTitle

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            button.setOnClickListener {
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onSuccess()
                }
            }

            cancelButton.setOnClickListener {
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
