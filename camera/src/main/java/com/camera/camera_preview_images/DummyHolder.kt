package com.camera.camera_preview_images

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DummyHolder(inflate: View, var interactionListener: Listener? = null) :
    RecyclerView.ViewHolder(inflate) {

    init {
        inflate.setOnClickListener {
            interactionListener?.onCameraClicked()
        }
    }

    interface Listener {
        fun onCameraClicked()
    }
}
