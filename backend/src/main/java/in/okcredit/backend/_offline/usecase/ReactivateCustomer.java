package in.okcredit.backend._offline.usecase;

import com.google.common.base.Strings;
import dagger.Lazy;
import in.okcredit.backend._offline.common.CoreModuleMapper;
import in.okcredit.backend._offline.database.CustomerRepo;
import in.okcredit.backend._offline.error.CustomerErrors;
import in.okcredit.backend.contract.Customer;
import in.okcredit.backend._offline.server.BackendRemoteSource;
import in.okcredit.fileupload.usecase.IUploadFile;
import in.okcredit.merchant.core.CoreSdk;
import in.okcredit.merchant.core.common.CoreException;
import io.reactivex.Single;
import in.okcredit.merchant.contract.GetActiveBusinessId;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;

public final class ReactivateCustomer {
    private Lazy<CustomerRepo> customerRepo;
    private Lazy<BackendRemoteSource> server;
    private Lazy<IUploadFile> uploadFile;
    private Lazy<CoreSdk> coreSdk;
    private Lazy<GetActiveBusinessId> getActiveBusinessId;
    @Inject
    public ReactivateCustomer(
            Lazy<CustomerRepo> customerRepo,
            Lazy<BackendRemoteSource> server,
            Lazy<IUploadFile> uploadFile,
            Lazy<CoreSdk> coreSdk,
            Lazy<GetActiveBusinessId> getActiveBusinessId) {
        this.customerRepo = customerRepo;
        this.server = server;
        this.uploadFile = uploadFile;
        this.coreSdk = coreSdk;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    public Single<Customer> execute(
            String name, String customerId, @Nullable String localProfileImage) {
        return getActiveBusinessId.get().execute().flatMap(businessId ->
                reactivateCustomer(name, customerId, localProfileImage, businessId)
        );
    }

    private Single<Customer> reactivateCustomer(
            String name,
            String customerId,
            @Nullable String localProfileImage,
            String businessId
    ){
        return coreSdk.get()
                .isCoreSdkFeatureEnabled(businessId)
                .flatMap(
                        it -> {
                            if (it) {
                                return coreExecute(name, customerId, localProfileImage, businessId);
                            } else {
                                return backendExecute(name, customerId, localProfileImage, businessId);
                            }
                        });
    }
    private Single<Customer> backendExecute(
            String name,
            String customerId,
            @Nullable String localProfileImage,
            String businessId
    ) {
        String profileRemoteUrl = null;
        if (!Strings.isNullOrEmpty(localProfileImage)) {
            profileRemoteUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID() + ".jpg";
        }
        String finalProfileRemoteUrl = profileRemoteUrl;

        return validateName(name)
                .flatMap(s -> customerRepo.get().getCustomer(customerId, businessId).firstOrError())
                .flatMap(
                        local ->
                                server.get()
                                        .addCustomer(
                                                backend_getName(name, local),
                                                local.getMobile(),
                                                true,
                                                finalProfileRemoteUrl,
                                                businessId))
                .flatMap(
                        server -> {
                            if (Strings.isNullOrEmpty(finalProfileRemoteUrl)) {
                                return customerRepo
                                        .get()
                                        .putCustomer(server, businessId)
                                        .andThen(Single.just(server));
                            } else {
                                return customerRepo
                                        .get()
                                        .putCustomer(server, businessId)
                                        .andThen(
                                                uploadFile
                                                        .get()
                                                        .schedule(
                                                                IUploadFile.CUSTOMER_PHOTO,
                                                                finalProfileRemoteUrl,
                                                                localProfileImage))
                                        .andThen(Single.just(server));
                            }
                        });
    }

    // If user wish to change the name of customer while reactivating, then he will provide name .
    // We will validate it
    // or else we will get existing name from db
    // This is just a reactivation , so name is not compulsory , we already have it in db
    private Single<String> validateName(String name) {
        if (name == null || name.isEmpty()) {
            return Single.just(name);
        } else if (name.length() > 30) {
            return Single.error(new CustomerErrors.InvalidName());
        } else {
            return Single.just(name);
        }
    }

    private String backend_getName(String name, Customer local) {
        if (name == null || name.isEmpty()) {
            return local.getDescription();
        } else {
            return name;
        }
    }

    private Single<Customer> coreExecute(
            String name, String customerId,
            @Nullable String localProfileImage,
            String businessId
    ) {
        return coreSdk.get()
                .reactivateCustomer(name, customerId, localProfileImage, businessId)
                .map(customer -> CoreModuleMapper.INSTANCE.toCustomer(customer))
                .onErrorResumeNext(
                        error -> {
                            if (error instanceof CoreException.MobileConflict) {
                                Customer conflict =
                                        CoreModuleMapper.toCustomer(
                                                ((CoreException.MobileConflict) error)
                                                        .getConflict());
                                return Single.<Customer>error(
                                        new CustomerErrors.MobileConflict(conflict));
                            } else if (error instanceof CoreException.DeletedCustomer) {
                                Customer conflict =
                                        CoreModuleMapper.toCustomer(
                                                ((CoreException.DeletedCustomer) error)
                                                        .getConflict());
                                return Single.<Customer>error(
                                        new CustomerErrors.DeletedCustomer(conflict));
                            } else if (error instanceof CoreException.InvalidName) {
                                return Single.<Customer>error(new CustomerErrors.InvalidName());
                            }
                            return Single.<Customer>error(error);
                        });
    }
}
