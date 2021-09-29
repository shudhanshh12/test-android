package `in`.okcredit.home

import `in`.okcredit.customer.contract.RelationshipType
import io.reactivex.Observable

interface IGetRelationsNumbersAndBalance {
    fun execute(): Observable<List<MobileNumberAndBalance>>
}

data class MobileNumberAndBalance(
    val mobile: String,
    val balance: Long,
    val relationshipId: String,
    val relationshipType: RelationshipType,
)
