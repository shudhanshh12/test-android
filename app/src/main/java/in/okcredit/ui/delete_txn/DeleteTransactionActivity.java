package in.okcredit.ui.delete_txn;

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
import in.okcredit.analytics.Tracker;
import in.okcredit.backend.utils.CurrencyUtil;
import in.okcredit.databinding.DeleteTxnActivityBinding;
import in.okcredit.ui._base_v2.BaseActivity;
import in.okcredit.ui._dialog.NetworkErrorDialog;
import merchant.okcredit.accounting.model.Transaction;
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

public class DeleteTransactionActivity extends BaseActivity
        implements DeleteTxnContract.View, OnSetPinClickListener, OnUpdatePinClickListener {

    public static final String EXTRA_TX_ID = "tx_id";
    public static final int DELETE_TRANSACTION = 121;
    public static final int DELETE_TRANSACTION_SET_NEW_PIN = 12001;
    public static final int DELETE_TRANSACTION_UPDATE_PIN = 12002;

    @Inject DeleteTxnContract.Presenter viewModel;

    @Inject Tracker tracker;

    @Inject Lazy<AppLock> appLock;

    @Inject Lazy<AppLockTracker> appLockTracker;

    private String transactionId;
    private Transaction transaction;
    private boolean isPageLoadTracked = false;
    private DeleteTxnActivityBinding binding;

    public static Intent startingIntent(Context context, @NonNull String transactionId) {
        Intent intent = new Intent(context, DeleteTransactionActivity.class);
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

                    if (transaction != null) {
                        Analytics.track(
                                AnalyticsEvents.DELETE_TRANSACTION_SCREEN_DELETE_CLICKED,
                                EventProperties.create()
                                        .with(PropertyKey.TYPE, transaction.getType())
                                        .with(PropertyKey.TXN_ID, transaction.getId()));
                    }
                });

        binding.rootView.setTracker(performanceTracker);
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
        tracker.trackError("Delete Customer Transaction", "Delete Transaction", "server error", "");

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
                .showUpdatePin(getSupportFragmentManager(), this, DELETE_TRANSACTION_UPDATE_PIN,
                        AnalyticsEvents.DELETE_TRANSACTION_SCREEN );
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
                            public void onCancel() {}
                        });
    }

    @Override
    public void setTransaction(Transaction transaction) {

        transactionId = transaction.getId();
        this.transaction = transaction;
        trackOnPageLoad(transactionId, transaction.getCustomerId());
        switch (transaction.getType()) {
            case Transaction.CREDIT:
                setTitle(R.string.credit_delete_desc);
                binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.tx_credit));
                binding.icon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_credit));

                if (transaction.isOnboarding()) {
                    if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                        binding.title.setText(R.string.old_balance_title_credit);
                    } else {
                        binding.title.setText(transaction.getNote());
                    }
                } else {

                    if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                        binding.title.setText(R.string.txn_credit_title);
                    } else {
                        binding.title.setText(transaction.getNote());
                    }
                }

                binding.delMsg.setText(getString(R.string.del_credit_msg));

                break;

            case Transaction.PAYMENT:
                if (transaction.getTransactionCategory() == Transaction.DISCOUNT) {
                    setTitle(R.string.delete_discount);
                    binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.tx_discount));
                    binding.icon.setImageDrawable(
                            ContextCompat.getDrawable(this, R.drawable.ic_discount));
                    if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                        binding.title.setText(R.string.discount_offered);
                    } else {
                        binding.title.setText(transaction.getNote());
                    }

                    binding.delMsg.setText(getString(R.string.del_discount_msg));
                } else if (transaction.getTransactionCategory() == Transaction.DEAFULT_CATERGORY) {
                    setTitle(R.string.payment_delete_desc);
                    binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.tx_payment));
                    binding.icon.setImageDrawable(
                            ContextCompat.getDrawable(this, R.drawable.ic_payment));

                    if (transaction.isOnboarding()) {

                        if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                            binding.title.setText(R.string.old_balance_title_payment);
                        } else {
                            binding.title.setText(transaction.getNote());
                        }
                    } else {

                        if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                            binding.title.setText(R.string.txn_payment_title);
                        } else {
                            binding.title.setText(transaction.getNote());
                        }
                    }

                    binding.delMsg.setText(getString(R.string.del_payment_msg));
                }

                break;
        }

        binding.subtitle.setText(DateTimeUtils.format(transaction.getCreatedAt()));
        binding.amount.setText(CurrencyUtil.formatV2(transaction.getAmountV2()));
    }

    @Override
    public void goToCustomerScreen() {
        Toast.makeText(
                        this,
                        transaction.getType() == Transaction.CREDIT
                                ? getString(R.string.credit_deleted)
                                : getString(R.string.payment_deleted),
                        Toast.LENGTH_LONG)
                .show();
        Analytics.track(
                AnalyticsEvents.DELETE_TRANSACTION_SUCCESS,
                EventProperties.create()
                        .with(PropertyKey.TYPE, transaction.getType())
                        .with(PropertyKey.TXN_ID, transaction.getId()));

        setResult(RESULT_OK);
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
                                AnalyticsEvents.DELETE_TRANSACTION_SCREEN,
                                null),
                DELETE_TRANSACTION);
    }

    @Override
    public void goToSetNewPinScreen() {
        appLock.get()
                .showSetNewPin(getSupportFragmentManager(), this, DELETE_TRANSACTION_SET_NEW_PIN,
                        AnalyticsEvents.DELETE_TRANSACTION_SCREEN);
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
            if (isAuthenticated) {
                if (requestCode == DELETE_TRANSACTION
                        || requestCode == DELETE_TRANSACTION_SET_NEW_PIN
                        || requestCode == DELETE_TRANSACTION_UPDATE_PIN) {
                    binding.delete.setVisibility(View.GONE);
                    onDeleteClicked();
                }
            }else {
                if (requestCode == DELETE_TRANSACTION_SET_NEW_PIN) {
                    appLockTracker
                            .get()
                            .trackEvents(SECURITY_PIN_SET, AnalyticsEvents.DELETE_TRANSACTION_SCREEN, null);
                } else {
                    appLockTracker
                            .get()
                            .trackEvents(SECURITY_PIN_CHANGED, AnalyticsEvents.DELETE_TRANSACTION_SCREEN, null);
                }
            }
        }
    }

    public void onDeleteClicked() {
        KeyboardUtil.hideKeyboard(this);
        if (transaction != null) {
            Analytics.track(
                    AnalyticsEvents.TX_DELETE_CONFIRM,
                    EventProperties.create()
                            .with(PropertyKey.TYPE, transaction.getType())
                            .with(PropertyKey.TXN_ID, transaction.getId()));
            if (transaction.getTransactionCategory() == Transaction.DISCOUNT) {
                viewModel.deleteDiscount(transactionId);
            } else {
                viewModel.delete(transactionId);
            }
        }
    }

    @Override
    public void onSetPinClicked(int requestCode) {
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.changepin_screen_deeplink),
                                this,
                                AnalyticsEvents.DELETE_TRANSACTION_SCREEN,
                                SET_PIN),
                requestCode);
    }

    @Override
    public void onDismissed() {}

    @Override
    public void onSetNewPinClicked(int requestCode) {
        startActivityForResult(
                appLock.get()
                        .appLock(
                                getString(R.string.changepin_screen_deeplink),
                                this,
                                AnalyticsEvents.DELETE_TRANSACTION_SCREEN,
                                SET_PIN),
                requestCode);
    }

    @Override
    public void onUpdateDialogDismissed() {}

    private void trackOnPageLoad(String transactionId, String accountId){
        if (isPageLoadTracked) {
            return;
        }
        Analytics.track(AnalyticsEvents.DELETE_TRANSACTION,
                EventProperties.create()
        .with(PropertyKey.ACCOUNT_ID, accountId)
        .with(PropertyKey.TXN_ID, transactionId)
        .with(PropertyKey.RELATION, PropertyValue.CUSTOMER));
        isPageLoadTracked = true;
    }
}
