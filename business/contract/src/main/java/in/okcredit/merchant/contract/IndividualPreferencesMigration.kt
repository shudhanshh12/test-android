package `in`.okcredit.merchant.contract

import tech.okcredit.android.base.preferences.SharedPreferencesMigration

/**
 * Migrate deprecated merchant preferences from merchant database to individual shared preferences
 */
interface IndividualPreferencesMigration {
    fun migration0To1(): SharedPreferencesMigration
}
