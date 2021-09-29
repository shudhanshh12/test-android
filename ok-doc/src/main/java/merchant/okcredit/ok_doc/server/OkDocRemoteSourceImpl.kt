package merchant.okcredit.ok_doc.server

import dagger.Lazy
import merchant.okcredit.ok_doc.contract.model.ImageDoc
import tech.okcredit.base.network.asError
import javax.inject.Inject

class OkDocRemoteSourceImpl @Inject constructor(private val okDocService: Lazy<OkDocService>) : OkDocRemoteSource {

    override suspend fun getImageDoc(mediaId: String, businessId: String): ImageDoc {
        val response = okDocService.get().getImageUrl(mediaId, businessId)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw response.asError()
        }
    }
}
