package `in`.okcredit.user_migration.presentation.utils

enum class FieldType(val value: String) {
    PHONE("mobile"),
    NAME("name"),
    AMOUNT("transaction.amount"),
    DATE("transaction.date")
}
