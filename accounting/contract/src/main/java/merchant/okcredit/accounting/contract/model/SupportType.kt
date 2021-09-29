package merchant.okcredit.accounting.contract.model

enum class SupportType(val value: String) {
    CALL("call"),
    CHAT("chat"),
    NONE("");

    companion object {
        val map = values().associateBy(SupportType::value)

        fun fromValue(value: String) = map[value] ?: SupportType.NONE
    }
}

const val FRC_PAYMENT_24X7 = "payment_24x7"
const val FRC_HElP_NUMBER_PAYMENT = "help_number_payment"
const val FRC_HElP_CHAT_NUMBER_PAYMENT = "help_chat_number_payment"
