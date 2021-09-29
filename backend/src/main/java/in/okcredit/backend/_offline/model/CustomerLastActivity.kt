package `in`.okcredit.backend._offline.model

import `in`.okcredit.backend.R

object CustomerLastActivity {

    private val lookupMapByCode = mapOf(
        0 to R.string.credit_deleted_new,
        1 to R.string.payment_deleted_new,
        2 to R.string.credit_added_new,
        3 to R.string.payment_added_new,
        5 to R.string.processing_amount,
        6 to R.string.discount_deleted_new,
        7 to R.string.discount_offered_new,
        8 to R.string.credit_edited_new,
        9 to R.string.payment_edited_new
    )

    private val customerSubtitleAbLookupMapByCode = mapOf(
        0 to R.plurals.credit_deleted_on_date,
        1 to R.plurals.payment_deleted_on_date,
        2 to R.plurals.credit_added_on_date,
        3 to R.plurals.payment_added_on_date,
        5 to R.plurals.processing_amount_new,
        6 to R.plurals.discount_deleted_on_date,
        7 to R.plurals.discount_offered_on_date,
        8 to R.plurals.credit_edited_on_date,
        9 to R.plurals.payment_edited_on_date
    )

    fun getActivityFromCode(code: Int?): Int =
        lookupMapByCode[code] ?: throw IllegalArgumentException("Unknown code: $code")

    fun getActivityFromCodeWithCustomerSubtitleAb(code: Int?): Int =
        customerSubtitleAbLookupMapByCode[code] ?: throw IllegalArgumentException("Unknown code: $code")
}
