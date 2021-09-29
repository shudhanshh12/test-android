package in.okcredit.ui.delete_txn.supplier.supplier;

import in.okcredit.di.UiThread;
import in.okcredit.analytics.PropertyKey;
import in.okcredit.analytics.PropertyValue;
import in.okcredit.analytics.Screen;
import in.okcredit.analytics.Tracker;
import in.okcredit.frontend.usecase.supplier.DeleteSupplier;
import in.okcredit.merchant.contract.Business;
import in.okcredit.merchant.suppliercredit.Supplier;
import in.okcredit.merchant.usecase.GetActiveBusinessImpl;
import in.okcredit.shared._base_v2.BasePresenter;
import in.okcredit.supplier.usecase.GetSupplier;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import merchant.okcredit.accounting.model.Transaction;
import tech.okcredit.android.auth.IncorrectPassword;
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam;
import timber.log.Timber;

public class DeleteSupplierPresenter extends BasePresenter<DeleteSupplierContract.View>
        implements DeleteSupplierContract.Presenter {
    private String supplierId;
    private Supplier supplier;

    private GetSupplier getSupplier;
    private GetActiveBusinessImpl getActiveBusiness;
    private DeleteSupplier deleteSupplier;
    private Business business;
    private Tracker tracker;

    @Inject
    public DeleteSupplierPresenter(
            @UiThread Scheduler uiScheduler,
            @ViewModelParam("customer_id") String supplierId,
            GetSupplier getSupplier,
            DeleteSupplier deleteSupplier,
            Tracker tracker,
            GetActiveBusinessImpl getActiveBusiness) {

        super(uiScheduler);
        this.supplierId = supplierId;
        this.getActiveBusiness = getActiveBusiness;
        this.getSupplier = getSupplier;
        this.deleteSupplier = deleteSupplier;
        this.tracker = tracker;
    }

    @Override
    protected void loadData() {
        addTask(
                getActiveBusiness
                        .execute()
                        .observeOn(uiScheduler)
                        .subscribe(
                                (merchant) -> this.business = merchant,
                                throwable -> {
                                    Timber.e(throwable);
                                    ifAttached(view -> view.hideLoading());
                                    if (isAuthenticationIssue(throwable)) {
                                        ifAttached(view -> view.gotoLogin());
                                    } else if (isInternetIssue(throwable)) {
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.showError());
                                    }
                                }));

        addTask(
                getSupplier
                        .executeObservable(supplierId)
                        .observeOn(uiScheduler)
                        .subscribe(
                                supplier -> {
                                    this.supplier = supplier;
                                    ifAttached(view -> view.setCustomer(supplier));
                                },
                                throwable -> {
                                    ifAttached(view -> view.hideLoading());
                                    if (isAuthenticationIssue(throwable)) {
                                        ifAttached(view -> view.gotoLogin());
                                    } else if (isInternetIssue(throwable)) {
                                        ifAttached(view -> view.showNoInternetMessage());
                                    } else {
                                        ifAttached(view -> view.showError());
                                    }
                                }));
    }

    @Override
    public void delete() {
        ifAttached(view -> view.showLoading());
        Completable.fromAction(
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                addTask(
                                        deleteSupplier
                                                .execute(supplierId)
                                                .observeOn(uiScheduler)
                                                .subscribe(
                                                        () -> {
                                                            tracker.trackDeleteRelationship(
                                                                    PropertyValue.SUPPLIER,
                                                                    supplier.getId(),
                                                                    null);
                                                            ifAttached(
                                                                    view -> view.gotoHomeScreen());
                                                            ifAttached(view -> view.hideLoading());
                                                        },
                                                        throwable -> {
                                                            Timber.d(
                                                                    "Delete Customer Error: %s",
                                                                    throwable.getMessage());
                                                            Timber.e(throwable);
                                                            ifAttached(view -> view.hideLoading());
                                                            if (throwable
                                                                    instanceof IncorrectPassword) {
                                                                ifAttached(
                                                                        view ->
                                                                                view
                                                                                        .showIncorrectPasswordError());
                                                            } else if (isAuthenticationIssue(
                                                                    throwable)) {
                                                                ifAttached(
                                                                        view -> view.gotoLogin());
                                                            } else if (isInternetIssue(throwable)) {
                                                                ifAttached(
                                                                        view ->
                                                                                view
                                                                                        .showNoInternetMessage());
                                                            } else {
                                                                ifAttached(
                                                                        view -> view.showError());
                                                            }
                                                        }));
                            }
                        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @Override
    public void settle() {
        if (supplier == null) {
            return;
        }
        if (supplier.getBalance() < 0) {
            tracker.trackAddTransactionFlowsStarted(
                    PropertyKey.PAYMENT,
                    PropertyValue.SUPPLIER,
                    supplierId,
                    Screen.DELETE_SUPPLIER);
            ifAttached(
                    view ->
                            view.gotoAddTxnScreen(
                                    supplierId, Transaction.PAYMENT, supplier.getBalance()));
        } else if (supplier.getBalance() > 0) {
            tracker.trackAddTransactionFlowsStarted(
                    PropertyKey.CREDIT,
                    PropertyValue.SUPPLIER,
                    supplierId,
                    Screen.DELETE_SUPPLIER);
            ifAttached(
                    view ->
                            view.gotoAddTxnScreen(
                                    supplierId, Transaction.CREDIT, supplier.getBalance()));
        }
    }

    @Override
    public void getSupplierAmount() {}

    @Override
    public void onInternetRestored() {
        loadData();
    }

    @Override
    public void onAuthenticationRestored() {
        loadData();
    }
}
