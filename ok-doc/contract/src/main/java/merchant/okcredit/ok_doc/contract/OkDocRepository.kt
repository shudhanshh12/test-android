package merchant.okcredit.ok_doc.contract

import merchant.okcredit.ok_doc.contract.model.ImageDoc

interface OkDocRepository {
    suspend fun getImageDoc(mediaId: String, businessId: String): ImageDoc
}
