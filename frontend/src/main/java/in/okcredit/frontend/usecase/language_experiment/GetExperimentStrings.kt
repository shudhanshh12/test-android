package `in`.okcredit.frontend.usecase.language_experiment

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.jcminarro.philology.Philology
import com.jcminarro.philology.PhilologyInterceptor
import com.jcminarro.philology.PhilologyRepository
import com.jcminarro.philology.PhilologyRepositoryFactory
import dagger.Lazy
import io.github.inflationx.viewpump.ViewPump
import io.reactivex.Completable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.ab.sdk.ABExperiments
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class GetExperimentStrings @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val localeManager: Lazy<LocaleManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapCompletable { businessId ->
                ab.get().getProfile(businessId)
                    .map {
                        val merchantLanguage = localeManager.get().getLanguage()
                        val strings = mutableMapOf<String, String>()
                        for ((key, value) in it.experiments) {
                            if (key.contains(ABExperiments.LANGUAGE)) {
                                val experimentLanguage = key.split("-")
                                try {
                                    if (merchantLanguage == experimentLanguage[1]) {
                                        strings.putAll(value.vars)
                                    }
                                } catch (e: Exception) {
                                    RecordException.recordException(e)
                                }
                            }
                        }
                        strings
                    }
                    .flatMapCompletable {
                        setupPhilology(it, ab.get())
                    }
            }
    }
}

internal fun setupPhilology(strings: Map<String, String>, ab: AbRepository): Completable {
    Timber.i("setupPhilology APP start")

    return Completable.fromAction {
        Philology.init(MyPhilologyRepositoryFactory(strings, ab))
        ViewPump.init(ViewPump.builder().addInterceptor(PhilologyInterceptor).build())
    }
}

class MyPhilologyRepositoryFactory(
    private val abStrings: Map<String, String>,
    private val ab: AbRepository,
) : PhilologyRepositoryFactory {
    override fun getPhilologyRepository(locale: Locale): PhilologyRepository? {
        return OKCPhilologyRepository(abStrings, ab)
    }
}

class OKCPhilologyRepository(
    private val abStrings: Map<String, String>,
    private val ab: AbRepository,
) : PhilologyRepository {
    override fun getPlural(s: String, s1: String): CharSequence? {
        return null
    }

    override fun getText(key: String): CharSequence? {
        return if (abStrings.isEmpty()) {
            null
        } else {
            if (abStrings[key] != null) {
                ab.startLanguageExperiment(key)
                abStrings[key]?.replace("\\n", "\n")
            } else {
                null
            }
        }
    }

    override fun getTextArray(s: String): Array<CharSequence>? {
        return null
    }
}
