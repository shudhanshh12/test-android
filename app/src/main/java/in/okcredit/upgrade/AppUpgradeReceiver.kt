package `in`.okcredit.upgrade

import `in`.okcredit.analytics.AppAnalytics
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.workmanager.OkcWorkManager
import javax.inject.Inject

class AppUpgradeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workManager: OkcWorkManager

    @Inject
    lateinit var appAnalytics: AppAnalytics

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        AppUpgradeWorker.schedule(workManager)
        cancelRemovedWorkers()
        appAnalytics.trackAppUpdate()
    }

    private fun cancelRemovedWorkers() {
        listOf("sync_everything_immediate", "home_screen_refresh", "sync_everything", "remote-apk-download")
            .forEach { uniqueWorkName -> workManager.cancelUniqueWork(uniqueWorkName, Scope.Individual) }
    }
}
