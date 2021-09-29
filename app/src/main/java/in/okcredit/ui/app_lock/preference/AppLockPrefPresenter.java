package in.okcredit.ui.app_lock.preference;

import in.okcredit.di.UiThread;
import in.okcredit.backend.contract.AppLockManager;
import in.okcredit.shared._base_v2.BasePresenter;
import io.reactivex.Scheduler;
import javax.inject.Inject;

public class AppLockPrefPresenter extends BasePresenter<AppLockPrefContract.View>
        implements AppLockPrefContract.Presenter {

    private AppLockManager appLockManager;

    @Inject
    public AppLockPrefPresenter(@UiThread Scheduler uiScheduler, AppLockManager appLockManager) {
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
}
