package tech.okcredit.help

import tech.okcredit.userSupport.model.Help
import tech.okcredit.userSupport.model.HelpInstruction
import tech.okcredit.userSupport.model.HelpItem

object TestData {
    val helpList = listOf(
        Help(
            "transaction", "https://d2vo9sg0p6n7i7.cloudfront.net/icons/add_customer.webp_Help",
            "Add a new customer_Help", "transaction_help",
            listOf(
                HelpItem(
                    "add_customer_HelpItem",
                    "Add a new customer_HelpItem",
                    "Create a new customer account in your OkCredit_HelpItem",
                    "video_HelpItem",
                    "He7kETWJ7CQ_HelpItem",
                    listOf(
                        HelpInstruction(
                            "add_customer_HelpInstruction",
                            "https://d2vo9sg0p6n7i7.cloudfront.net/images/addCustomer-en-1.webp_HelpInstruction",
                            "Add a new customer_HelpInstruction",
                            "video_HelpInstruction"
                        )
                    )

                )
            )
        )
    )
}
