@file:JvmName("ContextExtensions")

package tech.okcredit.android.base.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.text.SpannableString
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.util.*

fun Context.getDefaultFileProviderAuthority() = "$packageName.provider"

fun Context.getColorDrawable(@ColorRes colorId: Int) = ColorDrawable(ContextCompat.getColor(this, colorId))

fun Context.openRawResource(@RawRes rawId: Int): InputStream = resources.openRawResource(rawId)

fun Context.readRawFie(@RawRes rawId: Int): String = openRawResource(rawId).bufferedReader().use { it.readText() }

fun Context.shortToast(@StringRes resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

fun Context.longToast(@StringRes resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true,
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Context.shortToast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.longToast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.isConnectedToInternet() =
    (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        .activeNetworkInfo?.isConnected
        ?: false

fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.getDrawableCompact(@DrawableRes drawableId: Int) = ContextCompat.getDrawable(this, drawableId)

fun Context.doesPackageExist(packageName: String): Boolean {
    return packageManager.getInstalledApplications(0)
        .firstOrNull { info -> info.packageName == packageName } != null
}

fun Context.getLocalisedString(language: String, @StringRes stringId: Int): SpannableString {
    val config = Configuration(this.resources.configuration)
    config.setLocale(Locale(language))
    return SpannableString(createConfigurationContext(config).getText(stringId).toString())
}

fun Context.getResourcesByLocale(locale: String?): Resources {
    val configuration = Configuration(resources.configuration)
    if (!locale.isNullOrEmpty()) {
        configuration.setLocale(Locale(locale))
    }
    return createConfigurationContext(configuration).resources
}

fun Context.getDimension(@DimenRes id: Int): Float {
    return this.resources.getDimension(id)
}

fun Context.isAppPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(
            packageName,
            PackageManager
                .GET_ACTIVITIES
        )
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.getDisplayMatrix(): Float {
    return this.resources.displayMetrics.widthPixels.toFloat()
}

fun Context.dpToPixel(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics)
}

fun Context.copyToClipboard(msg: String): ClipboardManager? {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clipData = ClipData.newPlainText(null, msg)
    clipboardManager?.setPrimaryClip(clipData)
    return clipboardManager
}

fun Context.colorStateList(color: Int): ColorStateList {
    return ColorStateList.valueOf(this.getColorCompat(color))
}

fun Context.isPermissionGranted(permission: String) =
    checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
