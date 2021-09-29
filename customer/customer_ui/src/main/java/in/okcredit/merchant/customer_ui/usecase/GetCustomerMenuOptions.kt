package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.collection.contract.GetCustomerCollectionProfile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.SubscriptionFeatureEnabled
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.utils.AbFeatures
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.account_chat_contract.FEATURE
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetCustomerMenuOptions @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val showCollectWithGPay: Lazy<CanShowCollectWithGPay>,
    private val getCustomerCollectionProfile: Lazy<GetCustomerCollectionProfile>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getChatUnreadMessages: Lazy<IGetChatUnreadMessageCount>,
) {

    data class Response(
        val isEmptyTransaction: Boolean = false,
        val isCollectionActivated: Boolean = false,
        val isChatEnabled: Boolean = false,
        val isBillEnabled: Boolean = false,
        val isSubscriptionEnabled: Boolean = false,
        val isDiscountEnabled: Boolean = false,
        val isGooglePayEnabled: Boolean = false,
        val chatUnreadCountResult: Result<Pair<String, String?>>,
    )

    data class QrResponse(
        val isDiscountEnabled: Boolean = false,
        val isGooglePayEnabled: Boolean = false,
    )

    data class MenuOptionsResponse(
        val menuOptions: List<MenuOptionsBottomSheet.Companion.MenuOptions> = emptyList(),
        val toolbarOptions: List<MenuOptionsBottomSheet.Companion.MenuOptions> = emptyList(),
        val canShowContextualHelp: Boolean = false,
    )

    fun execute(customerId: String): Observable<MenuOptionsResponse> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.combineLatest(
                transactionCheckObservable(customerId, businessId),
                canShowQrCodeGpayAndDiscount(customerId),
                canShowChat(),
                canShowBill(),
                canShowSubscription(),
                canShowGpay(),
                getChatUnreadMessages.get().execute(customerId),
                { isEmptyTransaction, response, isChatEnabled, isBillEnabled, isSubscriptionEnabled, isGooglePayEnabled, countResult ->
                    Response(
                        isEmptyTransaction = isEmptyTransaction,
                        isChatEnabled = isChatEnabled,
                        isBillEnabled = isBillEnabled,
                        isSubscriptionEnabled = isSubscriptionEnabled,
                        isDiscountEnabled = response.isDiscountEnabled,
                        isGooglePayEnabled = response.isGooglePayEnabled || isGooglePayEnabled,
                        chatUnreadCountResult = countResult
                    )
                }
            ).map {
                getMenuList(it)
            }
        }
    }

    private fun transactionCheckObservable(customerId: String, businessId: String) =
        transactionRepo.get().listTransactions(customerId, businessId).map {
            it.isEmpty()
        }.distinctUntilChanged()

    private fun canShowQrCodeGpayAndDiscount(customerId: String) =
        Observable.combineLatest(
            getCustomerCollectionProfile.get().execute(customerId),
            abRepository.get().isFeatureEnabled(Features.GIVE_DISCOUNT),
            { customerCollectionProfile, isDiscountEnabled ->
                QrResponse(
                    isGooglePayEnabled = customerCollectionProfile.googlePayEnabled,
                    isDiscountEnabled = isDiscountEnabled
                )
            }
        )

    private fun canShowChat() = abRepository.get().isFeatureEnabled(FEATURE.FEATURE_ACCOUNT_CHATS)

    private fun canShowBill() = abRepository.get().isFeatureEnabled(AbFeatures.BILL_MANAGER)

    private fun canShowSubscription() =
        abRepository.get().isFeatureEnabled(SubscriptionFeatureEnabled.SUBSCRIPTION_FEATURE)

    private fun canShowGpay() = showCollectWithGPay.get().execute()

    private fun getMenuList(response: Response): MenuOptionsResponse {
        val menuOptions = mutableListOf<MenuOptionsBottomSheet.Companion.MenuOptions>()
        val toolbarOptions = mutableListOf<MenuOptionsBottomSheet.Companion.MenuOptions>()
        if (response.isEmptyTransaction) {
            menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Help)
            return MenuOptionsResponse(menuOptions, toolbarOptions, true)
        }

        toolbarOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Call)
        toolbarOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.CustomerStatements)
        toolbarOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.QrCode)
        menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Call)
        menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.CustomerStatements)
        menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.QrCode)

        if (response.isChatEnabled) {
            var unreadCount = 0
            if (response.chatUnreadCountResult is Result.Success) {
                unreadCount = response.chatUnreadCountResult.value.first.toIntOrNull() ?: 0
            }
            if (toolbarOptions.size < MAX_TOOLBAR_OPTIONS) {
                toolbarOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat(unreadCount))
            }
            menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.AccountChat(unreadCount))
        }
        if (response.isBillEnabled) {
            if (toolbarOptions.size < MAX_TOOLBAR_OPTIONS) {
                toolbarOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Bill)
            }
            menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Bill)
        }
        if (response.isSubscriptionEnabled) {
            if (toolbarOptions.size < MAX_TOOLBAR_OPTIONS) {
                toolbarOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Subscriptions)
            }
            menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Subscriptions)
        }
        if (response.isDiscountEnabled) {
            menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.GiveDiscounts)
        }
        if (response.isGooglePayEnabled) {
            menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.CollectWithGooglePay)
        }
        menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.Help)
        menuOptions.add(MenuOptionsBottomSheet.Companion.MenuOptions.DeleteRelationship)
        val difference = menuOptions.toSet()
            .minus(toolbarOptions.toSet()).size // find the size of options not present in toolbar options
        return MenuOptionsResponse(menuOptions, toolbarOptions, menuOptions.size <= 4 && difference <= 1)
    }

    companion object {
        private const val MAX_TOOLBAR_OPTIONS = 3
    }
}
