package `in`.okcredit.onboarding.language.usecase

import `in`.okcredit.backend.contract.ServerConfigManager
import android.content.Context
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

class SelectLanguage @Inject constructor(
    private val context: Lazy<Context>,
    private val serverConfigManager: Lazy<ServerConfigManager>
) {

    fun execute(language: String): Completable {
        return Completable.fromRunnable {
            LocaleManager.setNewLocale(context.get(), language)
            serverConfigManager.get().schedule()
        }
    }
}
