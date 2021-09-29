package `in`.okcredit.referral.contract.usecase

import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

interface GetReferralLink {

    fun execute(): Single<String>

    fun executeFlow(): Flow<String>
}
