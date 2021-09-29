package in.okcredit.backend._offline.usecase;

import javax.inject.Inject;

import in.okcredit.backend._offline.database.TransactionRepo;
import in.okcredit.backend._offline.server.BackendRemoteSource;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import io.reactivex.Observable;
import merchant.okcredit.accounting.model.Transaction;

public final class GetTransaction {
    private TransactionRepo transactionRepo;
    private BackendRemoteSource server;
    private GetActiveBusinessId getActiveBusinessId;

    @Inject
    public GetTransaction(
            TransactionRepo transactionRepo,
            BackendRemoteSource server,
            GetActiveBusinessId getActiveBusinessId
    ) {
        this.transactionRepo = transactionRepo;
        this.server = server;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    public Observable<Transaction> execute(String txnId) {
        return getActiveBusinessId.execute().flatMapObservable(businessId ->
                getTransaction(txnId, businessId)
        );
    }

    private Observable<Transaction> getTransaction(String txnId, String businessId) {
        return transactionRepo
                .isTransactionPresent(txnId, businessId)
                .flatMapObservable(
                        isPresent -> {
                            if (isPresent) {
                                return transactionRepo.getTransaction(txnId, businessId);
                            } else {
                                return server.getTransaction(txnId, businessId)
                                        .flatMapCompletable(
                                                transaction ->
                                                        transactionRepo.putTransaction(transaction, businessId))
                                        .andThen(transactionRepo.getTransaction(txnId, businessId));
                            }
                        });
    }
}
