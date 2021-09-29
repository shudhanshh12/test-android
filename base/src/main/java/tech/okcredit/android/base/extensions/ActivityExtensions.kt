package tech.okcredit.android.base.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.*

fun Activity.hideSoftKeyboard(editText: EditText? = null) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val focus = currentFocus
    if (focus != null) {
        inputMethodManager.hideSoftInputFromWindow(focus.windowToken, 0)
    } else {
        if (editText != null)
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        else
            inputMethodManager.hideSoftInputFromWindow(View(this).windowToken, 0)
    }
}

fun Activity.showSoftKeyboard(editText: EditText) {
    val inputMethodManager =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    editText.requestFocus()
    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun AppCompatActivity.findFragmentById(@IdRes resId: Int) =
    supportFragmentManager.findFragmentById(resId)

fun Activity.updateLanguage(language: String) {
    val locale = Locale(language)

    // Update activity configuration
    updateConfiguration(this, locale)

    // Update application configuration
    updateConfiguration(applicationContext, locale)
}

private fun updateConfiguration(context: Context, locale: Locale) {
    context.resources.apply {
        configuration.setLocale(locale)
        updateConfiguration(configuration, displayMetrics)
    }
}

fun Activity.isKeyboardOpen(): Boolean {
    val r = Rect()
    val activityRoot =
        (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
    activityRoot.getWindowVisibleDisplayFrame(r)

    val screenHeight: Int = activityRoot.rootView.height
    val heightDiff: Int = screenHeight - r.height()

    return heightDiff > screenHeight * 0.15
}

fun Activity.setStatusBarColor(@ColorRes colorId: Int) {
    try {
        window?.statusBarColor = getColorCompat(colorId)
    } catch (e: Exception) {
    }
}

fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    layoutId: Int,
    addToBackStack: Boolean = false,
    backStackName: String = "",
    tag: String? = null,
) {
    supportFragmentManager.beginTransaction().let {

        if (tag.isNotNullOrBlank())
            it.replace(layoutId, fragment, tag)
        else
            it.replace(layoutId, fragment)

        if (addToBackStack) {
            it.addToBackStack(backStackName)
        }
        it.commitAllowingStateLoss()
    }
}

fun AppCompatActivity.removeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
}

fun FragmentActivity.requireView(): View = window.decorView.rootView
