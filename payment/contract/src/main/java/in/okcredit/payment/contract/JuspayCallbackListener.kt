package `in`.okcredit.payment.contract

interface JuspayCallbackListener {
    fun onJuspaySdkOpened()
    fun onJuspaySdkBackpressed()
    fun onApiFailure(errorType: ApiErrorType)
    fun onJuspaySdkClosed(
        paymentId: String,
        paymentType: String,
        amount: Long,
        errorType: JuspayErrorType = JuspayErrorType.NONE
    )
}

enum class JuspayErrorType(val value: String) {
    JP_001("JP_001"), // payment mode having some issue mostly integration h
    JP_002("JP_002"), // Txn cancelled by user
    JP_005("JP_005"), // network error
    NONE("NONE");

    companion object {
        val map = values().associateBy(JuspayErrorType::value)
        fun fromValue(value: String) = map[value] ?: NONE
    }
}
