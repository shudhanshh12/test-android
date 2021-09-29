package com.camera.gallery

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.camera.R
import io.reactivex.subjects.PublishSubject

class GalleryItemViewHolder(
    val inflate: View,
    val photoSet: LinkedHashSet<com.camera.models.models.Picture>,
    val adapterCallback: GalleryItemAdapter.AdapterCallback
) : RecyclerView.ViewHolder(inflate) {
    private var galleryImage: ImageView
    private var selected: ImageView
    private var contianer: RelativeLayout
    private var clickSubject = PublishSubject.create<Unit>()

    init {
        galleryImage = inflate.findViewById(R.id.gallery_image)
        selected = inflate.findViewById(R.id.selectd)
        contianer = inflate.findViewById(R.id.container)
    }

    fun bind(picture: com.camera.models.models.Picture) {
        Glide.with(inflate.context).load(picture.path).transform(CenterCrop(), RoundedCorners(10)).into(galleryImage)
        contianer.setOnClickListener {
            clickSubject.onNext(Unit)
            picture.selected = !picture.selected
            if (picture.selected) {
                photoSet.add(picture)
                if (photoSet.size != 1) {
                    performSelection(picture)
                }
            } else {
                photoSet.remove(picture)
                performSelection(picture)
            }

            adapterCallback.onPhotoClicked(photoSet)
        }
        performSelection(picture)
    }

    private fun performSelection(picture: com.camera.models.models.Picture) {
        if (picture.selected) {
            selected.visibility = View.VISIBLE
        } else {
            selected.visibility = View.GONE
        }
    }
}
