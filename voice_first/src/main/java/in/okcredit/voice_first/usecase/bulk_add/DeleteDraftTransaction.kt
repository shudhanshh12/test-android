package `in`.okcredit.voice_first.usecase.bulk_add

import `in`.okcredit.voice_first.data.bulk_add.BulkAddTransactionsRepository
import dagger.Lazy
import javax.inject.Inject

class DeleteDraftTransaction @Inject constructor(
    private val repository: Lazy<BulkAddTransactionsRepository>,
) {

    suspend fun execute(draftId: String) = repository.get().discard(draftId)
}
