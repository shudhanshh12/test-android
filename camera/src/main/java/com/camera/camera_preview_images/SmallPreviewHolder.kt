package com.camera.camera_preview_images

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.camera.R
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.camera_contract.MultiScreenItem
import java.util.*

class SmallPreviewHolder(val inflate: View, interactionListener: CameraImagesPreview) :
    RecyclerView.ViewHolder(inflate) {
    private lateinit var thumbnailAdapter: SmallPreviewAdapter
    private var thumbnailContainer: RelativeLayout = inflate.findViewById(R.id.thumbnail_container)
    private var capturedPosition: Int = 0
    private lateinit var capturedPic: CapturedImage
    private var imagePreview: ImageView = inflate.findViewById(R.id.thumbnail)

    init {
        imagePreview.setOnClickListener {
            thumbnailAdapter.notifyItemChanged(thumbnailAdapter.selectedPosition)
            thumbnailAdapter.selectedPosition = capturedPosition
            interactionListener.onThumbnailClicked(capturedPic, capturedPosition)
            thumbnailAdapter.notifyItemChanged(capturedPosition)
        }
    }

    fun bind(
        imageList: ArrayList<MultiScreenItem>,
        position: Int,
        selectedPosition: Int,
        thumbnailAdapter: SmallPreviewAdapter
    ) {
        capturedPic = imageList[position] as CapturedImage
        this.thumbnailAdapter = thumbnailAdapter
        capturedPosition = position
        if (selectedPosition == capturedPosition) {
            thumbnailContainer.background = imagePreview.context.getDrawable(R.drawable.border_reactange_rounded_corner)
        } else thumbnailContainer.background = null
        val requestOptions = RequestOptions().transforms(
            CenterCrop(),

            RoundedCorners(DimensionUtil.dp2px(inflate.context, 4.0f).toInt())
        )

        Glide.with(inflate.context)
            .load(capturedPic.file.path)
            .apply(requestOptions)
            .into(imagePreview)
    }

    interface Listener {
        fun onThumbnailClicked(capturedPic: CapturedImage, capturedPosition: Int)
    }
}
