package `in`.okcredit.user_migration.presentation.utils

object UserMigrationUtils {

    sealed class ErrorType {
        object AmountError : ErrorType()
        object NameError : ErrorType()
        object PhoneError : ErrorType()
    }
}
