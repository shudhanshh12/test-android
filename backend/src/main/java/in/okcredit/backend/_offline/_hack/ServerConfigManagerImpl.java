package in.okcredit.backend._offline._hack;

import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.getIndividualScope;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import in.okcredit.backend.contract.ServerConfigManager;
import tech.okcredit.android.base.utils.LogUtils;
import tech.okcredit.android.base.workmanager.OkcWorkManager;

public final class ServerConfigManagerImpl implements ServerConfigManager {

    private Lazy<OkcWorkManager> workManager;

    @Inject
    public ServerConfigManagerImpl(Lazy<OkcWorkManager> workManager) {
        this.workManager = workManager;
    }

    @Override
    public void schedule() {
        Constraints constraints =
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(FetchVersionTask.class)
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                        .setConstraints(constraints)
                        .build();

        LogUtils.INSTANCE.enableWorkerLogging(workRequest);
        workManager
                .get()
                .schedule("latest-version", getIndividualScope(), ExistingWorkPolicy.KEEP, workRequest);
    }
}
