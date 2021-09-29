package in.okcredit.supplier.usecase;

import in.okcredit.merchant.suppliercredit.Transaction;

public class SupplierTransactionWrapper {
    private final Transaction transaction;
    private final String supplierName;
    private boolean isActive;

    public SupplierTransactionWrapper(
            Transaction transaction, String supplierName, boolean isActive) {
        this.transaction = transaction;
        this.supplierName = supplierName;
        this.isActive = isActive;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupplierTransactionWrapper that = (SupplierTransactionWrapper) o;

        if (isActive != that.isActive()) return false;
        if (transaction != null
                ? !transaction.equals(that.getTransaction())
                : that.getTransaction() != null) return false;
        return supplierName != null
                ? supplierName.equals(that.getSupplierName())
                : that.getSupplierName() == null;
    }

    @Override
    public int hashCode() {
        int result = transaction != null ? transaction.hashCode() : 0;
        result = 31 * result + (supplierName != null ? supplierName.hashCode() : 0);
        result = 31 * result + (isActive ? 1 : 0);
        return result;
    }
}
