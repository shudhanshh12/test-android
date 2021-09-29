package `in`.okcredit.user_migration.presentation.data

interface UserMigrationApiMessages {

    enum class TRANSACTION(val type: Int) {
        CREDIT(1),
        PAYMENT(2)
    }
}
