package tech.okcredit.account_chat_contract

import `in`.okcredit.shared.usecase.Result
import io.reactivex.Observable

interface IGetChatUnreadMessageCount {
    fun execute(accountId: String): Observable<Result<Pair<String, String?>>>
    fun getUnreadCountForRelation(relation: String): Observable<HashMap<String, Long>>
}
