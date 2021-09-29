package in.okcredit.backend._offline.error;

import in.okcredit.backend.contract.Customer;
import in.okcredit.merchant.suppliercredit.Supplier;

public final class CustomerErrors {
    public static class InvalidName extends Exception {}

    public static class InvalidMobile extends Exception {}

    public static class ActiveCyclicAccount extends Exception {
        private Supplier conflict;

        public ActiveCyclicAccount(Supplier conflict) {
            this.conflict = conflict;
        }

        public Supplier getConflict() {
            return conflict;
        }
    }

    public static class DeletedCyclicAccount extends Exception {
        private Supplier conflict;

        public DeletedCyclicAccount(Supplier conflict) {
            this.conflict = conflict;
        }

        public Supplier getConflict() {
            return conflict;
        }
    }

    public static class DeletedCustomer extends Exception {
        private Customer conflict;

        public DeletedCustomer(Customer conflict) {
            this.conflict = conflict;
        }

        public Customer getConflict() {
            return conflict;
        }
    }

    public static class MobileConflict extends Exception {
        private Customer conflict;

        public MobileConflict(Customer conflict) {
            this.conflict = conflict;
        }

        public Customer getConflict() {
            return conflict;
        }
    }

    public static class MobileUpdateAccessDenied extends Exception {}

    public static class DeletePermissionDenied extends Exception {
        private String errorMessage;

        public DeletePermissionDenied(String message) {
            errorMessage = message;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private CustomerErrors() {}
}
