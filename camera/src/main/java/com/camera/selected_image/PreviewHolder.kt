package com.camera.selected_image

import `in`.okcredit.fileupload.usecase.IImageLoader
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.camera.R
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.camera_contract.MultiScreenItem

class PreviewHolder(val inflate: View) : RecyclerView.ViewHolder(inflate) {
    private var imagePreview: ImageView = inflate.findViewById(R.id.image_preview)

    fun bind(imagelist: ArrayList<MultiScreenItem>, position: Int, imageLoader: IImageLoader) {
        val cap = imagelist[position] as CapturedImage
        imageLoader.context(inflate.context)
            .load(cap.file.path)
            .apply(RequestOptions())
            .into(imagePreview)
            .priority(Priority.HIGH)
            .buildNormal()
    }
}
