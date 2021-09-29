package in.okcredit.backend._offline.usecase;

import dagger.Lazy;
import dagger.Reusable;
import in.okcredit.backend._offline.common.CoreModuleMapper;
import in.okcredit.backend._offline.database.TransactionRepo;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import in.okcredit.merchant.core.CoreSdk;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import merchant.okcredit.accounting.model.Transaction;

@Reusable
public final class GetUnSyncedCustomers {
    private Lazy<TransactionRepo> transactionRepo;
    private Lazy<CoreSdk> coreSdk;
    private Lazy<GetActiveBusinessId> getActiveBusinessId;
    @Inject
    public GetUnSyncedCustomers(
            Lazy<TransactionRepo> transactionRepo,
            Lazy<CoreSdk> coreSdk,
            Lazy<GetActiveBusinessId> getActiveBusinessId
    ) {
        this.transactionRepo = transactionRepo;
        this.coreSdk = coreSdk;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    public Observable<List<String>> execute() {
        return getActiveBusinessId.get().execute().flatMapObservable(businessId ->
                coreSdk.get()
                        .isCoreSdkFeatureEnabled(businessId)
                        .flatMapObservable(
                                it -> {
                                    if (it) {
                                        return coreExecute(businessId);
                                    } else {
                                        return backendExecute(businessId);
                                    }
                                })
        );
    }

    private Observable<List<String>> backendExecute(String businessId) {
        return transactionRepo
                .get()
                .listDirtyTransactions(null, businessId)
                .flatMap(
                        unsyncedTxns -> {
                            List<String> unsyncedCustomers = new ArrayList<>();
                            for (Transaction unsyncedTxn : unsyncedTxns) {
                                if (!unsyncedCustomers.contains(unsyncedTxn.getCustomerId())) {
                                    unsyncedCustomers.add(unsyncedTxn.getCustomerId());
                                }
                            }

                            return Observable.just(unsyncedCustomers);
                        });
    }

    private Observable<List<String>> coreExecute(String businessId) {
        return coreSdk.get()
                .listDirtyTransactions(true, businessId)
                .map(
                        transactions -> {
                            List<Transaction> transactionList = new ArrayList<>();
                            for (in.okcredit.merchant.core.model.Transaction coreTransaction :
                                    transactions) {
                                transactionList.add(
                                        CoreModuleMapper.INSTANCE.toTransaction(coreTransaction));
                            }
                            return transactionList;
                        })
                .flatMap(
                        unsyncedTxns -> {
                            List<String> unsyncedCustomers = new ArrayList<>();
                            for (Transaction unsyncedTxn : unsyncedTxns) {
                                if (!unsyncedCustomers.contains(unsyncedTxn.getCustomerId())) {
                                    unsyncedCustomers.add(unsyncedTxn.getCustomerId());
                                }
                            }
                            return Observable.just(unsyncedCustomers);
                        });
    }
}
