package tech.okcredit.contacts.server

import tech.okcredit.contacts.contract.model.Contact

fun Contact.toApiModel(): tech.okcredit.contacts.server.Contact {
    return Contact(
        phonebookId,
        name,
        mobile,
        picUri,
        found,
        timestamp,
        type
    )
}
