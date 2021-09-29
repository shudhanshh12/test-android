package com.camera.camera_preview_images

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.camera.R
import kotlinx.android.synthetic.main.sample_camera_images_preview.view.*
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.camera_contract.DummyItem
import tech.okcredit.camera_contract.MultiScreenItem

/**
 * This class is responsible for showing of images selected from camera and provide
 * a add more images option
 */
class CameraImagesPreview : LinearLayout, SmallPreviewHolder.Listener, DummyHolder.Listener {
    private lateinit var preview: PreviewInteractor
    private lateinit var previewItem: PreviewItemInteractor
    private lateinit var thumbNailAdapter: SmallPreviewAdapter
    private lateinit var previewAdapter: BigPreviewAdapter
    private var prevPosition = 0
    private var deletedImageList = ArrayList<CapturedImage>()
    private var finalList = ArrayList<MultiScreenItem>()
    private var finalSelectedImageList = ArrayList<CapturedImage>()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(attrs: AttributeSet? = null) {
        LayoutInflater.from(context).inflate(R.layout.sample_camera_images_preview, this, true)
        var addMoreItemId: Int? = null

        attrs?.let {
            val typeArray = context?.obtainStyledAttributes(attrs, R.styleable.add_more_attr)
            addMoreItemId = typeArray?.getResourceId(R.styleable.add_more_attr_add_more, 0)
            if (addMoreItemId == 0) addMoreItemId = null
        }

        preview_vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                thumbNailAdapter.selectedPosition = position
                thumbnail_rv.scrollToPosition(position)
                thumbNailAdapter.notifyDataSetChanged()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position > 0) {
                    prevPosition = position
                }
                if (position == 0 && prevPosition != 0) {
                    prevPosition = 0
                    preview.onFirstItemLeftScrolled()
                }
            }
        })

        preview_vp.setOnTouchListener { v, event ->
            return@setOnTouchListener false
        }

        previewAdapter = BigPreviewAdapter(finalList)
        preview_vp.apply {
            adapter = previewAdapter
        }

        thumbNailAdapter = SmallPreviewAdapter(finalList, this, addMoreItemId)
        thumbnail_rv.apply {
            adapter = thumbNailAdapter
        }
    }

    fun setImages(images: ArrayList<CapturedImage>) {
        finalList.clear()
        finalList.addAll(images)
        finalList.add(0, DummyItem())
        preview_vp.offscreenPageLimit = finalList.size
        previewAdapter.notifyDataSetChanged()
        thumbNailAdapter.notifyDataSetChanged()
        preview_vp.setCurrentItem(1, false)
    }

    fun setActiveItem(position: Int) {
        // adding 1 beacuse of dummy item
        preview_vp.setCurrentItem(position + 1, false)
    }

    fun onImageDeleted(): CapturedImage? {
        if (finalList.get(preview_vp.currentItem) is CapturedImage) {
            val capturedPhoto = finalList.get(preview_vp.currentItem) as CapturedImage
            deletedImageList.add(capturedPhoto)
            finalSelectedImageList.remove(capturedPhoto)
            finalList.remove(capturedPhoto)
            previewAdapter.notifyItemRemoved(preview_vp.currentItem)
            thumbNailAdapter.notifyItemRemoved(preview_vp.currentItem)
            Handler().postDelayed(
                {
                    thumbNailAdapter.selectedPosition = preview_vp.currentItem
                    thumbNailAdapter.notifyItemChanged(thumbNailAdapter.selectedPosition)
                },
                200
            )

            if (finalList.size == 0) {
                preview.onLastImageDeletion()
            }
            return capturedPhoto
        }
        return null
    }

    fun setListener(preview: PreviewInteractor) {
        this.preview = preview
    }

    fun setItemListener(previewItem: PreviewItemInteractor) {
        this.previewItem = previewItem
    }

    interface PreviewInteractor {
        fun onLastImageDeletion()
        fun onFirstItemLeftScrolled()
        fun onCameraClicked()
    }

    interface PreviewItemInteractor {
        fun onThumbnailClicked(capturedPic: CapturedImage, capturedPosition: Int)
    }

    override fun onThumbnailClicked(capturedPic: CapturedImage, capturedPosition: Int) {
        preview_vp.setCurrentItem(capturedPosition, true)
        if (this::previewItem.isInitialized) {
            previewItem.onThumbnailClicked(capturedPic, capturedPosition)
        }
    }

    override fun onCameraClicked() {
        this.preview.onCameraClicked()
    }

    fun deleteImage(): CapturedImage? {
        return if (finalList.isNullOrEmpty() || (finalList.isNullOrEmpty().not() && finalList.size == 1)) {
            preview.onLastImageDeletion()
            null
        } else {
            onImageDeleted()
        }
    }

    fun getDataSet(): ArrayList<MultiScreenItem> {
        return finalList
    }
}
