package tech.okcredit.userSupport.store

import androidx.annotation.Keep
import com.f2prateek.rx.preferences2.Preference
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.offloaded
import tech.okcredit.userSupport.SupportLocalSource
import tech.okcredit.userSupport.model.Help
import javax.inject.Inject

class SupportLocalSourceImpl @Inject constructor(
    private val rxPref: Lazy<SupportPreferences>,
) : SupportLocalSource {

    companion object {
        internal const val KEY_LANGUAGE = "language"
        internal const val KEY_HELP = "help"
    }

    @Keep
    data class HelpResponse(
        val helpItems: List<Help>,
    )

    private val helpCodec by lazy {
        object : Preference.Converter<HelpResponse> {
            override fun deserialize(serialized: String): HelpResponse =
                GsonUtils.gson().fromJson(serialized, HelpResponse::class.java)

            override fun serialize(value: HelpResponse): String =
                GsonUtils.gson().toJson(value)
        }
    }

    override fun setLanguage(identity: String): Completable {
        return rxCompletable {
            rxPref.get().set(KEY_LANGUAGE, identity, Scope.Individual)
        }.subscribeOn(ThreadUtils.io())
    }

    override fun getLanguage(): Observable<String> =
        rxPref.offloaded().flatMapObservable {
            it.getString(KEY_LANGUAGE, Scope.Individual).asObservable()
        }.subscribeOn(ThreadUtils.io())

    override fun getHelp(): Observable<List<Help>> = rxPref.offloaded().flatMapObservable {
        it.getObject(KEY_HELP, Scope.Individual, HelpResponse(arrayListOf()), helpCodec)
            .asObservable()
            .map { it.helpItems }
    }.subscribeOn(ThreadUtils.io())

    override fun setHelp(helpItems: List<Help>): Completable {
        return rxCompletable {
            rxPref.get().set(KEY_HELP, HelpResponse(helpItems), Scope.Individual, helpCodec)
        }.subscribeOn(ThreadUtils.io())
    }
}
