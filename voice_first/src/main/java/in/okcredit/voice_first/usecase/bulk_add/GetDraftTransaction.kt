package `in`.okcredit.voice_first.usecase.bulk_add

import `in`.okcredit.voice_first.data.bulk_add.BulkAddTransactionsRepository
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import dagger.Lazy
import javax.inject.Inject

class GetDraftTransaction @Inject constructor(
    private val repository: Lazy<BulkAddTransactionsRepository>,
) {

    suspend fun execute(draftId: String): DraftTransaction? = repository.get().get(draftId)
}
