package `in`.okcredit.merchant.contract

import kotlinx.coroutines.flow.Flow

interface GetBusinessIdList {
    fun execute(): Flow<List<String>>
}
