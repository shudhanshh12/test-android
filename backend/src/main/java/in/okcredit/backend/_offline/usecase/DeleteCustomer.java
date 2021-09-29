package in.okcredit.backend._offline.usecase;

import in.okcredit.backend._offline.database.CustomerRepo;
import in.okcredit.backend._offline.error.CustomerErrors;
import in.okcredit.backend._offline.server.BackendRemoteSource;
import in.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import in.okcredit.merchant.core.CoreSdk;
import in.okcredit.merchant.core.common.CoreException;
import io.reactivex.Completable;
import javax.inject.Inject;

public final class DeleteCustomer {
    private BackendRemoteSource server;
    private SyncCustomer syncCustomer;
    private CustomerRepo customerRepo;
    private CoreSdk coreSdk;
    private GetActiveBusinessId getActiveBusinessId;

    @Inject
    public DeleteCustomer(
            BackendRemoteSource server,
            SyncCustomer syncCustomer,
            CustomerRepo customerRepo,
            CoreSdk coreSdk,
            GetActiveBusinessId getActiveBusinessId) {

        this.server = server;
        this.syncCustomer = syncCustomer;
        this.customerRepo = customerRepo;
        this.coreSdk = coreSdk;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    public Completable execute(String customerId, String businessId) {
        return getActiveBusinessId.thisOrActiveBusinessId(businessId).flatMapCompletable(_businessId ->
                coreSdk.isCoreSdkFeatureEnabled(_businessId)
                        .flatMapCompletable(
                                it -> {
                                    if (it) {
                                        return coreExecute(customerId, _businessId);
                                    } else {
                                        return backendExecute(customerId, _businessId);
                                    }
                                }
        ));
    }

    private Completable backendExecute(String customerId, String businessId) {
        return syncCustomer
                .execute(customerId, businessId) // sync customer (to handle any dirty transactions)
                .andThen(server.deleteCustomer(customerId, businessId)) // delete on server
                .andThen(server.getCustomer(customerId, businessId)) // fetch updated copy
                .flatMapCompletable(customer -> customerRepo.putCustomer(customer, businessId)); // save locally
    }

    private Completable coreExecute(String customerId, String businessId) {
        return coreSdk.deleteCustomer(customerId, businessId)
                .onErrorResumeNext(
                        error -> {
                            if (error instanceof CoreException.DeletePermissionDenied) {
                                CoreException.DeletePermissionDenied deletePermissionDenied =
                                        (CoreException.DeletePermissionDenied) error;
                                return Completable.error(
                                        new CustomerErrors.DeletePermissionDenied(
                                                deletePermissionDenied.getErrorMessage()));
                            }
                            return Completable.error(error);
                        });
    }
}
