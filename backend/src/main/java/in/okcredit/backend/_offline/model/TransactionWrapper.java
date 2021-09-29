package in.okcredit.backend._offline.model;

import merchant.okcredit.accounting.model.Transaction;

public final class TransactionWrapper {
    private final Transaction transaction;
    private final String customerName;
    private boolean isActive;

    public TransactionWrapper(Transaction transaction, String customerName, boolean isActive) {
        this.transaction = transaction;
        this.customerName = customerName;
        this.isActive = isActive;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getCustomerName() {
        return customerName;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionWrapper that = (TransactionWrapper) o;

        if (isActive != that.isActive) return false;
        if (transaction != null ? !transaction.equals(that.transaction) : that.transaction != null)
            return false;
        return customerName != null
                ? customerName.equals(that.customerName)
                : that.customerName == null;
    }

    @Override
    public int hashCode() {
        int result = transaction != null ? transaction.hashCode() : 0;
        result = 31 * result + (customerName != null ? customerName.hashCode() : 0);
        result = 31 * result + (isActive ? 1 : 0);
        return result;
    }
}
