package tech.okcredit.android.base.extensions

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.util.TypedValue
import android.widget.EditText
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.findNavController
import tech.okcredit.android.base.crashlytics.RecordException
import java.util.*
import javax.inject.Provider

fun Fragment.replaceFragment(
    fragment: Fragment,
    layoutId: Int,
    addToBackStack: Boolean = false,
    backStackName: String = "",
) {
    childFragmentManager.beginTransaction().let {
        it.replace(layoutId, fragment)
        if (addToBackStack) {
            it.addToBackStack(backStackName)
        }
        it.commitAllowingStateLoss()
    }
}

fun FragmentManager.addFragmentToFragmentManager(
    fragment: Fragment,
    tag: String,
    addToBackStack: Boolean = false,
) {
    beginTransaction().let {
        it.add(
            fragment,
            tag
        )
        if (addToBackStack) it.addToBackStack(tag)
        it.commitAllowingStateLoss()
    }
}

fun FragmentManager.replaceFragment(
    fragment: Fragment,
    @IdRes holder: Int,
    addToBackStack: Boolean = true,
    commitNow: Boolean = false,
) {
    val ft = this.beginTransaction()
    ft.replace(holder, fragment, fragment.javaClass.simpleName)
    if (addToBackStack)
        ft.addToBackStack(fragment.javaClass.simpleName)
    if (commitNow) ft.commitNow() else ft.commit()
}

fun FragmentManager.addFragment(
    fragment: Fragment,
    @IdRes holder: Int,
    hideFragment: Fragment? = null,
    addToBackStack: Boolean = true,
) {
    val ft = beginTransaction()
    hideFragment?.let { ft.hide(hideFragment) }
    ft.add(holder, fragment, fragment.javaClass.simpleName)
    if (addToBackStack) ft.addToBackStack(fragment.javaClass.simpleName)
    ft.commit()
}

fun Fragment.removeFragment(fragment: Fragment) {
    childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
}

fun Fragment.shortToast(message: CharSequence) = requireContext().shortToast(message)

fun Fragment.shortToast(@StringRes resId: Int) = requireContext().shortToast(resId)

fun Fragment.longToast(message: CharSequence) = requireContext().longToast(message)

fun Fragment.longToast(@StringRes resId: Int) = requireContext().longToast(resId)

fun Fragment.getColorCompat(@ColorRes color: Int) = requireContext().getColorCompat(color)

fun Fragment.getDrawableCompact(@DrawableRes drawableId: Int) = requireContext().getDrawableCompact(drawableId)

fun Fragment.doesPackageExist(packageName: String): Boolean {
    return requireContext().doesPackageExist(packageName)
}

fun Fragment.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true,
): Int {
    requireContext().getColorFromAttr(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Fragment.getColorStateListCompat(
    @ColorRes colorId: Int,
): ColorStateList? {
    return ContextCompat.getColorStateList(this.requireContext(), colorId)
}

fun Fragment.getLocalisedString(language: String, @StringRes id: Int): SpannableString {
    return requireContext().getLocalisedString(language, id)
}

fun Fragment.getDimension(@DimenRes id: Int): Float {
    return getDimension(id)
}

fun Fragment.hideSoftKeyboard(editText: EditText? = null) {
    requireActivity().hideSoftKeyboard(editText)
}

fun Fragment.showSoftKeyboard(editText: EditText) {
    requireActivity().showSoftKeyboard(editText)
}

fun Fragment.isCurrentDestination(@IdRes destinationId: Int): Boolean {
    return findNavController(this).currentDestination?.id == destinationId
}

fun Fragment.executeIfFragmentViewAvailable(block: () -> Unit) {
    // Log non-fatal if the fragment's view is unavailable
    try {
        viewLifecycleOwner
    } catch (e: IllegalStateException) {
        RecordException.recordException(
            IllegalStateException(
                e.message + " - for Fragment -> ${this.classType}"
            )
        )
        return
    }

    // else execute block
    block()
}

fun Fragment.navigate(directions: NavDirections) {
    findNavController(this).navigate(directions)
}

fun Fragment.navigate(resId: Int, args: Bundle? = null) {
    findNavController(this).navigate(resId, args)
}

fun Fragment.updateBaseLanguage(language: String) {
    val config = Configuration(resources.configuration)
    config.setLocale(Locale(language))
    this.requireActivity().baseContext.resources.updateConfiguration(
        config,
        this.requireActivity().baseContext.resources.displayMetrics
    )
}

@SuppressLint("ResourceAsColor")
fun Fragment.setStatusBarColor(@ColorRes colorId: Int) {
    try {
        requireActivity().window?.statusBarColor = getColorCompat(colorId)
    } catch (e: Exception) {
        requireActivity().window?.statusBarColor = colorId
    }
}

fun Fragment.getDisplayMatrix(): Float {
    return requireContext().getDisplayMatrix()
}

fun Fragment.dpToPixel(dp: Float): Float {
    return requireContext().dpToPixel(dp)
}

inline fun <reified T : ViewModel> ViewModelStoreOwner.createViewModel(
    provider: Provider<T>,
) = ViewModelProvider(
    this,
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = provider.get() as T
    }
)[T::class.java]

fun Fragment.getNavigationResult(key: String = "result_") =
    findNavController(this).currentBackStackEntry?.savedStateHandle?.get<Any>(key)

fun Fragment.getNavigationResultLiveData(key: String = "result_") =
    findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<Any>(key)

fun Fragment.setNavigationResult(result: Any, key: String = "result_") {
    findNavController(this).previousBackStackEntry?.savedStateHandle?.set(key, result)
}
