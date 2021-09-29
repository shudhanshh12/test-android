package in.okcredit.backend._offline.database.internal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import org.jetbrains.annotations.NotNull;


import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.List;

@Dao
public abstract class TransactionDao {

    @Query("SELECT * FROM `transaction` WHERE customerId = :customerId AND businessId = :businessId ORDER BY createdAt DESC")
    public abstract Flowable<List<DbEntities.Transaction>> listTransactions(String customerId, String businessId);

    @Query(
            "SELECT * FROM `transaction` WHERE customerId = :customerId AND isDeleted == 0 AND transactionState != 0 " +
                    "AND businessId = :businessId" +
                    " ORDER BY "
                    + "billDate DESC")
    public abstract Observable<List<DbEntities.Transaction>> listNonDeletedTransactionsByBillDate(
            String customerId,
            String businessId);

    // Returns transactions sorted by create time in ascending order
    @Query(
            "SELECT * FROM `transaction` WHERE customerId = :customerId  AND createdAt > :txStartTime AND businessId = :businessId ORDER BY " +
                    "createdAt ASC")
    public abstract Flowable<List<DbEntities.Transaction>> listTransactions(
            String customerId, long txStartTime, String businessId);

    @Query(
            "SELECT * FROM `transaction` WHERE customerId = :customerId  AND createdAt > :txStartTime AND businessId = :businessId ORDER BY " +
                    "billDate ASC, createdAt ASC")
    public abstract Flowable<List<DbEntities.Transaction>> listTransactionsSortedByBillDate(
            String customerId, long txStartTime, String businessId);

    @Query("SELECT * FROM `transaction` WHERE businessId = :businessId ORDER BY createdAt DESC")
    public abstract Flowable<List<DbEntities.Transaction>> listTransactions(String businessId);

    @Query(
            "SELECT * FROM `transaction` WHERE billDate > :startTime AND billDate <= :endTime AND isDeleted == 0 AND businessId = :businessId" +
                    " ORDER BY billDate DESC")
    public abstract Flowable<List<DbEntities.Transaction>> listActiveTransactionsBetweenBillDate(
            long startTime, long endTime, String businessId);

    @Query(
            "SELECT * FROM `transaction` WHERE customerId = :customerId AND createdAt > :customerTxnTime and billDate" +
                    " > :startTime AND billDate <= :endTime AND isDeleted == 0 AND businessId = :businessId ORDER BY " +
                    "billDate DESC")
    public abstract Observable<List<DbEntities.Transaction>>
            listCustomerActiveTransactionsBetweenBillDate(
                    String customerId, long customerTxnTime, long startTime, long endTime, String businessId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTransaction(DbEntities.Transaction... transaction);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertTransactionWithIgnoreConflictStrategy(
            DbEntities.Transaction... transaction);

    @Query("DELETE FROM `transaction` WHERE businessId = :businessId")
    public abstract void clearTransactions(String businessId);

    @Query("DELETE FROM `transaction`")
    public abstract void deleteAllTransactions();

    @Query("DELETE FROM `transaction`  WHERE customerId = :customerId AND businessId = :businessId")
    public abstract void deleteAllTransactionsForCustomer(String customerId, String businessId);

    @Query("SELECT count(*) FROM `Transaction` WHERE id = :txnId")
    public abstract Single<Integer> isTransactionPresent(String txnId);

    @Query("SELECT * FROM `Transaction` WHERE id = :txnId LIMIT 1")
    public abstract Flowable<DbEntities.Transaction> getTransaction(String txnId);

    @Query("SELECT * FROM `Transaction` WHERE collectionId = :collectionId AND businessId = :businessId LIMIT 1")
    public abstract Flowable<DbEntities.Transaction> getTransactionUsingCollectionId(
            String collectionId,
            String businessId);

    @Query("SELECT count(*) FROM `Transaction` WHERE collectionId = :collectionId AND businessId = :businessId")
    public abstract Single<Integer> getTransactionsCountForCollectionId(String collectionId, String businessId);

    @Query("SELECT id FROM `Transaction` WHERE collectionId = :collectionId AND businessId = :businessId")
    public abstract Single<String> getTransactionIdForCollectionId(String collectionId, String businessId);

    @Query("SELECT * FROM `transaction` WHERE isDirty = :isDirty AND businessId = :businessId")
    public abstract Flowable<List<DbEntities.Transaction>> listDirtyTransactions(boolean isDirty, String businessId);

    @Query("update `transaction` set receiptUrl = :jsonString where   id= :transactionId")
    public abstract void updateTransactionImage(String jsonString, String transactionId);

    @Query("update `transaction` set note = :note where   id= :transactionId")
    public abstract void updateTransactionNote(String note, String transactionId);

    @Query("update `transaction` set amount = :amount where   id= :transactionId")
    public abstract void updateTransactionAmount(long amount, String transactionId);

    @Query("SELECT MAX(updatedAt) FROM `Transaction` WHERE businessId = :businessId")
    public abstract Single<Long> lastUpdatedTransaction(String businessId);

    @Query("DELETE FROM `Transaction` WHERE id = :txnId")
    public abstract void deleteTransaction(String txnId);

    @Transaction
    public void replaceTransaction(String oldTxnId, DbEntities.Transaction newTxn) {
        deleteTransaction(oldTxnId);
        insertTransaction(newTxn);
    }

    @Query("SELECT count(*) FROM `Transaction` WHERE businessId = :businessId")
    public abstract Single<Integer> getAllTransactionsCount(String businessId);

    @Query("SELECT count(*) FROM `transaction` where updatedAt <= :time and isDirty=0 and businessId = :businessId")
    public abstract Single<Integer> getNumberOfSyncedTransactionsTillGivenUpdatedTime(long time, String businessId);

    @Query("SELECT count(*) FROM `Transaction` where type= :type and businessId = :businessId")
    public abstract Single<Integer> getAllTransactionsCountByType(int type, String businessId);

    @Query("select * from `transaction` where businessId = :businessId order by createdAt ASC limit 1")
    public abstract Single<DbEntities.Transaction> getFirstTransaction(String businessId);

    @Query("select * from `transaction` where businessId = :businessId order by createdAt DESC limit 1")
    public abstract Single<DbEntities.Transaction> getLastTransaction(String businessId);

    @Query("select * from `transaction` where  customerId = :customerId and createdByCustomer = 1 and " +
                    "deletedByCustomer = 0 and businessId = :businessId order by createdAt DESC limit 1")
    public abstract Single<DbEntities.Transaction> getLatestPaymentAddedByCustomer(
            String customerId,
            String businessId);

    @Query("select * from `transaction` where customerId = :customerId AND collectionId IS NOT NULL")
    public abstract Flowable<List<DbEntities.Transaction>> listOnlineTransactions(String customerId);

    @Query("select * from `Transaction` where  customerId = :customerId and deletedByCustomer = 0  order by createdAt DESC limit 1")
    public abstract Single<DbEntities.Transaction> getLatestTransaction(String customerId);
}
