package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase

import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.ParsedDataModels
import io.reactivex.Observable
import javax.inject.Inject

class UpdateParsedDataModels @Inject constructor() {
    fun execute(
        updateEntries: CustomerUiTemplate,
        oldParsedDataModels: List<ParsedDataModels>
    ): Observable<List<ParsedDataModels>> {
        return Observable.just(
            oldParsedDataModels
                .toMutableList()
                .apply {
                    getOrNull(updateEntries.index)
                        ?.let { it as? ParsedDataModels.CustomerModel }
                        ?.takeIf { it.customer.customerId == updateEntries.customerId }
                        ?.also {
                            set(
                                updateEntries.index,
                                ParsedDataModels.CustomerModel(
                                    customer = CustomerUiTemplate(
                                        index = updateEntries.index,
                                        isCheckedBoxChecked = updateEntries.isCheckedBoxChecked,
                                        customerId = updateEntries.customerId,
                                        name = updateEntries.name,
                                        phone = updateEntries.phone,
                                        amount = updateEntries.amount,
                                        type = updateEntries.type,
                                        dueDate = updateEntries.dueDate,
                                        error = updateEntries.error
                                    )
                                )
                            )
                        }
                }
        )
    }
}
