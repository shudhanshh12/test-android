package `in`.okcredit.collection.contract

object CollectionServerErrors {
    // bank account validation errors
    class InvalidAccountNumber : Exception()

    class InvalidName : Exception()

    class InvalidIFSCcode : Exception()

    class InvalidAccountOrIFSCcode : Exception()

    // upi validation errors
    class InvalidAPaymentAddress : Exception()

    class AddressNotFound : Exception()

    class VideoNotFoundException : Exception()

    class DestinationNotSet : Exception()

    class DuplicatePayoutRequest : Exception()

    data class Error(val id: String, val name: String, val mobile: String?)
}
