package com.camera.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.camera.R
import com.camera.models.models.Picture
import java.util.*

class GalleryItemAdapter(
    val photoSet: LinkedHashSet<Picture>,
    val adapterCallback: AdapterCallback,
    val itemId: Int? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listPhotos = ArrayList<Picture>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GalleryItemViewHolder(

            LayoutInflater.from(parent.context).inflate(itemId ?: R.layout.galleryholder, parent, false),
            photoSet,
            adapterCallback
        )
    }

    override fun getItemCount(): Int {
        return listPhotos.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GalleryItemViewHolder) {
            val galleryItemViewHolder = holder
            galleryItemViewHolder.bind(listPhotos[position])
        }
    }

    fun setData(listPhotos: ArrayList<Picture>) {
        this.listPhotos = listPhotos
        notifyDataSetChanged()
    }

    fun addItem(position: Int, picture: Picture) {
        listPhotos.add(position, picture)
        notifyItemInserted(position)
    }

    override fun getItemId(position: Int): Long {
        return listPhotos[position].path.hashCode().toLong()
    }

    fun deSelect(deletedPhoto: Picture?) {
        val index = listPhotos.indexOf(deletedPhoto)
        listPhotos.get(index).selected = false
        notifyItemChanged(index)
    }

    interface AdapterCallback {
        fun onPhotoClicked(photoSet: LinkedHashSet<Picture>)
    }
}
