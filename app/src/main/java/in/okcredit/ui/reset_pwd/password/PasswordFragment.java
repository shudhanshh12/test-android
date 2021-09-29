package in.okcredit.ui.reset_pwd.password;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import dagger.android.support.AndroidSupportInjection;
import in.okcredit.R;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.databinding.ResetpwdFragmentPasswordBinding;
import in.okcredit.navigation.NavigationActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import in.okcredit.ui.reset_pwd.ResetPwdActivity;
import javax.inject.Inject;
import tech.okcredit.android.base.utils.KeyboardUtil;

public class PasswordFragment extends Fragment implements PasswordContract.View {
    public static final String ARG_REQUESTED_SCREEN = "requested_screen";

    public static PasswordFragment newInstance(String requestedScreen) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUESTED_SCREEN, requestedScreen);
        PasswordFragment fragment = new PasswordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject PasswordContract.Presenter viewModel;

    private ResetpwdFragmentPasswordBinding binding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = ResetpwdFragmentPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            KeyboardUtil.showKeyboard(getContext(), binding.password);
        }
        viewModel.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.detachView();
        KeyboardUtil.hideKeyboard(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.ok.setOnClickListener(v -> onOkClicked());
        Analytics.track(AnalyticsEvents.RESET_PWD_PWD_SCREEN);
    }

    private void onOkClicked() {
        if (binding.password.getEditText() != null) {
            viewModel.resetPassword(binding.password.getEditText().getText().toString());
        }
    }

    /**
     * ************************************************************** MVP methods
     * **************************************************************
     */
    @Override
    public void showError() {
        Toast.makeText(getActivity(), R.string.err_default, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showInvalidPasswordError() {
        Analytics.track(AnalyticsEvents.RESET_PWD_INVALID_PASSWORD);
        binding.password.setError(getString(R.string.err_invalid_password));
        binding.password.setErrorEnabled(true);
    }

    @Override
    public void gotoHomeScreen(String requestedScreen) {
        Toast.makeText(getActivity(), getString(R.string.reset_pwd_success), Toast.LENGTH_SHORT)
                .show();
        if (ResetPwdActivity.REQUESTED_SCREEN_TX.equals(requestedScreen)) {
            getActivity().finish();
        } else {
            NavigationActivity.navigateToHomeScreen(getActivity());
            getActivity().finishAffinity();
        }
    }

    @Override
    public void showNoInternetMessage() {
        new NetworkErrorDialog()
                .show(
                        getActivity(),
                        new NetworkErrorDialog.Listener() {
                            @Override
                            public void onNetworkOk() {
                                viewModel.onInternetRestored();
                            }

                            @Override
                            public void onCancel() {
                                getActivity().onBackPressed();
                                KeyboardUtil.hideKeyboard(PasswordFragment.this);
                            }
                        });
    }

    @Override
    public void showLoading() {
        binding.loading.setVisibility(View.VISIBLE);
        binding.ok.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        binding.loading.setVisibility(View.GONE);
        binding.ok.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
