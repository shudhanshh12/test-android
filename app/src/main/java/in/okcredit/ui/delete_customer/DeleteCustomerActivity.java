package in.okcredit.ui.delete_customer;

import static tech.okcredit.contract.AppLockTrackerKt.SET_PIN;
import static tech.okcredit.contract.Constants.IS_AUTHENTICATED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_SET;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import dagger.Lazy;
import dagger.android.AndroidInjection;
import in.okcredit.R;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.EventProperties;
import in.okcredit.analytics.PropertyKey;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Screen;
import in.okcredit.analytics.Tracker;
import in.okcredit.backend.contract.Customer;
import in.okcredit.backend.utils.CurrencyUtil;
import in.okcredit.databinding.DelcstActivityBinding;
import in.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity;
import in.okcredit.navigation.NavigationActivity;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import javax.inject.Inject;
import merchant.okcredit.accounting.model.Transaction;
import tech.okcredit.contract.AppLock;
import tech.okcredit.contract.AppLockTracker;
import tech.okcredit.contract.OnSetPinClickListener;
import tech.okcredit.contract.OnUpdatePinClickListener;

public class DeleteCustomerActivity extends BaseActivity
        implements DeleteCustomerContract.View,
                UnableToDeleteCustomerBottomSheetListener,
                OnSetPinClickListener,
                OnUpdatePinClickListener {
    public static final String EXTRA_CUSTOMER_ID = "customer_id";
    public static final int DELETE_CUSTOMER = 131;

    @Inject Lazy<AppLock> appLock;

    @Inject Lazy<AppLockTracker> appLockTracker;

    @Inject DeleteCustomerContract.Presenter viewModel;
    @Inject Tracker tracker;

    private boolean isSetNewPin;
    private String customerId = "";
    private DelcstActivityBinding binding;
    private Boolean isFromAuth = false;

    public static Intent startingIntent(Context context, @NonNull String customerId) {
        Intent intent = new Intent(context, DeleteCustomerActivity.class);
        intent.putExtra(EXTRA_CUSTOMER_ID, customerId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DelcstActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showActionBar(true);
        setTitle(R.string.delete_cus);

        Analytics.track(AnalyticsEvents.DELETE_CUSTOMER_SCREEN);
        initListeners();
        binding.llRoot.setTracker(performanceTracker);
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

    private void initListeners() {
        binding.delete.setOnClickListener(v -> {
            trackDeleteClicked();
            checkForPasswordUpdate();
        });
        binding.settlement.setOnClickListener(v -> onSettleClicked());
    }

    private void trackDeleteClicked() {
        Analytics.track(
                AnalyticsEvents.DELETE_CUSTOMER_SCREEN_CLICK_DELETE,
                EventProperties.create().with(PropertyKey.ACCOUNT_ID, customerId));
    }

    private void checkForPasswordUpdate() {
        binding.delete.setVisibility(View.GONE);
        viewModel.checkIsPasswordSet();
    }

    @Override
    public void goToResetPasswordScreen() {
        binding.delete.setVisibility(View.VISIBLE);
        appLock.get().showSetNewPin(getSupportFragmentManager(),this, DELETE_CUSTOMER, AnalyticsEvents.CUSTOMER_PROFILE_SCREEN);
    }

    @Override
    public void deleteCustomer() {
        goToPasswordScreen();
    }

    @Override
    public void showUpdatePinDialog() {
        binding.delete.setVisibility(View.VISIBLE);
        appLock.get().showUpdatePin(getSupportFragmentManager(), this, DELETE_CUSTOMER, AnalyticsEvents.CUSTOMER_PROFILE_SCREEN);
    }

    private void goToPasswordScreen() {
        binding.delete.setVisibility(View.GONE);
        startActivityForResult(
                getAppLockIntent(
                        getString(R.string.enterpin_screen_deeplink)
                ),
                DELETE_CUSTOMER);
    }

    private Intent getAppLockIntent(String deepLink){
        return appLock.get().appLock(
                deepLink,
                this,
                AnalyticsEvents.CUSTOMER_PROFILE_SCREEN,
                null
        );
    }

    private Intent getAppLockIntent(String deepLink, String entry){
        return appLock.get().appLock(
                deepLink,
                this,
                AnalyticsEvents.CUSTOMER_PROFILE_SCREEN,
                entry
        );
    }

    public void onDeleteClicked() {
        viewModel.delete();
    }

    public void onSettleClicked() {
        Analytics.track(
                AnalyticsEvents.DELETE_CUSTOMER_SCREEN_CLICK_SETTLEMENT,
                EventProperties.create().with(PropertyKey.ACCOUNT_ID, customerId));
        viewModel.settle();
    }

    /**
     * ************************************************************** MVP methods
     * **************************************************************
     */
    @Override
    public void showError() {
        tracker.trackError("Delete Customer", "Delete Customer", "server error", "");
        Toast.makeText(this, R.string.err_default, Toast.LENGTH_SHORT).show();
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

    @Override
    public void setCustomer(Customer customer) {
        customerId = customer.getId();
        binding.name.setText(customer.getDescription());
        CurrencyUtil.renderV2(customer.getBalanceV2(), binding.balance, 0);

        if (customer.getBalanceV2() == 0) {
            binding.balanceLabel.setText(R.string.balance);
            if (customer.getMobile() == null) {
                binding.msg.setText(R.string.delcst_msg_no_mobile);
            } else {
                binding.msg.setText(R.string.delcst_msg);
            }
            if (!isFromAuth) binding.delete.setVisibility(View.VISIBLE);
            binding.settlement.setVisibility(View.GONE);
        } else {
            binding.msg.setText(R.string.delcst_msg_settlement);
            if (customer.getBalanceV2() < 0) {
                binding.balanceLabel.setText(R.string.balance);
                binding.settlement.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_payment));
                binding.settlement.setText(
                        getString(
                                R.string.delcst_settlement_label_payment,
                                CurrencyUtil.formatV2(customer.getBalanceV2())));
            } else if (customer.getBalanceV2() > 0) {
                binding.balanceLabel.setText(R.string.advance);
                binding.settlement.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_credit));
                binding.settlement.setText(
                        getString(
                                R.string.delcst_settlement_label_credit,
                                CurrencyUtil.formatV2(customer.getBalanceV2())));
            }
            binding.delete.setVisibility(View.GONE);
            binding.settlement.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void gotoAddTxnScreen(String customerId, int type, long amount) {
        String txnTypeFormatted;
        if (type == Transaction.CREDIT) {
            txnTypeFormatted = PropertyKey.CREDIT;
        } else {
            txnTypeFormatted = PropertyKey.PAYMENT;
        }
        tracker.trackAddTransactionFlowsStarted(
                txnTypeFormatted, PropertyValue.CUSTOMER, customerId, Screen.DELETE_CUSTOMER);
        startActivity(
                AddTxnContainerActivity.getIntent(
                        this,
                        customerId,
                        type,
                        AddTxnContainerActivity.ADD_TRANSACTION_WITH_AMOUNT,
                        amount,
                        AddTxnContainerActivity.Source.DELETE_CUSTOMER_SCREEN));
    }

    @Override
    public void gotoHomeScreen() {
        NavigationActivity.navigateToHomeScreen(this);
        finishAffinity();
    }

    @Override
    public void showMessageWithRetry() {
        new UnableToDeleteCustomerBottomSheet(this)
                .show(getSupportFragmentManager(), UnableToDeleteCustomerBottomSheet.TAG);
    }

    @Override
    public void hideLoadingOnly() {
        binding.loading.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        binding.loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.loading.setVisibility(View.GONE);
    }

    @Override
    public void gotoLogin() {
        super.gotoLogin();
    }

    @Override
    public void cancel() {
        onBackPressed();
    }

    @Override
    public void retry() {
        viewModel.onRetryClicked();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            boolean isAuthenticated = data.getBooleanExtra(IS_AUTHENTICATED, false);
            if (requestCode == DELETE_CUSTOMER) {
                if (isAuthenticated) {
                    isFromAuth = true;
                    binding.delete.setVisibility(View.GONE);
                    onDeleteClicked();
                } else {
                    binding.delete.setVisibility(View.VISIBLE);
                    if (isSetNewPin) {
                        appLockTracker
                                .get()
                                .trackEvents(SECURITY_PIN_SET, "Customer Profile Screen", null);
                    } else {
                        appLockTracker
                                .get()
                                .trackEvents(SECURITY_PIN_CHANGED, "Customer Profile Screen", null);
                    }
                }


            } else binding.delete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSetPinClicked(int requestCode) {
        isSetNewPin = true;
        startActivityForResult(
                getAppLockIntent(getString(R.string.changepin_screen_deeplink), SET_PIN),
                DELETE_CUSTOMER);
    }

    @Override
    public void onDismissed() {
        binding.delete.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSetNewPinClicked(int requestCode) {
        isSetNewPin = false;
        startActivityForResult(
                getAppLockIntent(getString(R.string.changepin_screen_deeplink), SET_PIN),
                DELETE_CUSTOMER);
    }

    @Override
    public void onUpdateDialogDismissed() {}
}
