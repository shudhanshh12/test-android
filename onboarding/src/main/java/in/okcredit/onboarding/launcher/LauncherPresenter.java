package in.okcredit.onboarding.launcher;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import in.okcredit.backend.contract.RxSharedPrefValues;
import in.okcredit.frontend.contract.CheckAppLockAuthentication;
import in.okcredit.frontend.contract.data.AppResume;
import in.okcredit.shared._base_v2.BasePresenter;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import tech.okcredit.android.base.preferences.DefaultPreferences;
import tech.okcredit.android.base.rxjava.SchedulerProvider;
import tech.okcredit.android.base.utils.LogUtils;

import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.blockingGetLong;
import static tech.okcredit.android.base.preferences.OkcSharedPreferencesBackwardCompatibilityExtensionsKt.getIndividualScope;

public class LauncherPresenter extends BasePresenter<LauncherContract.View>
        implements LauncherContract.Presenter {

    private final Lazy<Context> context;
    private final Lazy<DefaultPreferences> rxSharedPreference;
    private final Lazy<SchedulerProvider> scheduler;
    private final Lazy<CheckAppLockAuthentication> checkAppLockAuthentication;

    @Inject
    public LauncherPresenter(
            Lazy<Context> context,
            Lazy<DefaultPreferences> rxSharedPreference,
            Lazy<SchedulerProvider> scheduler,
            Lazy<CheckAppLockAuthentication> checkAppLockAuthentication
    ) {

        super(AndroidSchedulers.mainThread());
        this.context = context;
        this.rxSharedPreference = rxSharedPreference;
        this.scheduler = scheduler;
        this.checkAppLockAuthentication = checkAppLockAuthentication;
    }

    @Override
    public void attachView(LauncherContract.View view) {
        super.attachView(view);
        addTask(
                checkAppLockAuthentication
                        .get()
                        .execute()
                        .delay(2, TimeUnit.SECONDS) // explicit delay added for animation
                        .subscribeOn(scheduler.get().io())
                        .observeOn(scheduler.get().ui())
                        .subscribe(
                                type -> {
                                    if (type == AppResume.NOT_AUTHENTICAED) {
                                        ifAttached(LauncherContract.View::goToLanguageSelectionScreen);
                                    } else if (type == AppResume.APP_LOCK_SETUP) {
                                        ifAttached(LauncherContract.View::setupAppLock);
                                    } else if (type == AppResume.NEW_APP_LOCK_RESUME) {
                                        ifAttached(
                                                LauncherContract.View::authenticateViaNewAppLock);
                                    } else if (type == AppResume.OLD_APP_LOCK_RESUME) {
                                        ifAttached(
                                                LauncherContract.View::authenticateViaOldAppLOck);
                                    } else if (type == AppResume.BUSINESS_NAME) {
                                        ifAttached(LauncherContract.View::goToEnterBusinessName);
                                    } else if (type == AppResume.NONE) {
                                        ifAttached(LauncherContract.View::gotoHome);
                                    }
                                }));

        addTask(Single.fromCallable(() -> blockingGetLong(
                rxSharedPreference.get(),
                RxSharedPrefValues.LOGGING_END_TIME,
                getIndividualScope(),
                0L
        ))
                .subscribeOn(scheduler.get().io())
                .observeOn(uiScheduler)
                .subscribe(
                        time -> {
                            if (time > System.currentTimeMillis()) {
                                LogUtils.INSTANCE.startRemoteLogging(
                                        context.get(), "", "", "");
                            }
                        },
                        throwable -> {
                        }));
    }
}
