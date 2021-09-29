package `in`.okcredit.shared.base

import `in`.okcredit.shared.R
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily.ROUNDED
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import tech.okcredit.android.base.extensions.dpToPixel

// Bottom-sheet without any collapsed state.
abstract class ExpandedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.BottomSheetMaterialDialogStyleOverKeyboard
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            if (dialogInterface is BottomSheetDialog) {
                val bottomSheet: FrameLayout? = dialogInterface.findViewById(R.id.design_bottom_sheet)
                bottomSheet?.let {
                    val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(it)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        (dialog as? BottomSheetDialog)?.behavior?.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // In the EXPANDED STATE apply a new MaterialShapeDrawable with rounded corner
                    createMaterialShapeDrawable(bottomSheet)?.let { ViewCompat.setBackground(bottomSheet, it) }
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
        })
        return dialog
    }

    /**
     * This is hack added to override default behavior of bottom sheet when it is in expanded state. In new material
     * style corners are not rounded for bottom sheet.
     * Please refer to https://stackoverflow.com/questions/43852562/round-corner-for-bottomsheetdialogfragment/57627229#57627229
     * or https://github.com/material-components/material-components-android/issues/1278 for more info
     */
    internal fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable? {
        // Create a ShapeAppearanceModel with the same shapeAppearanceOverlay used in the style
        if (context == null) return null

        val shapeAppearanceModel = ShapeAppearanceModel.builder(
            context,
            0,
            theme,
        ).setTopLeftCorner(ROUNDED, requireContext().dpToPixel(16f))
            .setTopRightCorner(ROUNDED, requireContext().dpToPixel(16f))
            .build()

        // Create a new MaterialShapeDrawable (you can't use the original MaterialShapeDrawable in the BottomSheet)
        val currentMaterialShapeDrawable = bottomSheet.background as MaterialShapeDrawable
        val newMaterialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        // Copy the attributes in the new MaterialShapeDrawable
        newMaterialShapeDrawable.initializeElevationOverlay(context)
        newMaterialShapeDrawable.fillColor = currentMaterialShapeDrawable.fillColor
        newMaterialShapeDrawable.tintList = currentMaterialShapeDrawable.tintList
        newMaterialShapeDrawable.elevation = currentMaterialShapeDrawable.elevation
        newMaterialShapeDrawable.strokeWidth = currentMaterialShapeDrawable.strokeWidth
        newMaterialShapeDrawable.strokeColor = currentMaterialShapeDrawable.strokeColor
        return newMaterialShapeDrawable
    }
}
