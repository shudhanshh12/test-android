package tech.okcredit.applock

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import tech.okcredit.applock.AppLockActivityV2.Companion.ENTRY
import tech.okcredit.applock.AppLockActivityV2.Companion.SCREEN
import tech.okcredit.applock.changePin.ChangeSecurityPinFragment.Companion.Source
import tech.okcredit.applock.dialogs.SetNewPin
import tech.okcredit.applock.dialogs.UpdatePin
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.OnSetPinClickListener
import tech.okcredit.contract.OnUpdatePinClickListener
import tech.okcredit.contract.SET_PIN
import javax.inject.Inject

class AppLockImpl @Inject constructor() : AppLock {
    override fun appLock(deeplink: String, context: Context, sourceScreen: String, entry: String?): Intent {
        val intent = Intent(context, AppLockActivityV2::class.java)
        intent.putExtra(SCREEN, deeplink)
        intent.putExtra(Source, sourceScreen)
        intent.putExtra(ENTRY, entry ?: SET_PIN)
        return intent
    }

    override fun showSetNewPin(fragmentManager: FragmentManager, listener: OnSetPinClickListener, requestCode: Int, sourceScreen: String) {
        val bottomSheet = SetNewPin.newInstance(listener, requestCode, sourceScreen)
        bottomSheet.show(fragmentManager, bottomSheet.tag)
    }

    override fun showUpdatePin(fragmentManager: FragmentManager, listener: OnUpdatePinClickListener, requestCode: Int, sourceScreen: String) {
        val bottomSheet = UpdatePin.newInstance(listener, requestCode, sourceScreen)
        bottomSheet.show(fragmentManager, bottomSheet.tag)
    }
}
