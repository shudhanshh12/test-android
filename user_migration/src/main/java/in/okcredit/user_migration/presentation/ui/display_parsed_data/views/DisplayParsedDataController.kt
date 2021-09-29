package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views

import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.ParsedDataModels
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views.uploadFileShimmerLoader
import com.airbnb.epoxy.TypedEpoxyController
import dagger.Lazy
import javax.inject.Inject

class DisplayParsedDataController @Inject constructor(
    private val itemViewFileListener: Lazy<ItemViewFileName.ItemViewFileListener>,
    private val itemViewCustomerListener: Lazy<ItemViewCustomerList.ItemViewCustomerListener>
) : TypedEpoxyController<List<ParsedDataModels>>() {

    override fun buildModels(data: List<ParsedDataModels>?) {
        if (data.isNullOrEmpty()) {
            uploadFileShimmerLoader {
                id("loader")
            }
            return
        }

        data.forEach { model ->
            when (model) {
                is ParsedDataModels.CustomerModel -> {
                    itemViewCustomerList {
                        id("customerList${model.customer.customerId}")
                        listener(itemViewCustomerListener.get())
                        customerAndTransaction(model.customer)
                    }
                }
                is ParsedDataModels.FileModel -> {
                    itemViewFileName {
                        id("fileName${model.fileName}")
                        listener(itemViewFileListener.get())
                        fileName(model.fileName)
                    }
                }
                is ParsedDataModels.ParserErrorModel -> {
                    itemFileParsingError {
                        id("ParsedError")
                    }
                }
            }
        }
    }
}
