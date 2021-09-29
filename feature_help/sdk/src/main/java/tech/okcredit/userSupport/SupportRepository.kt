package tech.okcredit.userSupport

import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.userSupport.model.Help
import tech.okcredit.userSupport.model.HelpItem

interface SupportRepository {
    fun getHelp(): Observable<List<Help>>

    fun getHelpItem(helpId: String): Observable<HelpItem>

    fun getContextualHelp(displayType: String): Observable<Help>

    fun getContextualHelpIds(displayTypes: List<String>): Observable<List<String>>

    fun executeSyncEverything(language: String, businessId: String): Completable

    fun scheduleSyncEverything(language: String, businessId: String): Completable
}
