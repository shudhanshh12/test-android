package in.okcredit.ui.app_lock.prompt;

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
import dagger.Lazy;
import in.okcredit.R;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.backend.contract.AppLockManager;
import in.okcredit.databinding.ScreenAppLockPromptBinding;
import in.okcredit.frontend.utils.ResourceUtils;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui.app_lock.forgot.ForgotAppLockActivity;
import java.util.List;
import javax.inject.Inject;
import tech.okcredit.base.Traces;

public class AppLockPromptActivity extends BaseActivity {

    public static Intent startingIntent(Context context) {
        return new Intent(context, AppLockPromptActivity.class);
    }

    @Inject Lazy<AppLockManager> appLockManager;

    ScreenAppLockPromptBinding binding;

    @Override
    @AddTrace(name = Traces.OnCreate_AppLockPrompt)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusbarColor();
        Analytics.track(AnalyticsEvents.APP_LOCK_PROMPT_SCREEN);
        binding = ScreenAppLockPromptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.forgot.setOnClickListener(
                view -> {
                    appLockManager.get().setLocked(false);
                    startActivity(ForgotAppLockActivity.startingIntent(this));
                });

        binding.forgot.setVisibility(View.INVISIBLE);

        binding.patternLockView.setTactileFeedbackEnabled(false);
        binding.patternLockView.addPatternLockListener(
                new PatternLockViewListener() {
                    @Override
                    public void onStarted() {}

                    @Override
                    public void onProgress(List<PatternLockView.Dot> progressPattern) {}

                    @Override
                    public void onComplete(List<PatternLockView.Dot> pattern) {

                        String patternString =
                                PatternLockUtils.patternToString(binding.patternLockView, pattern);

                        if (appLockManager.get().authenticatePattern(patternString)) {
                            binding.patternLockView.setViewMode(
                                    PatternLockView.PatternViewMode.CORRECT);
                            binding.subtitle.setText(getString(R.string.pattern_verified));
                            closeScreen();
                        } else {
                            binding.patternLockView.setViewMode(
                                    PatternLockView.PatternViewMode.WRONG);
                            Toast.makeText(
                                            AppLockPromptActivity.this,
                                            getString(R.string.incorrect_pattern),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            binding.patternLockView.clearPattern();
                            binding.forgot.setVisibility(View.VISIBLE);
                            binding.subtitle.setText(getString(R.string.incorrect_pattern));
                        }
                    }

                    @Override
                    public void onCleared() {}
                });
    }

    private void closeScreen() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void setStatusbarColor() {
        getWindow()
                .setStatusBarColor(
                        ResourceUtils.INSTANCE.getColorFromAttr(this, R.attr.colorPrimary));
    }
}
