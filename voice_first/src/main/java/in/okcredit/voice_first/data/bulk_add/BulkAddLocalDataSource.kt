package `in`.okcredit.voice_first.data.bulk_add

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import tech.okcredit.android.base.di.AppScope
import javax.inject.Inject

@AppScope
class BulkAddLocalDataSource @Inject constructor() {

    private val localDrafts = LinkedHashMap<String, DraftTransaction>() // Linked to preserve insertion order
    private val publisher = MutableStateFlow(localDrafts.map { it.value })

    suspend fun add(draft: DraftTransaction) {
        localDrafts[draft.draftTransactionId] = draft
        updatePublisher()
    }

    suspend fun put(draft: DraftTransaction) {
        localDrafts[draft.draftTransactionId] = draft
        updatePublisher()
    }

    suspend fun get(draftId: String): DraftTransaction? {
        return localDrafts[draftId]
    }

    fun get(): Flow<List<DraftTransaction>> {
        return publisher
    }

    suspend fun discard(draftId: String) {
        localDrafts.remove(draftId)
        updatePublisher()
    }

    suspend fun discardAll() {
        localDrafts.clear()
        updatePublisher()
    }

    private suspend fun updatePublisher() {
        publisher.emit(localDrafts.map { it.value }) // Push a copy
    }
}
