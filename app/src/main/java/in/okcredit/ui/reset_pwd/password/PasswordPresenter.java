package in.okcredit.ui.reset_pwd.password;

import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.EventProperties;
import in.okcredit.backend.analytics.AnalyticsSuperProps;
import in.okcredit.shared._base_v2.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import javax.inject.Inject;
import tech.okcredit.android.auth.usecases.ResetPassword;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import timber.log.Timber;

public class PasswordPresenter extends BasePresenter<PasswordContract.View>
        implements PasswordContract.Presenter {
    private final String requestedScreen;

    private final ResetPassword resetPassword;

    @Inject
    public PasswordPresenter(
            @ViewModelParam("requested_screen") String requestedScreen,
            ResetPassword resetPassword) {

        super(AndroidSchedulers.mainThread());
        this.requestedScreen = requestedScreen;
        this.resetPassword = resetPassword;
    }

    @Override
    public void resetPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            ifAttached(view -> view.showInvalidPasswordError());
            return;
        }

        ifAttached(view -> view.showLoading());
        addTask(
                resetPassword
                        .execute(newPassword)
                        .observeOn(uiScheduler)
                        .subscribe(
                                () -> {
                                    Analytics.track(
                                            AnalyticsEvents.RESET_PWD_SUCCESSFUL,
                                            EventProperties.create().with("error", "false"));
                                    Analytics.setUserProperty(
                                            AnalyticsSuperProps.EXP_PAYMENT_PASSWORD_ENABLED,
                                            "true");
                                    ifAttached(view -> view.hideLoading());
                                    ifAttached(view -> view.gotoHomeScreen(requestedScreen));
                                },
                                throwable -> {
                                    Timber.e(throwable, "failed to reset password");
                                    ifAttached(view -> view.hideLoading());
                                    if (isInternetIssue(throwable)) {
                                        Analytics.track(
                                                AnalyticsEvents.RESET_PWD_SUCCESSFUL,
                                                EventProperties.create()
                                                        .with("error", "internet issue"));
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        Analytics.track(
                                                AnalyticsEvents.RESET_PWD_SUCCESSFUL,
                                                EventProperties.create().with("error", "true"));
                                        ifAttached(view -> view.showError());
                                    }
                                }));
    }

    @Override
    public void onInternetRestored() {}

}
