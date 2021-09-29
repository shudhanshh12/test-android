package `in`.okcredit.voice_first.usecase.bulk_add

import `in`.okcredit.voice_first.contract.ResetDraftTransactions
import `in`.okcredit.voice_first.data.bulk_add.BulkAddTransactionsRepository
import dagger.Lazy
import javax.inject.Inject

class ClearDraftTransactions @Inject constructor(
    private val repository: Lazy<BulkAddTransactionsRepository>,
) : ResetDraftTransactions {

    override suspend fun execute() = repository.get().discardAll()
}
