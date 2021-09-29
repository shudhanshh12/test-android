package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.Tracker
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Lazy
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.rxjava.SchedulerProvider
import java.lang.Exception
import javax.inject.Inject

class AppShortcutHelper @Inject constructor(
    private val context: Lazy<Context>,
    private val ab: Lazy<AbRepository>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val tracker: Lazy<Tracker>
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun isShortcutAlreadyPinned(id: String): Boolean {
        val shortcutManager = context.get().getSystemService(ShortcutManager::class.java)
        if (shortcutManager.isRequestPinShortcutSupported) {
            return shortcutManager.pinnedShortcuts.map { it.id }.contains(id)
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestPinShortcut(id: String) {
        try {
            val shortcutManager = context.get().getSystemService(ShortcutManager::class.java)
            val shortcutInfo = ShortcutInfo.Builder(context.get(), id).build()
            if (shortcutManager.isRequestPinShortcutSupported) {
                val intent = shortcutManager.createShortcutResultIntent(shortcutInfo)
                val successCallback = PendingIntent.getBroadcast(context.get(), 0, intent, 0)
                shortcutManager.requestPinShortcut(shortcutInfo, successCallback.intentSender)
            } else {
                tracker.get().trackDebug("add_transaction_shortcut: Pin shortcut not supported")
            }
        } catch (e: Exception) {
            tracker.get().trackDebug(
                "add_transaction_shortcut: exception occurred (shortcut not added?)",
                mapOf(PropertyKey.REASON to e.localizedMessage)
            )
        }
    }

    fun addShortcutIfFeatureIsEnabled(shortcutInfo: ShortcutInfo, featureName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ab.get().isFeatureEnabled(featureName).firstOrError()
                .map { enabled ->
                    if (enabled) {
                        addDynamicShortcutsIfNotAdded(listOf(shortcutInfo))
                    }
                }.subscribeOn(schedulerProvider.get().io()).subscribe()
        }
    }

    fun addDynamicShortcutsIfNotAdded(shortcutList: List<ShortcutInfo>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.get().getSystemService(ShortcutManager::class.java)
            shortcutList.forEach { shortcut ->
                if (shortcutManager.dynamicShortcuts.map { it.id }.contains(shortcut.id).not()) {
                    shortcutManager.addDynamicShortcuts(listOf(shortcut))
                }
            }
        }
    }
}
