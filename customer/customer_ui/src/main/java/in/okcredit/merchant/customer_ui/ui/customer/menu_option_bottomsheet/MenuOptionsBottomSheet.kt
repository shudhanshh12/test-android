package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.MenuBottomSheetBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.analytics.MenuOptionEventTracker
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class MenuOptionsBottomSheet :
    BaseBottomSheetWithViewEvents<MenuOptionBottomSheetContract.State, MenuOptionBottomSheetContract.ViewEvent, MenuOptionBottomSheetContract.Intent>(
        "MenuOptionsBottomSheet"
    ) {

    private lateinit var menuBinding: MenuBottomSheetBinding
    internal var menuListener: MenuListener? = null
    private var customer: Customer? = null

    private lateinit var menuOptionsController: MenuOptionsController

    @Inject
    lateinit var menuOptionEventTracker: Lazy<MenuOptionEventTracker>

    fun initialise(menuListener: MenuListener) {
        this.menuListener = menuListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        menuBinding = MenuBottomSheetBinding.inflate(inflater, container, false)
        return menuBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customer = arguments?.getParcelable(CUSTOMER_NAME) as Customer?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    private fun initList() {
        menuOptionsController = MenuOptionsController()
        menuOptionsController.setListener(object : MenuOptionItemView.Listener {
            override fun onMenuItemClicked(menuOption: MenuOptions) {
                if (menuOption is MenuOptions.More) {
                    pushIntent(MenuOptionBottomSheetContract.Intent.MoreOptionClicked)
                } else {
                    menuListener?.onMenuItemClicked(menuOption)
                    dismiss()
                }
            }
        })
        menuBinding.menuList.adapter = menuOptionsController.adapter
        menuBinding.menuList.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * MenuListener Interface to update reminder mode and it's extend
     * send payment reminder
     */
    interface MenuListener {
        fun onMenuItemClicked(menu: MenuOptions)
    }

    companion object {
        const val UNREAD_CHAT_ACCOUNT = "UNREAD_CHAT_ACCOUNT"
        const val CUSTOMER_NAME = "customer"
        val TAG: String = MenuOptionsBottomSheet::class.java.simpleName

        const val MENU_PARCEL = "menu_parcel"

        fun newInstance(): MenuOptionsBottomSheet {
            return MenuOptionsBottomSheet()
        }

        sealed class MenuOptions(@DrawableRes val icon: Int, @StringRes val text: Int) : Parcelable {
            @Parcelize
            object CustomerStatements : MenuOptions(R.drawable.ic_statement, R.string.share_report)

            @Parcelize
            object GiveDiscounts : MenuOptions(R.drawable.ic_discount, R.string.give_discount)

            @Parcelize
            object QrCode : MenuOptions(R.drawable.ic_qr_code, R.string.show_qr_code)

            @Parcelize
            object Help : MenuOptions(R.drawable.ic_help, R.string.help)

            @Parcelize
            object Call : MenuOptions(R.drawable.ic_call_solid, R.string.call)

            @Parcelize
            data class AccountChat(val unreadCount: Int = 0) : MenuOptions(R.drawable.ic_chat, R.string.chat_with_customer)

            @Parcelize
            object Bill : MenuOptions(R.drawable.ic_bill, R.string.bills)

            @Parcelize
            object Subscriptions : MenuOptions(R.drawable.ic_icon_repeat, R.string.subscription)

            @Parcelize
            object CollectWithGooglePay : MenuOptions(R.drawable.ic_google_pay_primary_logo, R.string.collect_with_gpay)

            @Parcelize
            data class More(val canShow: Boolean = false) : MenuOptions(0, 0)

            @Parcelize
            object DeleteRelationship : MenuOptions(R.drawable.ic_delete, R.string.delete_cus)
        }
    }

    override fun loadIntent(): UserIntent {
        return MenuOptionBottomSheetContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(MenuOptionBottomSheetContract.Intent.CustomerModel(customer = customer)),
        )
    }

    override fun render(state: MenuOptionBottomSheetContract.State) {
        menuOptionsController.setData(
            state.menuSheet?.menuOptionsToShow,
            state.menuSheet?.menuOptionsToHide,
            state.showHiddenMenuOptions
        )
    }

    override fun handleViewEvent(event: MenuOptionBottomSheetContract.ViewEvent) {
        when (event) {
            is MenuOptionBottomSheetContract.ViewEvent.SendInviteToTargetedUser ->
                startActivity(event.shareIntent)
        }
    }
}
