package tech.okcredit.feature_help.contract

import io.reactivex.Observable

interface GetContextualHelpIds {
    fun execute(screenName: String): Observable<List<String>>
}
