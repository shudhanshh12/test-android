package tech.okcredit.android.base.preferences

/**
 * [Scope] denotes the scope or mapping of the shared preference key / worker unique name.
 *
 * A shared preference key-value pair can be scoped to :
 * - [Individual] : value will be same for all Businesses of the User (Individual).
 * - [Business] : value will be different for each Business of the User (Individual).
 * Note : to change the scope of an existing key, a [SharedPreferencesMigration] is required.
 *
 * A worker can be scoped to :
 * - [Individual] : no changes in unique worker name.
 * - [Business] : business id will be appended to the unique worker name.
 */
sealed class Scope {
    object Individual : Scope()
    class Business(val businessId: String) : Scope()

    companion object {
        internal const val DIVIDER = ":::::"

        internal fun getScopedKey(key: String, scope: Scope): String {
            return when (scope) {
                is Individual -> key // key (no change)
                is Business -> "${scope.businessId}$DIVIDER$key" // business-id::key (prefix businessId & divider)
            }
        }
    }
}
