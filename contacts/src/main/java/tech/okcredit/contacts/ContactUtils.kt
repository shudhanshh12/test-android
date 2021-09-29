package tech.okcredit.contacts

import com.google.common.base.Strings

object ContactUtils {
    const val FRC_KEY_CONTACT_PAGING_LIMIT = "contact_sync_paging_limit"
    const val FRC_KEY_CHUNK_SIZE = "contact_sync_chunk_size"
    const val FRC_KEY_PHONEBOOK_PAGINATION_MAX_REPEAT_COUNT = "phonebook_pagination_max_repeat_count"

    fun generateContactId(name: String, mobile: String): String {
        return String.format(
            "%s_%s",
            (if (Strings.isNullOrEmpty(name)) "" else name).trim { it <= ' ' }.replace(" ", "_").toLowerCase(),
            mobile
        )
    }
}
