package `in`.okcredit.expense.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

class Models {

    data class ExpenseListResponse(
        @SerializedName("expenses")
        var expenseList: List<Expense>,
        @SerializedName("total_amount")
        var totalAmount: Double,
        @SerializedName("start_date")
        var startDate: DateTime? = null,
        @SerializedName("end_date")
        var endDate: DateTime? = null
    )

    data class Expense(
        @SerializedName("amount")
        var amount: Double,
        @SerializedName("created_at")
        var createdAt: DateTime,
        @SerializedName("deleted_at")
        var deletedAt: DateTime? = null,
        @SerializedName("id")
        var id: String,
        @SerializedName("expense_type")
        var type: String,
        @SerializedName("updated_at")
        var updatedAt: DateTime? = null,
        @SerializedName("user_id")
        var userId: String,
        @SerializedName("expense_date")
        var expenseDate: DateTime
    )

    data class UserExpenseTypes(
        @SerializedName("expense_types")
        var expenseTypes: List<String>
    )

    data class ExpenseRequestModel(
        @SerializedName("request_id")
        private var requestId: String? = null,
        @SerializedName("expense")
        private var expense: AddedExpense
    )

    data class AddedExpense(
        @SerializedName("user_id")
        private var userId: String,
        @SerializedName("amount")
        private var amount: Double,
        @SerializedName("expense_type")
        private var expenseType: String,
        @SerializedName("expense_date")
        private var expenseDate: DateTime
    )

    data class AddExpenseResponse(
        @SerializedName("id")
        private var id: String,
        @SerializedName("request_id")
        private var requestId: String? = null,
        @SerializedName("amount")
        private var amount: Int,
        @SerializedName("expense_type")
        private var expenseType: String? = null,
        @SerializedName("created_at")
        private var createdAt: Int
    )
}
