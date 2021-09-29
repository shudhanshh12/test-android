package com.camera.camera_preview_images

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.camera.R
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.camera_contract.MultiScreenItem
import kotlin.collections.ArrayList

class BigPreviewHolder(val inflate: View) : RecyclerView.ViewHolder(inflate) {
    private var imagePreview: ImageView = inflate.findViewById(R.id.image_preview)

    fun bind(
        imagelist: ArrayList<MultiScreenItem>,
        position: Int
    ) {
        val cap = imagelist[position] as CapturedImage
        Glide.with(inflate.context)
            .load(cap.file.path)
            .apply(RequestOptions())
            .into(imagePreview)
    }
}
