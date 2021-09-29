package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.ParsedDataModels
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.usecase.CheckValidation
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.user_migration.contract.UserMigrationRepository
import java.io.File
import javax.inject.Inject

class GetControllerDataModels @Inject constructor(
    private val userMigrationRepository: Lazy<UserMigrationRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(remoteUrls: List<String>, localUrls: List<String>): Observable<Result<Response>> {

        val renderTheseModels = mutableListOf<ParsedDataModels>()

        var customerCount = 0
        var modelIndex = 0
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                userMigrationRepository.get().getParseFileData(remoteUrls, businessId).map { parsedFilesContent ->
                    parsedFilesContent.forEachIndexed { index, fileContent ->
                        customerCount += fileContent.data.size
                        val file = File(localUrls[index])
                        renderTheseModels.add(ParsedDataModels.FileModel(fileName = file.name))
                        ++modelIndex

                        if (fileContent.data.isNullOrEmpty()) {
                            renderTheseModels.add(ParsedDataModels.ParserErrorModel)
                            ++modelIndex
                        } else {
                            fileContent.data.forEach {
                                val errorPresent = isAnyValidationError(
                                    it.name ?: "",
                                    (it.transaction?.amount ?: 0).toString(),
                                    it.mobile ?: ""
                                )
                                renderTheseModels.add(
                                    ParsedDataModels.CustomerModel(
                                        customer = CustomerUiTemplate(
                                            index = modelIndex,
                                            isCheckedBoxChecked = !errorPresent,
                                            customerId = it.customer_object_id,
                                            name = it.name,
                                            phone = it.mobile,
                                            amount = it.transaction?.amount ?: 0, // take zero if server break
                                            type = it.transaction?.type ?: 1, // take Credit if server break
                                            error = errorPresent
                                        )
                                    )
                                )
                                ++modelIndex
                            }
                        }
                    }
                    Response(
                        models = renderTheseModels,
                        totalCustomerCount = customerCount
                    )
                }
            }
        )
    }

    data class Response(
        val models: List<ParsedDataModels>,
        val totalCustomerCount: Int
    )

    private fun isAnyValidationError(customerName: String, amount: String, phone: String) =
        CheckValidation.execute(customerName, amount, phone).isNotEmpty()
}
