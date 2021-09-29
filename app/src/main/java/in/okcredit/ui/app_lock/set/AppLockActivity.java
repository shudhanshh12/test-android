package in.okcredit.ui.app_lock.set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.google.firebase.perf.metrics.AddTrace;
import in.okcredit.R;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.EventProperties;
import in.okcredit.databinding.ScreenAppLockBinding;
import in.okcredit.frontend.utils.ResourceUtils;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import tech.okcredit.android.base.utils.ThreadUtils;
import tech.okcredit.base.Traces;

public class AppLockActivity extends BaseActivity implements AppLockContract.View {

    public static Intent startingIntent(Context context) {
        return new Intent(context, AppLockActivity.class);
    }

    @Inject AppLockContract.Presenter viewModel;

    String firstPattern;
    private String secondPattern;
    boolean isFirstPatternSet;

    private boolean isAppLockActive;

    ScreenAppLockBinding binding;

    @Override
    @AddTrace(name = Traces.OnCreate_AppLock)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScreenAppLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusbarColor();

        firstPattern = "";
        secondPattern = "";
        isFirstPatternSet = false;

        binding.action.setVisibility(View.INVISIBLE);

        binding.patternLockView.setTactileFeedbackEnabled(false);
        binding.patternLockView.addPatternLockListener(
                new PatternLockViewListener() {
                    @Override
                    public void onStarted() {
                        binding.action.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onProgress(List<PatternLockView.Dot> progressPattern) {}

                    @Override
                    public void onComplete(List<PatternLockView.Dot> pattern) {

                        if (!isAppLockActive) {
                            binding.action.setVisibility(View.VISIBLE);
                            if (!isFirstPatternSet) {
                                firstPattern =
                                        PatternLockUtils.patternToString(
                                                binding.patternLockView, pattern);
                            } else {
                                secondPattern =
                                        PatternLockUtils.patternToString(
                                                binding.patternLockView, pattern);
                            }
                        } else {
                            viewModel.disableAppLock(
                                    PatternLockUtils.patternToString(
                                            binding.patternLockView, pattern));
                        }
                    }

                    @Override
                    public void onCleared() {}
                });

        binding.action.setOnClickListener(
                view -> {
                    if (!isFirstPatternSet) {
                        if (firstPattern == null || firstPattern.length() < 4) {
                            isFirstPatternSet = false;
                            binding.patternLockView.setViewMode(
                                    PatternLockView.PatternViewMode.WRONG);
                            binding.action.setVisibility(View.INVISIBLE);
                            binding.subtitle.setVisibility(View.VISIBLE);
                            binding.subtitle.setText(R.string.invalid_pattern);
                        } else {
                            isFirstPatternSet = true;
                            binding.patternLockView.clearPattern();
                            binding.action.setVisibility(View.INVISIBLE);
                            binding.subtitle.setText(getString(R.string.draw_patern_again));
                            binding.action.setText(getString(R.string.confirm_pattern));
                            binding.subtitle.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (secondPattern == null) {
                            showPatternMismatchMessage();
                            binding.patternLockView.setViewMode(
                                    PatternLockView.PatternViewMode.WRONG);
                            binding.action.setVisibility(View.INVISIBLE);
                            binding.subtitle.setVisibility(View.VISIBLE);
                            binding.subtitle.setText(R.string.pattern_mismatch);
                        } else {
                            if (secondPattern.equals(firstPattern)) {
                                binding.patternLockView.setViewMode(
                                        PatternLockView.PatternViewMode.CORRECT);
                                viewModel.enableAppLock(firstPattern);
                            } else {
                                showPatternMismatchMessage();
                                binding.patternLockView.setViewMode(
                                        PatternLockView.PatternViewMode.WRONG);
                                binding.action.setVisibility(View.INVISIBLE);
                                binding.subtitle.setVisibility(View.VISIBLE);
                                binding.subtitle.setText(R.string.pattern_mismatch);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.detachView();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void setAppLockStatus(boolean isAppLockActive) {

        this.isAppLockActive = isAppLockActive;

        Analytics.track(
                AnalyticsEvents.APP_LOCK_SCREEN,
                EventProperties.create().with("isAppLockActive", isAppLockActive));

        if (isAppLockActive) {
            binding.title.setText(getString(R.string.disable_app_lock));
            binding.subtitle.setText(R.string.draw_unlock_pattern);
            binding.action.setVisibility(View.INVISIBLE);
        } else {
            binding.title.setText(getString(R.string.enable_app_lock));
            binding.subtitle.setText(R.string.draw_unlock_pattern);
            binding.action.setText(getString(R.string.set_pattern));
        }
    }

    @Override
    public void goToAppLockPrefActivity(boolean isAppLockEnabled) {

        Analytics.track(
                AnalyticsEvents.APP_LOCK_SCREEN_SUCCESS,
                EventProperties.create().with("enabled", isAppLockEnabled));

        if (isAppLockEnabled) {
            Toast.makeText(this, getString(R.string.app_lock_enabled), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.app_lock_disabled), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void showIncorrectOldPatternMessage() {

        Analytics.track(
                AnalyticsEvents.APP_LOCK_WRONG_PATTERN,
                EventProperties.create().with("state", isAppLockActive));

        binding.patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
        binding.action.setVisibility(View.INVISIBLE);
        binding.subtitle.setVisibility(View.VISIBLE);
        binding.subtitle.setText(R.string.incorrect_pattern);

        Observable.timer(1, TimeUnit.SECONDS, ThreadUtils.INSTANCE.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new DisposableObserver<Long>() {
                            @Override
                            public void onNext(@NotNull Long time) {
                                binding.patternLockView.clearPattern();
                            }

                            @Override
                            public void onError(@NotNull Throwable e) {}

                            @Override
                            public void onComplete() {}
                        });
    }

    @Override
    public void showPatternMismatchMessage() {
        Analytics.track(
                AnalyticsEvents.APP_LOCK_WRONG_PATTERN,
                EventProperties.create().with("state", isAppLockActive));
    }

    @Override
    public void showError() {
        Toast.makeText(this, R.string.err_default, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void showNoInternetMessage() {
        new NetworkErrorDialog()
                .show(
                        this,
                        new NetworkErrorDialog.Listener() {
                            @Override
                            public void onNetworkOk() {
                                viewModel.onInternetRestored();
                            }

                            @Override
                            public void onCancel() {}
                        });
    }

    private void setStatusbarColor() {
        getWindow()
                .setStatusBarColor(
                        ResourceUtils.INSTANCE.getColorFromAttr(this, R.attr.colorPrimary));
    }
}
