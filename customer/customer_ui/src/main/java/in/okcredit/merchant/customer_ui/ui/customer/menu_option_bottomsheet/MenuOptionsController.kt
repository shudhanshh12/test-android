package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet

import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet.Companion.MenuOptions
import com.airbnb.epoxy.Typed3EpoxyController
import javax.inject.Inject

class MenuOptionsController @Inject constructor() :
    Typed3EpoxyController<List<MenuOptions>, List<MenuOptions>, Boolean>() {

    private var listener: MenuOptionItemView.Listener? = null

    fun setListener(listener: MenuOptionItemView.Listener) {
        this.listener = listener
    }

    override fun buildModels(
        menuOptionsToShow: List<MenuOptions>?,
        menuOptionsToHide: List<MenuOptions>?,
        showHiddenMenuOptions: Boolean,
    ) {
        menuOptionsToShow?.forEach {
            menuOptionItemView {
                id(it.hashCode())
                menu(it)
                listener(listener)
            }
        }
        if (showHiddenMenuOptions) {
            menuOptionsToHide?.forEach {
                menuOptionItemView {
                    id(it.hashCode())
                    menu(it)
                    listener(listener)
                }
            }
        }

        if (!menuOptionsToHide.isNullOrEmpty()) {
            menuOptionItemView {
                id("more")
                menu(MenuOptions.More(showHiddenMenuOptions))
                listener(listener)
            }
        }
    }
}
