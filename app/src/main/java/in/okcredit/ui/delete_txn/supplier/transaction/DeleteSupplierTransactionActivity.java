package in.okcredit.ui.delete_txn.supplier.transaction;

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
import in.okcredit.analytics.PropertyValue;
import in.okcredit.backend.utils.CurrencyUtil;
import in.okcredit.databinding.DeleteTxnActivityBinding;
import in.okcredit.merchant.suppliercredit.Transaction;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import tech.okcredit.android.base.utils.DateTimeUtils;
import tech.okcredit.android.base.utils.KeyboardUtil;
import tech.okcredit.contract.AppLock;
import tech.okcredit.contract.AppLockTracker;
import tech.okcredit.contract.OnSetPinClickListener;
import tech.okcredit.contract.OnUpdatePinClickListener;

import static tech.okcredit.contract.AppLockTrackerKt.SET_PIN;
import static tech.okcredit.contract.Constants.IS_AUTHENTICATED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED;
import static tech.okcredit.contract.Constants.SECURITY_PIN_SET;

public class DeleteSupplierTransactionActivity extends BaseActivity
        implements DeleteSupplierTxnContract.View, OnSetPinClickListener, OnUpdatePinClickListener {

    public static final String EXTRA_TX_ID = "tx_id";
    public static final int DELETE_TRANSACTION_SUPPLIER = 1221;
    public static final int DELETE_TRANSACTION_SUPPLIER_SET_NEW_PIN = 12201;
    public static final int DELETE_TRANSACTION_SUPPLIER_UPDATE_PIN = 12202;
    @Inject
    DeleteSupplierTxnContract.Presenter viewModel;

    @Inject
    Lazy<AppLock> appLock;

    @Inject
    Lazy<AppLockTracker> appLockTracker;

    private String transactionId;
    private Transaction transaction;
    private DeleteTxnActivityBinding binding;
    private boolean isPageLoadTracked = false;

    public static Intent startingIntent(Context context, @NonNull String transactionId) {
        Intent intent = new Intent(context, DeleteSupplierTransactionActivity.class);
        intent.putExtra(EXTRA_TX_ID, transactionId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DeleteTxnActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showActionBar(true);

        binding.delete.setOnClickListener(
                view -> {
                    viewModel.checkPasswordSet();

                    Analytics.track(
                            AnalyticsEvents.DELETE_TRANSACTION_SCREEN_DELETE_CLICKED,
                            EventProperties.create()
                                    .with(PropertyKey.TYPE, getTransactionType(transaction))
                                    .with(
                                            PropertyKey.TXN_ID,
                                            transaction != null ? transaction.getId() : ""));
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

    /**
     * ************************************************************** MVP methods
     * **************************************************************
     */
    @Override
    public void showError() {

        Toast.makeText(this, R.string.err_default, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteLoading() {
        binding.delete.setVisibility(View.GONE);
        binding.deleteLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void showUpdatePinDialog() {
        appLock.get()
                .showUpdatePin(
                        getSupportFragmentManager(), this, DELETE_TRANSACTION_SUPPLIER_UPDATE_PIN, AnalyticsEvents.DELETE_TRANSACTION_SCREEN);
    }

    @Override
    public void hideDeleteLoading() {
        binding.delete.setVisibility(View.VISIBLE);
        binding.deleteLoader.setVisibility(View.GONE);
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
                            public void onCancel() {
                            }
                        });
    }

    @Override
    public void setTransaction(Transaction transaction) {

        transactionId = transaction.getId();
        this.transaction = transaction;
        trackOnPageLoad(transactionId, transaction.getSupplierId());
        if (transaction.getPayment()) {
            setTitle(R.string.payment_delete_desc);
            binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.tx_payment));
            binding.icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_payment));

            if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                binding.title.setText(R.string.txn_payment_title);
            } else {
                binding.title.setText(transaction.getNote());
            }

            binding.delMsg.setText(getString(R.string.del_payment_msg));
        } else {
            setTitle(R.string.credit_delete_desc);
            binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.tx_credit));
            binding.icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_credit));

            if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                binding.title.setText(R.string.txn_credit_title);
            } else {
                binding.title.setText(transaction.getNote());
            }

            binding.delMsg.setText(getString(R.string.del_credit_msg));
        }
        binding.subtitle.setText(DateTimeUtils.format(transaction.getCreateTime()));
        binding.amount.setText(CurrencyUtil.formatV2(transaction.getAmount()));
    }

    @Override
    public void goToCustomerScreen() {
        Toast.makeText(
                this,
                transaction.getPayment()
                        ? getString(R.string.payment_deleted)
                        : getString(R.string.credit_deleted),
                Toast.LENGTH_LONG)
                .show();
        Analytics.track(
                AnalyticsEvents.DELETE_TRANSACTION_SUCCESS,
                EventProperties.create()
                        .with(PropertyKey.TYPE, getTransactionType(transaction))
                        .with(PropertyKey.TXN_ID, transaction.getId()));
        finish();
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
    public void goToAuthScreen() {
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.enterpin_screen_deeplink),
                                this,
                                "Delete supplier Screen",
                                null),
                DELETE_TRANSACTION_SUPPLIER);
    }

    @Override
    public void goToSetNewPinScreen() {
        appLock.get()
                .showSetNewPin(
                        getSupportFragmentManager(), this, DELETE_TRANSACTION_SUPPLIER_SET_NEW_PIN, AnalyticsEvents.DELETE_TRANSACTION_SCREEN);
    }

    @Override
    public void gotoLogin() {
        super.gotoLogin();
    }

    private String getTransactionType(Transaction transaction) {
        if (transaction == null) {
            return "na";
        }
        if (transaction.getPayment()) {
            return "payment";
        } else {
            return "credit";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            boolean isAuthenticated = data.getBooleanExtra(IS_AUTHENTICATED, false);
            if (isAuthenticated) {
                if (requestCode == DELETE_TRANSACTION_SUPPLIER
                        || requestCode == DELETE_TRANSACTION_SUPPLIER_SET_NEW_PIN
                        || requestCode == DELETE_TRANSACTION_SUPPLIER_UPDATE_PIN) {
                    binding.delete.setVisibility(View.GONE);
                    onDelete();
                }
            }
            if (requestCode == DELETE_TRANSACTION_SUPPLIER_SET_NEW_PIN) {
                appLockTracker
                        .get()
                        .trackEvents(SECURITY_PIN_SET, AnalyticsEvents.DELETE_TRANSACTION_SCREEN, null);
            } else if (requestCode == DELETE_TRANSACTION_SUPPLIER_UPDATE_PIN) {
                appLockTracker
                        .get()
                        .trackEvents(SECURITY_PIN_CHANGED, AnalyticsEvents.DELETE_TRANSACTION_SCREEN, null);
            }
        }
    }

    private void onDelete() {
        KeyboardUtil.hideKeyboard(this);
        String id = "";
        if (transactionId != null) {
            id = transactionId;
        }
        Analytics.track(
                AnalyticsEvents.TX_DELETE_CONFIRM,
                EventProperties.create()
                        .with(PropertyKey.TYPE, getTransactionType(transaction))
                        .with(PropertyKey.TXN_ID, id));

        if (transactionId != null) {
            viewModel.delete(transactionId);
        }
    }

    @Override
    public void onSetPinClicked(int requestCode) {
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.changepin_screen_deeplink),
                                this,
                                "Delete supplier Screen",
                                SET_PIN),
                requestCode);
    }

    @Override
    public void onDismissed() {
    }

    @Override
    public void onUpdateDialogDismissed() {
    }

    @Override
    public void onSetNewPinClicked(int requestCode) {
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.changepin_screen_deeplink),
                                this,
                                "Delete supplier Screen",
                                SET_PIN),
                requestCode);
    }

    private void trackOnPageLoad(String transactionId, String accountId){
        if (isPageLoadTracked) {
            return;
        }
        Analytics.track(AnalyticsEvents.DELETE_TRANSACTION,
                EventProperties.create()
                        .with(PropertyKey.ACCOUNT_ID, accountId)
                        .with(PropertyKey.TXN_ID, transactionId)
                        .with(PropertyKey.RELATION, PropertyValue.SUPPLIER));
        isPageLoadTracked = true;
    }
}
