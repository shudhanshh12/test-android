package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.internal.VoiceInputResponseBody
import `in`.okcredit.backend._offline.usecase.VoiceInputSyncer
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import retrofit2.Response
import javax.inject.Inject

class PostVoiceInput @Inject constructor(private val voiceInputSyncer: Lazy<VoiceInputSyncer>) :
    UseCase<PostVoiceInput.Request, Response<VoiceInputResponseBody>> {
    override fun execute(req: Request): Observable<Result<Response<VoiceInputResponseBody>>> {
        return UseCase.wrapSingle(voiceInputSyncer.get().execute(req.text))
    }

    data class Request(val text: String)
}
