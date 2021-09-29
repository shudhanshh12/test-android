package in.okcredit.ui.delete_txn.supplier.supplier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.AndroidInjection;
import in.okcredit.R;
import in.okcredit.analytics.Analytics;
import in.okcredit.analytics.AnalyticsEvents;
import in.okcredit.analytics.EventProperties;
import in.okcredit.analytics.PropertyKey;
import in.okcredit.backend.utils.CurrencyUtil;
import in.okcredit.databinding.DelcstActivityBinding;
import in.okcredit.frontend.ui.MainActivity;
import in.okcredit.frontend.ui.SupplierActivity;
import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.navigation.NavigationActivity;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import tech.okcredit.contract.AppLock;
import tech.okcredit.contract.AppLockTracker;

import static tech.okcredit.contract.Constants.IS_AUTHENTICATED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED;

public class DeleteSupplierActivity extends BaseActivity implements DeleteSupplierContract.View {
    public static final String EXTRA_SUPPLIER_ID = "supplier_id";
    public static final int DELETE_SUPPLIER = 1011;

    @Inject DeleteSupplierContract.Presenter presenter;

    @Inject
    Lazy<AppLockTracker> appLockTracker;

    @Inject
    Lazy<AppLock> appLock;

    private DelcstActivityBinding binding;
    private Boolean isFromAuth = false;

    private String supplierId = "";

    public static Intent startingIntent(Context context, @NonNull String supplierId) {
        Intent intent = new Intent(context, DeleteSupplierActivity.class);
        intent.putExtra(EXTRA_SUPPLIER_ID, supplierId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DelcstActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showActionBar(true);
        initViews();
        presenter.getSupplierAmount();
        Analytics.track(AnalyticsEvents.DELETE_SUPPLIER_SCREEN);
        initListeners();
    }

    private void initListeners() {
        binding.delete.setOnClickListener(v -> {
            Analytics.track(AnalyticsEvents.DELETE_SUPPLIER_SCREEN_CLICK_DELETE,
                    EventProperties.create().with(PropertyKey.ACCOUNT_ID, supplierId));
            goToEnterPinScreen();
        });
        binding.settlement.setOnClickListener(v -> onSettleClicked());
    }

    private void initViews() {
        setTitle(R.string.delete_supplier);
        binding.msg.setText(getString(R.string.delcst_supplier_msg_settlement));
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.detachView();
    }

    private void onDeleteClicked() {
        presenter.delete();
    }

    private void onSettleClicked() {
        Analytics.track(AnalyticsEvents.DELETE_SUPPLIER_SCREEN_CLICK_SETTLEMENT);
        presenter.settle();
    }

    @Override
    public void showError() {
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
                                presenter.onInternetRestored();
                            }

                            @Override
                            public void onCancel() {}
                        });
    }

    @Override
    public void setCustomer(Supplier supplier) {
        supplierId = supplier.getId();
        binding.name.setText(supplier.getName());
        CurrencyUtil.renderV2(supplier.getBalance(), binding.balance, 0);

        if (supplier.getBalance() == 0) {
            binding.balanceLabel.setText(R.string.balance);
            if (supplier.getMobile() == null) {
                binding.msg.setText(R.string.delcst_supplier_msg_no_mobile);
            } else {
                binding.msg.setText(R.string.delcst_supplier_msg);
            }
            if (!isFromAuth) {
                binding.delete.setVisibility(View.VISIBLE);
            }
            binding.settlement.setVisibility(View.GONE);
        } else {
            binding.msg.setText(R.string.delcst_supplier_msg_settlement);
            if (supplier.getBalance() < 0) {
                binding.balanceLabel.setText(R.string.balance);
                binding.settlement.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_payment));
                binding.settlement.setText(
                        getString(
                                R.string.delcst_settlement_label_payment,
                                CurrencyUtil.formatV2(supplier.getBalance())));
            } else if (supplier.getBalance() > 0) {
                binding.balanceLabel.setText(R.string.advance);
                binding.settlement.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_credit));
                binding.settlement.setText(
                        getString(
                                R.string.delcst_settlement_label_credit,
                                CurrencyUtil.formatV2(supplier.getBalance())));
            }
            binding.delete.setVisibility(View.GONE);
            binding.settlement.setVisibility(View.VISIBLE);
        }
    }

    public void goToEnterPinScreen() {
        startActivityForResult(
                appLock.get().appLock(
                        getString(R.string.enterpin_screen_deeplink),
                        this,
                        AnalyticsEvents.SUPPLIER_PROFILE_SCREEN,
                        null),
                DELETE_SUPPLIER);
    }

    @Override
    public void gotoAddTxnScreen(String supplierId, int type, long amount) {
        Intent intent = SupplierActivity.startingAddTxnSupplierIntent(this, supplierId);
        intent.putExtra(MainActivity.ARG_TX_TYPE, type);
        intent.putExtra(MainActivity.ARG_TX_AMOUNT, amount);
        startActivity(intent);
    }

    @Override
    public void gotoHomeScreen() {
        // HomeConstants.INSTANCE.setCurrentTab(HomeConstants.HomeTab.SUPPLIER_TAB);
        NavigationActivity.navigateToHomeScreen(this);
        finishAffinity();
    }

    @Override
    public void showIncorrectPasswordError() {
        Analytics.track(AnalyticsEvents.DELETE_SUPPLIER_INCORRECT_PASSWORD);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            boolean isAuthenticated = data.getBooleanExtra(IS_AUTHENTICATED, false);
            if (requestCode == DELETE_SUPPLIER && isAuthenticated) {
                isFromAuth = true;
                binding.delete.setVisibility(View.GONE);
                onDeleteClicked();
            }else {
                appLockTracker
                        .get()
                        .trackEvents(SECURITY_PIN_CHANGED, "Delete Supplier Screen", null);
            }
        }
    }
}
