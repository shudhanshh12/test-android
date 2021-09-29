package in.okcredit.ui.delete_txn.supplier.transaction;

import javax.inject.Inject;

import dagger.Lazy;
import in.okcredit.di.UiThread;
import in.okcredit.backend.contract.GetMerchantPreference;
import in.okcredit.fileupload.utils.SchedulerProvider;
import in.okcredit.frontend.usecase.supplier.DeleteSupplierTransaction;
import in.okcredit.frontend.usecase.supplier.GetSupplierTransaction;
import in.okcredit.individual.contract.PreferenceKey;
import in.okcredit.shared._base_v2.BasePresenter;
import io.reactivex.Scheduler;
import tech.okcredit.android.auth.usecases.IsPasswordSet;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import tech.okcredit.contract.MerchantPrefSyncStatus;

public class DeleteSupplierTxnPresenter extends BasePresenter<DeleteSupplierTxnContract.View>
        implements DeleteSupplierTxnContract.Presenter {

    private final String transactionId;
    private final DeleteSupplierTransaction deleteSupplierTransaction;
    private final GetSupplierTransaction getSupplierTransaction;
    private final IsPasswordSet isPasswordSet;
    private final Lazy<GetMerchantPreference> getMerchantPreference;
    private final Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus;
    private final Lazy<SchedulerProvider> schedulerProvider;

    @Inject
    public DeleteSupplierTxnPresenter(
            @UiThread Scheduler uiScheduler,
            @ViewModelParam("tx_id") String transactionId,
            DeleteSupplierTransaction deleteSupplierTransaction,
            GetSupplierTransaction getSupplierTransaction,
            IsPasswordSet isPasswordSet,
            Lazy<GetMerchantPreference> getMerchantPreference,
            Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus,
            Lazy<SchedulerProvider> schedulerProvider) {
        super(uiScheduler);
        this.transactionId = transactionId;
        this.deleteSupplierTransaction = deleteSupplierTransaction;
        this.getSupplierTransaction = getSupplierTransaction;
        this.isPasswordSet = isPasswordSet;
        this.getMerchantPreference = getMerchantPreference;
        this.merchantPrefSyncStatus = merchantPrefSyncStatus;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    protected void loadData() {

        addTask(
                getSupplierTransaction
                        .execute(transactionId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                transaction1 ->
                                        ifAttached(view -> view.setTransaction(transaction1)),
                                throwable -> ifAttached(view -> view.showError())));

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
    public void delete(String transactionId) {

        ifAttached(view -> view.showLoading());

        addTask(
                deleteSupplierTransaction
                        .execute(transactionId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                () -> {
                                    ifAttached(view -> view.hideLoading());
                                    ifAttached(view -> view.goToCustomerScreen());
                                },
                                throwable -> {
                                    ifAttached(view -> view.hideLoading());
                                    ifAttached(view -> view.showError());
                                }));
    }

    @Override
    public void checkPasswordSet() {
        ifAttached(view -> view.showDeleteLoading());
        addTask(
                isPasswordSet
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    ifAttached(view -> view.hideDeleteLoading());
                                    if (it) {
                                        checkIsSyncPrefDone();
                                    } else {
                                        ifAttached(view -> view.goToSetNewPinScreen());
                                    }
                                },
                                throwable -> {
                                    ifAttached(view -> view.hideDeleteLoading());
                                    if (isInternetIssue(throwable)) {
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.hideLoading());
                                        ifAttached(view -> view.showError());
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
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    if (Boolean.parseBoolean(it)) {
                                        ifAttached(view -> view.goToAuthScreen());
                                    } else {
                                        ifAttached(view -> view.showUpdatePinDialog());
                                    }
                                },
                                e -> {
                                    ifAttached(view -> view.hideDeleteLoading());
                                    if (isInternetIssue(e)) {
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.hideLoading());
                                        ifAttached(view -> view.showError());
                                    }
                                }));
    }

    private void checkIsSyncPrefDone() {
        addTask(
                merchantPrefSyncStatus
                        .get()
                        .checkMerchantPrefSync()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    if (it) {
                                        checkIsFourDigitPin();
                                    } else {
                                        syncMerchantPref();
                                    }
                                },
                                e -> {
                                    if (isAuthenticationIssue(e)) {
                                        ifAttached(view -> view.gotoLogin());
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.showError());
                                    }
                                }));
    }

    private void syncMerchantPref() {
        addTask(
                merchantPrefSyncStatus
                        .get()
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                () -> {
                                    checkIsFourDigitPin();
                                },
                                e -> {
                                    if (isAuthenticationIssue(e)) {
                                        ifAttached(view -> view.gotoLogin());
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.showError());
                                    }
                                }));
    }
}
