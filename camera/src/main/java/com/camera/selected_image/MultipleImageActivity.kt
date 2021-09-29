package com.camera.selected_image

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.fileupload.usecase.IImageLoader
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import androidx.viewpager2.widget.ViewPager2
import com.camera.CameraActivity
import com.camera.R
import com.camera.makeFullscreen
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_multiple_image.*
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.camera_contract.DummyItem
import tech.okcredit.camera_contract.MultiScreenItem
import javax.inject.Inject

class MultipleImageActivity : BaseLanguageActivity(), ThumbNailViewHolder.Listener {
    @Inject
    internal lateinit var imageLoader: IImageLoader

    @Inject
    internal lateinit var tracker: Tracker

    private var flow: String? = null
    private var relation: String? = null
    private var type: String? = null
    private var screen: String? = null
    private var mobile: String? = null
    private var account: String? = null
    private var txnId: String? = null

    internal var prevPosition = 0
    internal lateinit var thumbNailAdapter: ThumbnailAdapter
    private lateinit var previewAdapter: PreviewAdapter
    private var selectedImages: CapturedImage? = null
    private lateinit var imagelist: java.util.ArrayList<CapturedImage>
    private var tempImagelist = java.util.ArrayList<CapturedImage>()
    private var tempDeletedImageList = java.util.ArrayList<CapturedImage>()
    private var deletedImageList = ArrayList<CapturedImage>()
    private var addedImages = ArrayList<CapturedImage>()
    private var finalList = ArrayList<MultiScreenItem>()
    private var finalSelectedImageList = ArrayList<CapturedImage>()
    private val CAMERA_START_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_image)
        getDataFromIntent()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        previewAdapter = PreviewAdapter(finalList, imageLoader)
        preview_vp.offscreenPageLimit = finalList.size
        preview_vp.apply {
            adapter = previewAdapter
        }

        thumbNailAdapter = ThumbnailAdapter(finalList, this, imageLoader)
        thumbnail_rv.apply {
            adapter = thumbNailAdapter
        }

        if (selectedImages != null) {
            val position = imagelist.indexOfFirst { it.file.path == this.selectedImages!!.file.path } + 1
            preview_vp.currentItem = position
            thumbNailAdapter.selectedPosition = position
            thumbnail_rv.scrollToPosition(position)
        }

        delete.setOnClickListener {
            if (finalList.get(preview_vp.currentItem) is CapturedImage) {
                tracker.trackDeleteReceipt(flow, relation, type, "Edit Screen", account, txnId)
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

                if (imagelist.size == 0) {
                    startOkcCameraActivity()
                }
            }
        }

        back.setOnClickListener {
            onBackPressed()
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
                    startOkcCameraActivity()
                }
            }
        })

        preview_vp.setOnTouchListener { _, _ ->
            return@setOnTouchListener false
        }
    }

    override fun onResume() {
        super.onResume()
        makeFullscreen(this)
    }

    internal fun startOkcCameraActivity() {
        tempImagelist.addAll(imagelist)
        tempDeletedImageList.addAll(deletedImageList)
        val filteredList = ArrayList(tempImagelist.minus(tempDeletedImageList))
        tracker.trackAddReceiptStarted(flow, relation, type, "Edit Screen", account, txnId)
        startActivityForResult(
            CameraActivity.createSelectedImageIntent(
                this@MultipleImageActivity,
                filteredList,
                Utils.SCREEN_SOURCE.MULTI_IMAGE_ACTIVITY,
                flow,
                relation,
                type,
                screen,
                account,
                mobile,
                filteredList.size,
                txnId
            ),
            CAMERA_START_REQUEST_CODE
        )
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putExtra("selectedImages", imagelist)
        returnIntent.putExtra("deletedImages", deletedImageList)
        returnIntent.putExtra("addedImages", addedImages)
        returnIntent.putExtra(FINAL_SELECTED_IMAGE_LIST, finalSelectedImageList)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun getDataFromIntent() {
        finalList.add(0, DummyItem())
        imagelist = intent.getSerializableExtra("list") as ArrayList<CapturedImage>
        finalSelectedImageList.addAll(imagelist)
        imagelist.let {
            finalList.addAll(imagelist)
        }
        intent.getSerializableExtra("selectedImages")?.let {
            selectedImages = intent.getSerializableExtra("selectedImages") as CapturedImage
        }
        flow = intent.getStringExtra("flow")
        relation = intent.getStringExtra("relation")
        type = intent.getStringExtra("type")
        screen = intent.getStringExtra("screen")
        mobile = intent.getStringExtra("mobile")
        txnId = intent.getStringExtra("txnId")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_START_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val returnIntent = Intent()
                if (data?.getSerializableExtra("addedImages") != null) {
                    addedImages = data.getSerializableExtra("addedImages") as ArrayList<CapturedImage>
                    finalSelectedImageList.addAll(addedImages)
                }
                returnIntent.putExtra("selectedImages", imagelist)
                returnIntent.putExtra("addedImages", addedImages)
                returnIntent.putExtra("deletedImages", deletedImageList)
                returnIntent.putExtra(FINAL_SELECTED_IMAGE_LIST, finalSelectedImageList)

                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                val positionToScroll = prevPosition + 1
                preview_vp.currentItem = positionToScroll
                thumbNailAdapter.selectedPosition = positionToScroll
                thumbnail_rv.scrollToPosition(positionToScroll)
                if (finalSelectedImageList.size == 0) {
                    onBackPressed()
                }
            }
        }
    }

    companion object {

        const val FINAL_SELECTED_IMAGE_LIST = "finalSelectedImagelist"

        @JvmStatic
        fun createSelectedImagesIntent(
            context: Context,
            selectedImages: CapturedImage,
            transactionImageList: List<CapturedImage>,
            flow: String?,
            relation: String?,
            type: String?,
            screen: String?,
            account: String?,
            mobile: String?,
            txnId: String? = null
        ): Intent {
            val intent = Intent(context, MultipleImageActivity::class.java)
            intent.putExtra("list", ArrayList(transactionImageList))
            intent.putExtra("selectedImages", selectedImages)
            intent.putExtra("flow", flow)
            intent.putExtra("relation", relation)
            intent.putExtra("type", type)
            intent.putExtra("screen", screen)
            intent.putExtra("mobile", mobile)
            intent.putExtra("account", account)
            intent.putExtra("txnId", txnId)
            return intent
        }
    }

    override fun onThumbnailClicked(capturedPic: CapturedImage, capturedPosition: Int) {
        preview_vp.setCurrentItem(capturedPosition, true)
    }

    fun onCameraClicked() {
        startOkcCameraActivity()
    }
}
