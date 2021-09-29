package in.okcredit.ui.app_lock.set;

import in.okcredit.di.UiThread;
import in.okcredit.backend.contract.AppLockManager;
import in.okcredit.shared._base_v2.BasePresenter;
import io.reactivex.Scheduler;
import javax.inject.Inject;

public class AppLockPresenter extends BasePresenter<AppLockContract.View>
        implements AppLockContract.Presenter {

    private AppLockManager appLockManager;

    @Inject
    public AppLockPresenter(@UiThread Scheduler uiScheduler, AppLockManager appLockManager) {
        super(uiScheduler);
        this.appLockManager = appLockManager;
    }

    @Override
    protected void loadData() {
        ifAttached(view -> view.setAppLockStatus(appLockManager.isAppLockActive()));
    }

    @Override
    public void onInternetRestored() {
        loadData();
    }

    @Override
    public void onAuthenticationRestored() {
        loadData();
    }

    @Override
    public void enableAppLock(String pattern) {

        appLockManager.enableAppLock(pattern);
        ifAttached(view -> view.goToAppLockPrefActivity(true));
    }

    @Override
    public void disableAppLock(String oldPatternAttempt) {

        if (appLockManager.disableAppLock(oldPatternAttempt)) {
            ifAttached(view -> view.goToAppLockPrefActivity(false));
        } else {
            ifAttached(view -> view.showIncorrectOldPatternMessage());
        }
    }
}
