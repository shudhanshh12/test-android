package `in`.okcredit.merchant.collection.analytics

/**
 * Names for custom traces and custom metrics must meet the following requirements:
 * 1. no leading or trailing whitespace
 * 2. no leading underscore
 * 3. max length is 32 characters
 **/

object CollectionTraces {
    const val RENDER_ADD_BANK_ACCOUNT = "RenderAddBankAccount"
    const val RENDER_COLLECTION_ADOPTION_V2 = "RenderCollectionAdoptionV2"
    const val RENDER_ADD_UPI = "RenderUpi"
    const val RENDER_CONFIRM_BANK_ACCOUNT = "RenderConfirmBankAccount"
    const val RENDER_FEE_CALCULATION = "RenderCollectionInfoScreen"
    const val RENDER_COLLECTION_ADOPTION_POPUP = "RenderCollectionAdoptionPopup"
    const val RENDER_QR_SCANNER_SCREEN = "RenderQRScannerScreen"

    // LiveSales
    const val RENDER_LIVE_SALES = "RenderLiveSales"
}
