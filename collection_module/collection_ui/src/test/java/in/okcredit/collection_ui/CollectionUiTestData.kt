package `in`.okcredit.collection_ui

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import org.joda.time.DateTime

object CollectionUiTestData {
    val COLLECTION_ONLINE_ITEM = CollectionOnlinePayment(
        "id_1", DateTime(), DateTime(),
        5, "", "account_id", 1.0, "", "",
        "", "",
        "", true, "", ""
    )

    val COLLECTION_ONLINE_ITEM_2 = CollectionOnlinePayment(
        "id_2", DateTime(), DateTime(),
        1, "", "account_id_2", 1.0, "", "",
        "", "",
        "", true, "", ""
    )
}
