package `in`.okcredit.merchant.contract

object BusinessErrors {
    class InvalidName : Exception()
    class InvalidEmail : Exception()
    class InvalidAbout : Exception()
    class InvalidAddress : Exception()
    class LatLongNotFound : Throwable()
    class NoInternet : Throwable()
    class ServiceNotAvailable : Throwable()

    class NameChangeLimitExceeded : Exception()
    class EmailAlreadyExist : Exception()
    class MerchantExists : Throwable()
}
