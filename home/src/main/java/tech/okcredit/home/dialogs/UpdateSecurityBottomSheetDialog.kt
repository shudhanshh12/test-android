package tech.okcredit.home.dialogs

import android.app.Activity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import tech.okcredit.home.R

object UpdateSecurityBottomSheetDialog {

    interface Listener {
        fun onSecurityEnabled()
        fun onFocalArea()
        fun onDismissed()
    }

    private var userCancelled = true

    fun showUpdate(context: Activity, listener: Listener) {

        val layoutInflater = context.layoutInflater
        val dialog = BottomSheetDialog(context)
        val bottomSheet = layoutInflater.inflate(R.layout.applock_update_botton_sheet, null)
        dialog.setContentView(bottomSheet)

        initViews(dialog, listener)

        dialog.show()
    }

    private fun initViews(dialog: BottomSheetDialog, listener: Listener) {

        val mbUpdateSecurity = dialog.findViewById<MaterialButton>(R.id.mbUpdateSecurity)
        val clContent = dialog.findViewById<ConstraintLayout>(R.id.clContent)

        mbUpdateSecurity?.setOnClickListener {
            userCancelled = false
            listener.onSecurityEnabled()
            dialog.dismiss()
        }
        clContent?.setOnClickListener {
            listener.onFocalArea()
        }
        dialog.setOnDismissListener {

            if (userCancelled) {
                listener.onDismissed()
            }
        }
    }
}
