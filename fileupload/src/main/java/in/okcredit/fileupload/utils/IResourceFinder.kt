package `in`.okcredit.fileupload.utils

import android.graphics.drawable.Drawable

interface IResourceFinder {

    fun getColour(colorId: Int): Int

    fun getDrawable(drawableId: Int): Drawable

    fun getDimension(dimensionId: Int): Int

    fun getString(stringId: Int): String
}
