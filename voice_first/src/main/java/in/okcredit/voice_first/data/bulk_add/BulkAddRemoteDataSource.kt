package `in`.okcredit.voice_first.data.bulk_add

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.data.bulk_add.entities.ParseTransactionRequest
import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class BulkAddRemoteDataSource @Inject constructor(
    private val service: Lazy<BulkAddApiService>,
) {

    suspend fun parseTextToDraft(
        businessId: String,
        draft: DraftTransaction
    ) = service.get().parseTransaction(
        businessId,
        ParseTransactionRequest(
            draft_transaction_id = draft.draftTransactionId,
            voice_transcript = draft.voiceTranscript
        )
    )
}
