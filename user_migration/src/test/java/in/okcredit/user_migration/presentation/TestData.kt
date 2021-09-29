package `in`.okcredit.user_migration.presentation

import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.ParsedDataModels

val listOfFilePaths = listOf("xyz.pdf", "this.pdf")

val testModels =
    listOf<ParsedDataModels>(
        ParsedDataModels.FileModel("Khatabook.customer.pdf"),
        ParsedDataModels.CustomerModel(
            CustomerUiTemplate(
                index = 1,
                isCheckedBoxChecked = true,
                customerId = "1234",
                phone = "7728398939",
                name = "James Bond",
                amount = 2500,
                type = 2,
                error = false,
            )
        ),
        ParsedDataModels.CustomerModel(
            CustomerUiTemplate(
                index = 2,
                isCheckedBoxChecked = true,
                customerId = "1234",
                phone = "0070070707",
                name = "James jr. Bond",
                amount = 2500,
                type = 2,
                error = false,
            )
        )
    )

val fakeCustomerUiTemplate = CustomerUiTemplate(
    isCheckedBoxChecked = true,
    customerId = "1234",
    phone = "7728398939",
    name = "James Bond",
    amount = 2500,
    type = 2,
    error = false,
)
