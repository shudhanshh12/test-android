package merchant.okcredit.ok_doc.server

import merchant.okcredit.ok_doc.contract.model.ImageDoc

interface OkDocRemoteSource {
    suspend fun getImageDoc(mediaId: String, businessId: String): ImageDoc
}
