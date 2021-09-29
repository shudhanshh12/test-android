package `in`.okcredit.voice_first.data.bulk_add

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import java.util.*
import java.util.concurrent.CancellationException
import javax.inject.Inject

@Reusable
class BulkAddTransactionsRepository @Inject constructor(
    private val localDataSource: BulkAddLocalDataSource,
    private val remoteDataSource: BulkAddRemoteDataSource,
) {

    suspend fun add(voiceTranscript: String): DraftTransaction {
        return generateSequence { UUID.randomUUID().toString() }
            .first { this.get(it) == null }
            .let { DraftTransaction(it, voiceTranscript) }
            .also { localDataSource.add(it) }
    }

    suspend fun put(draft: DraftTransaction) {
        localDataSource.put(draft)
    }

    suspend fun get(draftId: String): DraftTransaction? {
        return localDataSource.get(draftId)
    }

    fun get(): Flow<List<DraftTransaction>> {
        return localDataSource.get()
    }

    suspend fun discard(draftId: String) {
        localDataSource.discard(draftId)
    }

    suspend fun discardAll() {
        localDataSource.discardAll()
    }

    suspend fun parseVoiceTranscript(
        businessId: String,
        draft: DraftTransaction
    ): DraftTransaction {
        val response = runCatching { remoteDataSource.parseTextToDraft(businessId, draft) }
        response.exceptionOrNull()?.takeIf { it is CancellationException }?.also { throw it }

        return response.getOrNull()
            ?.draft?.copy(isParsed = true)
            ?.also { this.put(it) }
            ?: draft
    }
}
