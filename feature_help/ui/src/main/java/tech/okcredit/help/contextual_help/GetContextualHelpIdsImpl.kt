package tech.okcredit.help.contextual_help

import `in`.okcredit.shared.utils.ScreenName
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.userSupport.ContextualHelp
import tech.okcredit.userSupport.SupportRepository
import javax.inject.Inject

class GetContextualHelpIdsImpl @Inject constructor(
    private val supportRepository: Lazy<SupportRepository>,
) : GetContextualHelpIds {

    override fun execute(screenName: String): Observable<List<String>> {
        var displayType: List<String> = emptyList()
        when (screenName) {
            ScreenName.CustomerScreen.value ->
                displayType = ContextualHelp.CUSTOMER.value

            ScreenName.TxnDetailsScreen.value ->
                displayType = ContextualHelp.TRANSACTION.value

            ScreenName.RewardsScreen.value ->
                displayType = ContextualHelp.REWARD.value

            ScreenName.AccountScreen.value ->
                displayType = ContextualHelp.ACCOUNT.value

            ScreenName.MerchantScreen.value ->
                displayType = ContextualHelp.MERCHANT.value

            ScreenName.SecurityScreen.value ->
                displayType = ContextualHelp.SECURITY.value

            ScreenName.Collection.value ->
                displayType = ContextualHelp.COLLECTION.value

            ScreenName.CustomerProfile.value ->
                displayType = ContextualHelp.CUSTOMER_PROFILE.value

            ScreenName.SupplierScreen.value ->
                displayType = ContextualHelp.SUPPLIER.value

            ScreenName.SupplierProfile.value ->
                displayType = ContextualHelp.SUPPLIER_PROFILE.value
            ScreenName.SupplierTxnDetailsScreen.value ->
                displayType = ContextualHelp.SUPPLIER_TRANSACTION.value
            ScreenName.LanguageScreen.value ->
                displayType = ContextualHelp.LANGUAGE.value
            ScreenName.ShareOkCreditScreen.value ->
                displayType = ContextualHelp.SHARE_OKC.value
            ScreenName.CollectionTargetedReferralScreen.value ->
                displayType = ContextualHelp.COLLECTION_TARGETED_REFERRAL.value
        }

        return supportRepository.get().getContextualHelpIds(displayType)
    }
}
