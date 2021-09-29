package `in`.okcredit.fileupload.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject

class ResourceFinderImp @Inject constructor(private val context: Context) : IResourceFinder {

    // --------------------------------- Color ------------------------------------------------------
    override fun getColour(colorId: Int): Int {
        return ContextCompat.getColor(context, colorId)
    }

    // --------------------------------- Drawable ---------------------------------------------------
    override fun getDrawable(drawableId: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(drawableId, null)
        } else {
            context.resources.getDrawable(drawableId)
        }
    }

    // --------------------------------- Dimension --------------------------------------------------
    override fun getDimension(dimensionId: Int): Int {
        return context.resources.getDimension(dimensionId).toInt()
    }

    // --------------------------------- String -----------------------------------------------------
    override fun getString(stringId: Int): String {
        return context.getString(stringId)
    }
}
