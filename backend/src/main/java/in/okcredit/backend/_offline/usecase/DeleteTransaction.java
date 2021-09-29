package in.okcredit.backend._offline.usecase;

import javax.inject.Inject;

import in.okcredit.backend._offline.database.TransactionRepo;
import in.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer;
import in.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import in.okcredit.merchant.core.Command;
import in.okcredit.merchant.core.CoreSdk;
import io.reactivex.Completable;
import io.reactivex.Single;
import merchant.okcredit.accounting.model.Transaction;

public final class DeleteTransaction {

    private TransactionRepo transactionRepo;
    private SyncCustomer syncCustomer;
    private SyncTransactionsImpl syncTransactionsImpl;
    private CoreSdk coreSdk;
    private GetActiveBusinessId getActiveBusinessId;

    @Inject
    public DeleteTransaction(
            TransactionRepo transactionRepo,
            SyncCustomer syncCustomer,
            SyncTransactionsImpl SyncTransactionsImpl,
            CoreSdk coreSdk,
            GetActiveBusinessId getActiveBusinessId) {

        this.transactionRepo = transactionRepo;
        this.syncCustomer = syncCustomer;
        this.syncTransactionsImpl = SyncTransactionsImpl;
        this.coreSdk = coreSdk;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    public Completable execute(String txnId) {
        return getActiveBusinessId.execute().flatMapCompletable(businessId ->
                coreSdk.isCoreSdkFeatureEnabled(businessId)
                        .flatMapCompletable(
                                it -> {
                                    if (it) {
                                        return coreDeleteTransaction(txnId, businessId);
                                    } else {
                                        return backendDeleteTransaction(txnId, businessId);
                                    }
                                })
        );
    }

    private Completable coreDeleteTransaction(String txnId, String businessId) {
        return coreSdk.processTransactionCommand(new Command.DeleteTransaction(txnId), businessId)
                .flatMapCompletable(
                        transaction -> syncCustomer.schedule(transaction.getCustomerId(), businessId));
    }

    private Completable backendDeleteTransaction(String txnId, String businessId) {
        return delete(txnId, businessId)
                .flatMapCompletable(
                        transaction ->
                                transactionRepo
                                        .putTransaction(transaction, businessId)
                                        .andThen(syncCustomer.schedule(transaction.getCustomerId(), businessId))
                                        .andThen(syncTransactionsImpl.schedule("delete_txn", businessId)));
    }

    private Single<Transaction> delete(String txnId,String businessId) {
        return transactionRepo
                .getTransaction(txnId, businessId)
                .firstOrError()
                .map(transaction -> transaction.asDeleted().withDirty(true));
    }
}
