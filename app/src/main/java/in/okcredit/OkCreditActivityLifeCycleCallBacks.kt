package `in`.okcredit

import `in`.okcredit.frontend.usecase.language_experiment.GetExperimentStrings
import android.app.Activity
import android.app.Application
import android.app.LauncherActivity
import android.os.Bundle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import tech.okcredit.account_chat_sdk.ChatCore
import tech.okcredit.android.base.rxjava.SchedulerProvider
import javax.inject.Inject

class OkCreditActivityLifeCycleCallBacks @Inject constructor(
    private val getExperimentStrings: Lazy<GetExperimentStrings>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val chatCore: ChatCore,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        chatCore.onActivityDestroyed()
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        val isStringExperimentEnabled = firebaseRemoteConfig.get().getBoolean(STRING_EXPERIMENT_FLAG_KEY)
        if (activity !is LauncherActivity && isStringExperimentEnabled) {
            getExperimentStrings.get().execute().subscribeOn(schedulerProvider.get().io()).subscribe()
        }
        chatCore.onActivityCreated()
    }

    companion object {
        const val STRING_EXPERIMENT_FLAG_KEY = "string_experiment_flag"
    }
}
