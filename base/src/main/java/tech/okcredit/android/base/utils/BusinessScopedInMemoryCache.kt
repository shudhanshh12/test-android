package tech.okcredit.android.base.utils

import io.reactivex.subjects.BehaviorSubject

/**
 * [BusinessScopedInMemoryCache] is a wrapper class over [BehaviorSubject] to implement in-memory cache along with
 * support for multiple accounts. Data cached belongs to a particular business id denoted by [forBusinessId] field.
 */
class BusinessScopedInMemoryCache<T : Any>(val cachedData: BehaviorSubject<T>, internal val forBusinessId: String) {
    fun isValidForBusinessId(businessId: String): Boolean {
        return forBusinessId == businessId
    }
}
