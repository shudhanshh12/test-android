package in.okcredit.backend._offline.usecase;

import android.util.Pair;

import androidx.room.EmptyResultSetException;

import com.google.common.base.Strings;

import org.joda.time.DateTime;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;

import in.okcredit.backend._offline.common.CoreModuleMapper;
import in.okcredit.backend._offline.database.CustomerRepo;
import in.okcredit.backend._offline.database.DueInfoRepo;
import in.okcredit.backend._offline.error.CustomerErrors;
import in.okcredit.backend._offline.model.DueInfo;
import in.okcredit.backend._offline.server.BackendRemoteSource;
import in.okcredit.backend.contract.Customer;
import in.okcredit.fileupload.usecase.IUploadFile;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import in.okcredit.merchant.core.CoreSdk;
import in.okcredit.merchant.core.common.CoreException;
import in.okcredit.merchant.suppliercredit.SupplierCreditRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import tech.okcredit.android.auth.AuthService;
import tech.okcredit.android.base.utils.MobileUtils;

public final class AddCustomer {
    private final DueInfoRepo dueInfoRepo;
    private CustomerRepo customerRepo;
    private BackendRemoteSource server;
    private IUploadFile uploadFile;
    private SupplierCreditRepository supplierCreditRepository;
    private CoreSdk coreSdk;
    private AuthService authService;
    private GetActiveBusinessId getActiveBusinessId;

    @Inject
    public AddCustomer(
            CustomerRepo customerRepo,
            BackendRemoteSource server,
            IUploadFile uploadFile,
            SupplierCreditRepository supplierCreditRepository,
            DueInfoRepo dueInfoRepo,
            AuthService authService,
            CoreSdk coreSdk,
            GetActiveBusinessId getActiveBusinessId) {
        this.customerRepo = customerRepo;
        this.server = server;
        this.uploadFile = uploadFile;
        this.supplierCreditRepository = supplierCreditRepository;
        this.dueInfoRepo = dueInfoRepo;
        this.coreSdk = coreSdk;
        this.authService = authService;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    // TODO: Remove isSupplierAbEnabled and merchant from request.
    public Single<Customer> execute(String desc, String mobile, String localProfileImage) {
        return getActiveBusinessId.execute().flatMap(businessId ->
                coreSdk.isCoreSdkFeatureEnabled(businessId)
                        .flatMap(
                                it -> {
                                    if (it) {
                                        return coreExecute(desc, mobile, localProfileImage, businessId);
                                    } else {
                                        return backendExecute(desc, mobile, localProfileImage, businessId);
                                    }
                                })
        );
    }

    private Single<Customer> backendExecute(String desc, String mobile, String localProfileImage, String businessId) {
        String profileRemoteUrl = null;
        if (!Strings.isNullOrEmpty(localProfileImage)) {
            profileRemoteUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID() + ".jpg";
        }
        String finalProfileRemoteUrl = profileRemoteUrl;

        if (!Strings.isNullOrEmpty(mobile) && MobileUtils.parseMobile(mobile).length() != 10) {
            return Single.<Customer>error(new CustomerErrors.InvalidMobile());
        }

        return Single.zip(
                        validateMobile(mobile, businessId),
                        validateCyclicAccount(mobile, businessId),
                        (_mobile, mobile1) -> new Pair<>(desc, _mobile))
                .flatMap(
                        data ->
                                server.addCustomer(
                                        data.first,
                                        data.second,
                                        false,
                                        finalProfileRemoteUrl,
                                        businessId
                                )
                )
                .flatMap(
                        customer -> {
                            Completable scheduleReceipt = Completable.complete();
                            if (!Strings.isNullOrEmpty(finalProfileRemoteUrl)) {
                                scheduleReceipt =
                                        uploadFile.schedule(
                                                IUploadFile.CONTACT_PHOTO,
                                                finalProfileRemoteUrl,
                                                localProfileImage);
                            }

                            return customerRepo
                                    .putCustomer(customer, businessId)
                                    .andThen(scheduleReceipt)
                                    .andThen(
                                            dueInfoRepo.insertDueInfo(
                                                    new DueInfo(
                                                            customer.getId(),
                                                            false,
                                                            null,
                                                            false,
                                                            false),
                                                    businessId))
                                    .andThen(
                                            supplierCreditRepository
                                                    .scheduleSyncSupplierEnabledCustomerIds(businessId))
                                    .andThen(Single.just(customer));
                        });
    }

    private Single<Customer> coreExecute(String desc, String mobile, String localProfileImage, String businessId) {
        if (!Strings.isNullOrEmpty(mobile) && MobileUtils.parseMobile(mobile).length() != 10) {
            return Single.<Customer>error(new CustomerErrors.InvalidMobile());
        }
        return Single.zip(
                        validateMobile(mobile, businessId),
                        validateCyclicAccount(mobile, businessId),
                        (_mobile, mobile1) -> new Pair<>(desc, _mobile))
                .flatMap(
                        data ->
                                coreSdk.createCustomer(data.first, data.second, localProfileImage, businessId)
                                        .map(CoreModuleMapper::toCustomer))
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
                            }
                            return Single.<Customer>error(error);
                        })
                .flatMap(
                        customer ->
                                dueInfoRepo
                                        .insertDueInfo(
                                                new DueInfo(
                                                        customer.getId(),
                                                        false,
                                                        DateTime.now(),
                                                        false,
                                                        false),
                                                businessId)
                                        .andThen(
                                                supplierCreditRepository
                                                        .scheduleSyncSupplierEnabledCustomerIds(businessId))
                                        .andThen(Single.just(customer)));
    }

    // if this mobile number is already added as customer , we throw MobileConflict error
    private Single<String> validateMobile(String mobile, String businessId) {
        return coreSdk.isCoreSdkFeatureEnabled(businessId)
                .flatMap(
                        it -> {
                            if (it) {
                                return coreValidateMobile(mobile, businessId);
                            } else {
                                return backendValidateMobile(mobile, businessId);
                            }
                        });
    }

    private Single<String> backendValidateMobile(String mobile, String businessId) {
        if (Strings.isNullOrEmpty(mobile) || mobile.length() != 10) {
            return Single.just("");
        } else {
            return customerRepo
                    .findCustomerByMobile(mobile, businessId)
                    .flatMap(
                            customer -> {
                                if (customer.getStatus() == 1) {
                                    return Single.<String>error(
                                            new CustomerErrors.MobileConflict(customer));
                                } else {
                                    return Single.just(mobile);
                                }
                            })
                    .onErrorResumeNext(
                            (Function<Throwable, SingleSource<String>>)
                                    throwable -> {
                                        if (throwable instanceof NoSuchElementException) {
                                            return Single.just(mobile);
                                        } else {
                                            return Single.error(throwable);
                                        }
                                    });
        }
    }

    private Single<String> coreValidateMobile(String mobile, String businessId) {
        if (Strings.isNullOrEmpty(mobile) || mobile.length() != 10) {
            return Single.just("");
        } else {
            return coreSdk.getCustomerByMobile(mobile, businessId)
                    .map(CoreModuleMapper::toCustomer)
                    .flatMap(
                            customer -> {
                                if (customer.getStatus() == 1) {
                                    return Single.<String>error(
                                            new CustomerErrors.MobileConflict(customer));
                                } else {
                                    return Single.just(mobile);
                                }
                            })
                    .onErrorResumeNext(
                            (Function<Throwable, SingleSource<String>>)
                                    throwable -> {
                                        if (throwable instanceof NoSuchElementException) {
                                            return Single.just(mobile);
                                        } else {
                                            return Single.error(throwable);
                                        }
                                    });
        }
    }

    // if , merchant trying to added this mobile number as customer , but this mobile number is
    // already added as
    // supplier , then
    // 1. if that supplier is deleted type, we take user to supplier transaction screen and
    // un-delete this supplier
    // 2. if that supplier is active type, we show cyclic account error
    private Single<String> validateCyclicAccount(String mobile, String businessId) {
        // we validateCyclicAccount only when Supplier Credit feature is enabled for the merchant
        // (this device user)
        if (Strings.isNullOrEmpty(mobile) || mobile.length() != 10) {
            return Single.just("");
        } else {
            return supplierCreditRepository
                    .getSupplierByMobile(mobile, businessId)
                    .flatMap(
                            supplier -> {
                                // If merchant adds his own number as customer , then we don't
                                // consider Cyclic Account Error
                                if (authService.getMobile().equals(mobile)) {
                                    return Single.just(mobile);
                                } else {
                                    if (supplier.getDeleted()) {
                                        return Single.error(
                                                new CustomerErrors.DeletedCyclicAccount(
                                                        supplier)); // refer point 1
                                    } else {
                                        return Single.error(
                                                new CustomerErrors.ActiveCyclicAccount(
                                                        supplier)); // refer point 2
                                    }
                                }
                            })
                    .onErrorResumeNext(
                            (Function<Throwable, SingleSource<String>>)
                                    throwable -> {
                                        if (throwable instanceof EmptyResultSetException) {
                                            return Single.just(mobile);
                                        } else {
                                            return Single.error(throwable);
                                        }
                                    });
        }
    }
}
