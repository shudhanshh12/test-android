package in.okcredit.backend._offline.server.internal;

import com.google.common.base.Converter;
import javax.inject.Inject;
import merchant.okcredit.accounting.model.Transaction;
import org.joda.time.DateTime;

public class TransactionMapper
        extends Converter<in.okcredit.backend._offline.server.internal.Transaction, Transaction> {

    @Inject
    public TransactionMapper() {}

    @Override
    protected Transaction doForward(
            in.okcredit.backend._offline.server.internal.Transaction apiEntity) {

        // billDate and updatedAt added on version 1.19.0. setting default value here
        DateTime billDate = apiEntity.billDate;
        DateTime updatedAt = apiEntity.updatedAt;

        if (billDate == null) {
            billDate = apiEntity.createdAt;
        }

        if (updatedAt == null) {
            if (apiEntity.isDeleted && apiEntity.deleteTime != null) {
                updatedAt = apiEntity.deleteTime;
            } else {
                updatedAt = apiEntity.createdAt;
            }
        }

        long amountV2 = apiEntity.amountV2;
        if (apiEntity.amount != 0 && apiEntity.amountV2 == 0L) {
            amountV2 = (long) (apiEntity.amount * 100);
        }

        return new Transaction(
                apiEntity.id,
                apiEntity.type,
                apiEntity.customerId,
                apiEntity.collectionId,
                amountV2,
                apiEntity.transactionImageList,
                apiEntity.note,
                apiEntity.createdAt,
                apiEntity.isOnboarding,
                apiEntity.isDeleted,
                apiEntity.deleteTime,
                false,
                billDate,
                updatedAt,
                true,
                apiEntity.createdByCustomer,
                apiEntity.deletedByCustomer,
                "",
                "",
                apiEntity.transactionState,
                apiEntity.transactionCategory,
                apiEntity.amountUpdated,
                apiEntity.amountUpdatedAt);
    }

    @Override
    protected in.okcredit.backend._offline.server.internal.Transaction doBackward(
            Transaction transaction) {
        throw new IllegalStateException(
                "illegal operation: cannot convert transaction domain entity to api entity");
    }
}
