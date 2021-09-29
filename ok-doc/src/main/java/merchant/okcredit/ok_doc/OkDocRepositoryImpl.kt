package merchant.okcredit.ok_doc

import dagger.Lazy
import merchant.okcredit.ok_doc.contract.OkDocRepository
import merchant.okcredit.ok_doc.contract.model.ImageDoc
import merchant.okcredit.ok_doc.server.OkDocRemoteSource
import javax.inject.Inject

class OkDocRepositoryImpl @Inject constructor(private val okDocRemoteSource: Lazy<OkDocRemoteSource>) :
    OkDocRepository {
    override suspend fun getImageDoc(mediaId: String, businessId: String): ImageDoc = okDocRemoteSource.get().getImageDoc(mediaId, businessId)
}
