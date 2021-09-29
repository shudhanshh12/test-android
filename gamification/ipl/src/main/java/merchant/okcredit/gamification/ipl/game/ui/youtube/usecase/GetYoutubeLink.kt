package merchant.okcredit.gamification.ipl.game.ui.youtube.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.gamification.ipl.game.data.server.model.response.YoutubeLinks
import tech.okcredit.android.base.language.LocaleManager
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class GetYoutubeLink @Inject constructor(private val localeManager: Lazy<LocaleManager>) {

    fun execute(links: List<YoutubeLinks>): Observable<Result<String>> {
        return UseCase.wrapSingle(

            Single.create {
                val pattern = "(?<=youtu.be/|watch\\?v=|/channel/|/videos/|embed/)[^#&?]*"
                val compiledPattern: Pattern = Pattern.compile(pattern)
                val matcher: Matcher = compiledPattern.matcher(getLinkByLanguage(links))
                if (matcher.find()) {
                    it.onSuccess(matcher.group())
                } else {
                    it.onError(Throwable("no valid youtube link"))
                }
            }
        )
    }

    private fun getLinkByLanguage(links: List<YoutubeLinks>): String {
        var url = ""
        var link = links.filter { it.language == localeManager.get().getLanguage() }
        if (link.isNullOrEmpty()) {
            link = links.filter { it.language == LocaleManager.LANGUAGE_ENGLISH }
        }
        url = link[0].link
        return url
    }
}
