package tech.okcredit.help.help_main.usecase

import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.userSupport.SupportRepository
import javax.inject.Inject

class GetHelp @Inject constructor(private val userSupport: Lazy<SupportRepository>) {
    fun scheduleSyncEverything(language: String, businessId: String): Completable {
        return userSupport.get().scheduleSyncEverything(language, businessId)
    }
}
