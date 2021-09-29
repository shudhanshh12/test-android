package in.okcredit.ui.delete_txn;

import javax.inject.Inject;

import dagger.Lazy;
import in.okcredit.di.UiThread;
import in.okcredit.backend._offline.usecase.DeleteTransaction;
import in.okcredit.backend._offline.usecase.GetTransaction;
import in.okcredit.backend.contract.GetMerchantPreference;
import in.okcredit.fileupload.utils.SchedulerProvider;
import in.okcredit.individual.contract.PreferenceKey;
import in.okcredit.merchant.contract.Business;
import in.okcredit.merchant.customer_ui.usecase.DeleteDiscount;
import in.okcredit.merchant.usecase.GetActiveBusinessImpl;
import in.okcredit.shared._base_v2.BasePresenter;
import in.okcredit.shared._base_v2.MVP;
import in.okcredit.shared.usecase.Result;
import in.okcredit.ui._base_v2.BaseContracts;
import io.reactivex.Scheduler;
import kotlin.Unit;
import tech.okcredit.android.auth.usecases.IsPasswordSet;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import tech.okcredit.contract.MerchantPrefSyncStatus;

public class DeleteTxnPresenter extends BasePresenter<DeleteTxnContract.View>
        implements DeleteTxnContract.Presenter {

    private final String transactionId;

    private final GetTransaction getTransaction;
    private final DeleteTransaction deleteTransaction;
    private final DeleteDiscount deleteDiscount;
    private final GetActiveBusinessImpl getActiveBusiness;
    private final IsPasswordSet isPasswordSet;
    private Business business;
    private final Lazy<GetMerchantPreference> getMerchantPreference;
    private final Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus;
    private final Lazy<SchedulerProvider> schedulerProvider;

    @Inject
    public DeleteTxnPresenter(
            @UiThread Scheduler uiScheduler,
            @ViewModelParam("tx_id") String transactionId,
            GetTransaction getTransaction,
            DeleteTransaction deleteTransaction,
            GetActiveBusinessImpl getActiveBusiness,
            IsPasswordSet isPasswordSet,
            DeleteDiscount deleteDiscount,
            Lazy<GetMerchantPreference> getMerchantPreference,
            Lazy<MerchantPrefSyncStatus> merchantPrefSyncStatus,
            Lazy<SchedulerProvider> schedulerProvider) {
        super(uiScheduler);
        this.transactionId = transactionId;
        this.getTransaction = getTransaction;
        this.deleteTransaction = deleteTransaction;
        this.getActiveBusiness = getActiveBusiness;
        this.isPasswordSet = isPasswordSet;
        this.deleteDiscount = deleteDiscount;
        this.getMerchantPreference = getMerchantPreference;
        this.merchantPrefSyncStatus = merchantPrefSyncStatus;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    protected void loadData() {

        addTask(
                getTransaction
                        .execute(transactionId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                transaction1 ->
                                        ifAttached(view -> view.setTransaction(transaction1)),
                                throwable -> ifAttached(MVP.View::showError)));

        addTask(
                getActiveBusiness
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribe(
                                business -> this.business = business,
                                throwable -> ifAttached(MVP.View::showError)));
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

        ifAttached(BaseContracts.Loading.View::showLoading);

        addTask(
                deleteTransaction
                        .execute(transactionId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                () -> {
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                    ifAttached(DeleteTxnContract.View::goToCustomerScreen);
                                },
                                throwable -> {
                                    ifAttached(BaseContracts.Loading.View::hideLoading);
                                    ifAttached(MVP.View::showError);
                                }));
    }

    @Override
    public void deleteDiscount(String transactionId) {

        ifAttached(BaseContracts.Loading.View::showLoading);

        addTask(
                deleteDiscount
                        .execute(transactionId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                unitResult -> {
                                    if (unitResult instanceof Result.Failure) {
                                        ifAttached(BaseContracts.Loading.View::hideLoading);
                                        Throwable throwable =
                                                ((Result.Failure<Unit>) unitResult).getError();
                                        if (isInternetIssue(throwable)) {
                                            ifAttached(
                                                    BaseContracts.Online.View
                                                            ::showNoInternetMessage);
                                        } else {
                                            ifAttached(MVP.View::showError);
                                        }
                                    }
                                    if (unitResult instanceof Result.Success) {
                                        ifAttached(BaseContracts.Loading.View::hideLoading);
                                        ifAttached(DeleteTxnContract.View::goToCustomerScreen);
                                    }
                                }));
    }

    @Override
    public void checkPasswordSet() {
        if (business != null) {
            executePasswordSet();
        } else {
            addTask(
                    getActiveBusiness
                            .execute()
                            .observeOn(uiScheduler)
                            .subscribe(
                                    business -> {
                                        this.business = business;
                                        executePasswordSet();
                                    },
                                    throwable -> ifAttached(MVP.View::showError)));
        }
    }

    private void executePasswordSet() {
        ifAttached(DeleteTxnContract.View::showDeleteLoading);
        addTask(
                isPasswordSet
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    ifAttached(DeleteTxnContract.View::hideDeleteLoading);
                                    if (it) {
                                        checkIsSyncPrefDone();
                                    } else {
                                        ifAttached(DeleteTxnContract.View::goToSetNewPinScreen);
                                    }
                                },
                                throwable -> {
                                    ifAttached(DeleteTxnContract.View::hideDeleteLoading);
                                    if (isInternetIssue(throwable)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(BaseContracts.Loading.View::hideLoading);
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
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                (it) -> {
                                    if (Boolean.parseBoolean(it)) {
                                        ifAttached(DeleteTxnContract.View::goToAuthScreen);
                                    } else {
                                        ifAttached(DeleteTxnContract.View::showUpdatePinDialog);
                                    }
                                },
                                e -> {
                                    ifAttached(DeleteTxnContract.View::hideDeleteLoading);
                                    if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(BaseContracts.Loading.View::hideLoading);
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
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(e)) {
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
                        .subscribeOn(schedulerProvider.get().api())
                        .subscribe(
                                this::checkIsFourDigitPin,
                                e -> {
                                    if (isAuthenticationIssue(e)) {
                                        ifAttached(BaseContracts.Authenticated.View::gotoLogin);
                                    } else if (isInternetIssue(e)) {
                                        ifAttached(
                                                BaseContracts.Online.View::showNoInternetMessage);
                                    } else {
                                        ifAttached(MVP.View::showError);
                                    }
                                }));
    }
}
