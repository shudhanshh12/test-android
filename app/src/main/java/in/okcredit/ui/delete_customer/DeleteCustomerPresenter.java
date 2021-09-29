package in.okcredit.ui.delete_customer;


import static java.lang.Math.abs;

import dagger.Lazy;
import in.okcredit.di.UiThread;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Tracker;
import in.okcredit.backend._offline.error.CustomerErrors;
import in.okcredit.backend.contract.Customer;
import in.okcredit.backend._offline.usecase.DeleteCustomer;
import in.okcredit.backend._offline.usecase.GetCustomerImpl;
import in.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl;
import in.okcredit.backend.contract.GetMerchantPreference;
import in.okcredit.individual.contract.PreferenceKey;
import in.okcredit.shared._base_v2.BasePresenter;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.ui._base_v2.BaseContracts;
import io.reactivex.Scheduler;
import javax.inject.Inject;
import merchant.okcredit.accounting.model.Transaction;
import tech.okcredit.android.auth.usecases.IsPasswordSet;
import tech.okcredit.android.base.rxjava.SchedulerProvider;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import tech.okcredit.contract.MerchantPrefSyncStatus;
import timber.log.Timber;

public class DeleteCustomerPresenter extends BasePresenter<DeleteCustomerContract.View>
        implements DeleteCustomerContract.Presenter {
    private static final String BALANCE_NOT_ZERO_API_MESSAGE = "balance_not_zero";
    private final String customerId;
    private final GetCustomerImpl getCustomer;
    private final DeleteCustomer deleteCustomer;
    private final Tracker tracker;
    private final SyncTransactionsImpl syncTransactionsImpl;
    private final Lazy<IsPasswordSet> isPasswordSet;
    private final Lazy<SchedulerProvider> schedulerProvider;
    private Customer customer;
    private final Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus;
    private final Lazy<GetMerchantPreference> getMerchantPreference;

    @Inject
    public DeleteCustomerPresenter(
            @UiThread Scheduler uiScheduler,
            @ViewModelParam("customer_id") String customerId,
            GetCustomerImpl getCustomer,
            DeleteCustomer deleteCustomer,
            Tracker tracker,
            SyncTransactionsImpl syncTransactionsImpl,
            Lazy<IsPasswordSet> isPasswordSet,
            Lazy<SchedulerProvider> schedulerProvider,
            Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus,
            Lazy<GetMerchantPreference> getMerchantPreference) {

        super(uiScheduler);
        this.customerId = customerId;
        this.getCustomer = getCustomer;
        this.deleteCustomer = deleteCustomer;
        this.tracker = tracker;
        this.syncTransactionsImpl = syncTransactionsImpl;
        this.isPasswordSet = isPasswordSet;
        this.schedulerProvider = schedulerProvider;
        this.merchantPrefSyncStatus = merchantPrefSyncStatus;
        this.getMerchantPreference = getMerchantPreference;
    }

    @Override
    protected void loadData() {
        addTask(
                getCustomer
                        .execute(customerId)
                        .firstOrError()
                        .observeOn(uiScheduler)
                        .subscribe(
                                customer -> {
                                    this.customer = customer;
                                    ifAttached(view -> view.setCustomer(customer));
                                },
                                throwable -> {
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                    if (isAuthenticationIssue(throwable)) {
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(throwable)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void delete() {
        ifAttached(BaseContracts.Loading.View::showLoading);
        addTask(
                deleteCustomer
                        .execute(customerId, null)
                        .observeOn(uiScheduler)
                        .subscribe(
                                () -> {
                                    if (customer != null) {
                                        tracker.trackDeleteRelationship(
                                                PropertyValue.CUSTOMER,
                                                customer.getId(),
                                                getCustomerSyncStatus(customer)
                                                );
                                    }
                                    ifAttached(DeleteCustomerContract.View::gotoHomeScreen);
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                },
                                throwable -> {
                                    tracker.trackError(
                                            "DeleteCustomerActivity", "Delete_Customer", throwable);
                                    Timber.d("Delete Customer Error: %s", throwable.getMessage());
                                    Timber.e(throwable);
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                    if (isAuthenticationIssue(throwable)) {
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(throwable)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        if (throwable
                                                instanceof CustomerErrors.DeletePermissionDenied) {
                                            CustomerErrors.DeletePermissionDenied error =
                                                    (CustomerErrors.DeletePermissionDenied)
                                                            throwable;
                                            if (BALANCE_NOT_ZERO_API_MESSAGE.equals(
                                                    error.getErrorMessage())) {
                                                ifAttached(
                                                        DeleteCustomerContract.View
                                                                ::showMessageWithRetry);
                                                return;
                                            }
                                        }
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    @Override
    public void settle() {
        if (customer == null) {
            return;
        }

        if (customer.getBalanceV2() < 0) {
            ifAttached(
                    view ->
                            view.gotoAddTxnScreen(
                                    customerId, Transaction.PAYMENT, abs(customer.getBalanceV2())));
        } else if (customer.getBalanceV2() > 0) {
            ifAttached(
                    view ->
                            view.gotoAddTxnScreen(
                                    customerId, Transaction.CREDIT, customer.getBalanceV2()));
        }
    }

    @Override
    public void onRetryClicked() {
        ifAttached(BaseContracts.Loading.View::showLoading);
        addTask(
                syncTransactionsImpl
                        .execute("DeleteCustomerPresenter", null, false, null)
                        .andThen(deleteCustomer.execute(customerId, null))
                        .observeOn(uiScheduler)
                        .subscribe(
                                () -> {
                                    if (customer != null) {
                                        tracker.trackDeleteRelationship(
                                                PropertyValue.CUSTOMER,
                                                customer.getId(), null);
                                    }
                                    ifAttached(DeleteCustomerContract.View::gotoHomeScreen);
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                },
                                throwable -> {
                                    if (throwable
                                            instanceof CustomerErrors.DeletePermissionDenied) {
                                        CustomerErrors.DeletePermissionDenied error =
                                                (CustomerErrors.DeletePermissionDenied) throwable;
                                        if (BALANCE_NOT_ZERO_API_MESSAGE.equals(
                                                error.getErrorMessage())) {
                                            ifAttached(
                                                    DeleteCustomerContract.View::hideLoadingOnly);
                                            return;
                                        }
                                    }
                                    tracker.trackError(
                                            "DeleteCustomerActivity", "Delete_Customer", throwable);
                                    Timber.d("Delete Customer Error: %s", throwable.getMessage());
                                    Timber.e(throwable);
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                    if (isAuthenticationIssue(throwable)) {
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
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
        loadData();
    }

    @Override
    public void onAuthenticationRestored() {
        loadData();
    }

    @Override
    public void checkIsPasswordSet() {
        addTask(
                isPasswordSet
                        .get()
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().io())
                        .subscribe(
                                (it) -> {
                                    if (it) {
                                        checkIsSyncPrefDone();
                                    } else {
                                        ifAttached(
                                                view ->
                                                        view.goToResetPasswordScreen());
                                    }
                                },
                                e -> {
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    private void checkIsFourDigitPin() {
        addTask(
                getMerchantPreference
                        .get()
                        .execute(PreferenceKey.FOUR_DIGIT_PIN)
                        .firstOrError()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().io())
                        .subscribe(
                                (it) -> {
                                    if (Boolean.parseBoolean(it)) {
                                        ifAttached(view -> view.deleteCustomer());
                                    } else {
                                        ifAttached(
                                                DeleteCustomerContract.View::showUpdatePinDialog);
                                    }
                                },
                                e -> {
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    private void checkIsSyncPrefDone() {
        addTask(
                merchantPrefSyncStatus
                        .get()
                        .checkMerchantPrefSync()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().io())
                        .subscribe(
                                (it) -> {
                                    if (it) {
                                        checkIsFourDigitPin();
                                    } else {
                                        syncMerchantPref();
                                    }
                                },
                                e -> {
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    private void syncMerchantPref() {
        addTask(
                merchantPrefSyncStatus
                        .get()
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().io())
                        .subscribe(
                                this::checkIsFourDigitPin,
                                e -> {
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }

    public String getCustomerSyncStatus(Customer customer) {
        switch (customer.getCustomerSyncStatus()) {
            case 0: return "Clean";
            case 1: return "Dirty";
            case 3: return "Immutable";
            default: return "Unknown";
        }
    }


}
