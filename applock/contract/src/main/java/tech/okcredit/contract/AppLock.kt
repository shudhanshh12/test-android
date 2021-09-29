package tech.okcredit.contract

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager

interface AppLock {
    fun appLock(deeplink: String, context: Context, sourceScreen: String, entry: String? = null): Intent

    fun showSetNewPin(
        fragmentManager: FragmentManager,
        listener: OnSetPinClickListener,
        requestCode: Int = 0,
        sourceScreen: String = "",
    )

    fun showUpdatePin(
        fragmentManager: FragmentManager,
        listener: OnUpdatePinClickListener,
        requestCode: Int = 0,
        sourceScreen: String = "",
    )
}
