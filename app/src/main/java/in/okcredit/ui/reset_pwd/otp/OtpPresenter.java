package in.okcredit.ui.reset_pwd.otp;

import in.okcredit.onboarding.enterotp.usecase.RequestOtp;
import in.okcredit.shared._base_v2.BasePresenter;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.inject.Inject;
import tech.okcredit.android.auth.AuthService;
import tech.okcredit.android.auth.Credential;
import tech.okcredit.android.auth.ExpiredOtp;
import tech.okcredit.android.auth.InvalidOtp;
import tech.okcredit.android.auth.OtpToken;
import tech.okcredit.android.base.utils.ThreadUtils;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;

public class OtpPresenter extends BasePresenter<OtpContract.View> implements OtpContract.Presenter {

    private String mobile;

    private @Nullable OtpToken otpToken;

    private Disposable timerTask;

    private RequestOtp requestOtp;

    private AuthService authService;

    @Inject
    public OtpPresenter(
            @ViewModelParam("mobile") String mobile,
            RequestOtp requestOtp,
            AuthService authService) {
        super(AndroidSchedulers.mainThread());
        this.mobile = mobile;
        this.requestOtp = requestOtp;
        this.authService = authService;
    }

    @Override
    public void attachView(OtpContract.View v) {
        super.attachView(v);
        sendOtp();
        ifAttached(view -> view.setMobile(mobile));

        ifAttached(OtpContract.View::showAutoreadLoading);

        ifAttached(view -> view.setTimer(30));

        timerTask =
                Observable.interval(1, TimeUnit.SECONDS, ThreadUtils.INSTANCE.newThread())
                        .observeOn(uiScheduler)
                        .subscribe(
                                secondsElapsed -> {
                                    ifAttached(view -> view.setTimer(30 - secondsElapsed));

                                    if (secondsElapsed == 30) {
                                        ifAttached(OtpContract.View::showResendButton);
                                        timerTask.dispose();
                                    }
                                },
                                throwable -> {});

        addTask(timerTask);
    }

    @Override
    public void reSendOtp() {
        otpToken = null;
        sendOtp();

        ifAttached(OtpContract.View::showAutoreadLoading);
        ifAttached(view -> view.setTimer(30));

        if (!timerTask.isDisposed()) {
            timerTask.dispose();
        }

        timerTask =
                Observable.interval(1, TimeUnit.SECONDS, ThreadUtils.INSTANCE.newThread())
                        .observeOn(uiScheduler)
                        .subscribe(
                                secondsElapsed -> {
                                    ifAttached(view -> view.setTimer(30 - secondsElapsed));

                                    if (secondsElapsed == 30) {
                                        ifAttached(view -> view.showResendButton());
                                        timerTask.dispose();
                                    }
                                },
                                throwable -> {});

        addTask(timerTask);
    }

    @Override
    public void verifyOtp(String otp) {
        if (otpToken == null) {
            ifAttached(OtpContract.View::showResendButton);
            ifAttached(OtpContract.View::showExpiredOtpError);
        }

        ifAttached(OtpContract.View::showVerificationLoading);

        addTask(
                Single.fromCallable(
                                () -> authService.authenticate(new Credential.Otp(otpToken, otp)))
                        .subscribeOn(ThreadUtils.INSTANCE.api())
                        .observeOn(uiScheduler)
                        .subscribe(
                                token -> {
                                    ifAttached(OtpContract.View::showVerificationSuccess);
                                },
                                throwable -> {
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                    if (throwable instanceof InvalidOtp) {
                                        ifAttached(OtpContract.View::showResendButton);
                                        ifAttached(OtpContract.View::showIncorrectOtpError);
                                    } else if (throwable instanceof ExpiredOtp) {
                                        ifAttached(OtpContract.View::showResendButton);
                                        ifAttached(OtpContract.View::showExpiredOtpError);
                                    } else if (isInternetIssue(throwable)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void onInternetRestored() {
        sendOtp();
    }

    private void sendOtp() {
        ifAttached(OtpContract.View::hideResendButton);
        addTask(
                requestOtp
                        .execute(mobile)
                        .observeOn(uiScheduler)
                        .subscribe(
                                (otpToken) -> {
                                    this.otpToken = otpToken;
                                },
                                throwable -> {
                                    ifAttached(OtpContract.View::showResendButton);
                                    if (isInternetIssue(throwable)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }
}
