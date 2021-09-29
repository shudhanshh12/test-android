package in.okcredit.backend._offline.usecase;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import in.okcredit.backend._offline.database.CustomerRepo;
import in.okcredit.backend.contract.Customer;
import in.okcredit.backend.contract.GetCustomerAccountNetBalance;
import in.okcredit.merchant.contract.GetActiveBusinessId;
import io.reactivex.Observable;
import javax.inject.Inject;

public final class GetAccountSummary implements GetCustomerAccountNetBalance {
    private CustomerRepo customerRepo;
    private GetActiveBusinessId getActiveBusinessId;

    @Inject
    public GetAccountSummary(CustomerRepo customerRepo, GetActiveBusinessId getActiveBusinessId) {
        this.customerRepo = customerRepo;
        this.getActiveBusinessId = getActiveBusinessId;
    }

    public Observable<AccountSummary> execute() {
        return getActiveBusinessId.execute().flatMapObservable(this::executeForBusiness);
    }

    private Observable<AccountSummary> executeForBusiness(String businessId){
        return customerRepo
                .listActiveCustomers(businessId)
                .map(
                        customers -> {
                            long balance = 0L;
                            int count = 0;
                            for (Customer customer : customers) {
                                if (customer.getState() != Customer.State.BLOCKED) {
                                    balance += customer.getBalanceV2();
                                    count++;
                                }
                            }
                            return new AccountSummary(balance, count);
                        });
    }
    @NotNull
    @Override
    public Observable<Long> getNetBalance(@NonNull String businessId) {
        return executeForBusiness(businessId).map(
                summary -> summary.balance
        );
    }

    public static final class AccountSummary {
        final long balance;
        private final int customerCount;

        public AccountSummary(long balance, int customerCount) {
            this.balance = balance;
            this.customerCount = customerCount;
        }

        public long getBalance() {
            return balance;
        }

        public int getCustomerCount() {
            return customerCount;
        }
    }
}
