package com.camera.gallery

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.camera.R
import com.camera.models.models.Picture
import kotlinx.android.synthetic.main.gallery.view.*

class GalleryView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs), GalleryItemAdapter.AdapterCallback {

    private var mLayoutManager: LinearLayoutManager
    private var galleryAdapter: GalleryItemAdapter
    private var photoSet: java.util.LinkedHashSet<Picture> = LinkedHashSet()
    private var endReached: Boolean = false
    private var loading: Boolean = false
    private var pastVisibleItems: Int = 0
    private var totalItemCount: Int = 0
    private var visibleItemCount: Int = 0
    private val listOfAllImages = ArrayList<Picture>()
    private val REQUEST_PERMISSIONS: Int = 1
    private var offset = 0
    private var photoLimit = "50"
    private var total = 0
    private var lstId = Long.MAX_VALUE.toString()

    init {

        LayoutInflater.from(context).inflate(R.layout.gallery, this, true)
        val typeArray = context?.obtainStyledAttributes(attrs, R.styleable.gallery_view)
        var adapterItemId = typeArray?.getResourceId(R.styleable.gallery_view_item_view, 0)
        if (adapterItemId == 0) adapterItemId = null

        mLayoutManager = LinearLayoutManager(context)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        galleryAdapter = GalleryItemAdapter(photoSet, this, adapterItemId)
        list.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()

                    if (!loading && !endReached) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 20) {
                            loading = false
                            getPhotos()
                        }
                    }
                }
            })
            adapter = galleryAdapter
        }
        checkGalleryPermission()
        typeArray?.recycle()
    }

    private fun checkGalleryPermission() {
        getActivityFormContext(context)?.let {
            if (ContextCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        it.applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSIONS
                )
            } else {
                getPhotos()
            }
        }
    }

    private fun getPhotos() {
        getActivityFormContext(context)?.runOnUiThread {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                val columns = arrayOf(
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.MediaColumns._ID
                )
                val orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC" + " LIMIT " + photoLimit
                val selectionArgs = arrayOf(lstId)
                val imageCursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    MediaStore.MediaColumns._ID + "<?", selectionArgs, orderBy
                )
                val count = imageCursor?.count ?: 0
                if (count < photoLimit.toInt()) {
                    endReached = true
                }
                total += count

                val columnIndexData = imageCursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA) ?: 0
                imageCursor?.let {
                    while (imageCursor.moveToNext()) {
                        val absolutePathOfImage = imageCursor.getString(columnIndexData)
                        listOfAllImages.add(Picture(absolutePathOfImage))
                        lstId = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    }

                    offset = total - 1
                    galleryAdapter.setData(listOfAllImages)
                    imageCursor.close()
                }
            } else {
                checkGalleryPermission()
            }
        }
    }

    private fun getActivityFormContext(context: Context?) = context as? Activity

    override fun onPhotoClicked(photoSet: java.util.LinkedHashSet<Picture>) {
        val arrylist = ArrayList(photoSet)
        val photoIntent = Intent(GalleryUtils.INTENTFILTER.selectedPhtots)
        photoIntent.putExtra(GalleryUtils.LOCALBROADCAST.PHOTOS, arrylist)
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(photoIntent)
    }

    fun addCameraImage(position: Int, picture: Picture) {
        galleryAdapter.addItem(position, picture)
        list.scrollToPosition(position)
    }

    fun notifyDataSetChanged() {
        galleryAdapter.notifyDataSetChanged()
    }

    fun removeItem(deletedPhoto: Picture?) {
        photoSet.remove(deletedPhoto)
        galleryAdapter.deSelect(deletedPhoto)
        onPhotoClicked(photoSet)
    }

    fun getList(): LinkedHashSet<Picture> {
        return galleryAdapter.photoSet
    }

    fun setPictureSelected(canSelect: Boolean, picture: Picture) {

        if (!galleryAdapter.photoSet.contains(picture)) {
            if (canSelect) {
                galleryAdapter.photoSet.add(picture)
                picture.selected = true
                notifyDataSetChanged()
            }
        }
    }
}
