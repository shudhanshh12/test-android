package com.camera.selected_image

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DummyHolder(inflate: View, var interactionListener: MultipleImageActivity? = null) :
    RecyclerView.ViewHolder(inflate) {

    init {
        inflate.setOnClickListener {
            interactionListener?.onCameraClicked()
        }
    }
}
