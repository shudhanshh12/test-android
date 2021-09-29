package `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.usecase

import `in`.okcredit.user_migration.presentation.utils.UserMigrationUtils.ErrorType
import tech.okcredit.android.base.mobile.isValidMobile as checkValidMobile

object CheckValidation {
    fun execute(
        customerName: String,
        amount: String,
        mobile: String
    ): List<ErrorType> {
        val listOfErrorType = mutableListOf<ErrorType>()
        if (!isValidAmount(amount)) {
            listOfErrorType.add(ErrorType.AmountError)
        }
        if (!isValidName(customerName)) {
            listOfErrorType.add(ErrorType.NameError)
        }
        if (!isValidMobile(mobile)) {
            listOfErrorType.add(ErrorType.PhoneError)
        }
        return listOfErrorType
    }

    private fun isValidMobile(mobile: String) = checkValidMobile(mobile) || mobile.isEmpty()

    private fun isValidAmount(amount: String): Boolean {
        return try {
            amount.toFloat() > 0
        } catch (exception: Exception) {
            false
        }
    }

    private fun isValidName(name: String) = name.isNotEmpty()
}
