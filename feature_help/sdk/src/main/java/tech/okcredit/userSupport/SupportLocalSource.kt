package tech.okcredit.userSupport

import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.userSupport.model.Help

interface SupportLocalSource {

    fun setLanguage(identity: String): Completable

    fun getLanguage(): Observable<String>

    fun getHelp(): Observable<List<Help>>

    fun setHelp(helpItems: List<Help>): Completable
}
