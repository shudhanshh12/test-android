package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ViewMenuOptionItemBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet.Companion.MenuOptions
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.addRipple
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MenuOptionItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onMenuItemClicked(menuOption: MenuOptions)
    }

    private lateinit var menuOption: MenuOptions

    private var listener: Listener? = null
    private val binding: ViewMenuOptionItemBinding =
        ViewMenuOptionItemBinding.inflate(LayoutInflater.from(context), this)

    init {
        addRipple()
    }

    @ModelProp(options = [ModelProp.Option.IgnoreRequireHashCode])
    fun setMenu(menuOption: MenuOptions) {
        this.menuOption = menuOption
    }

    @AfterPropsSet
    fun render() {
        binding.divider.visible()
        when (menuOption) {
            is MenuOptions.More -> {
                setLoadMore((menuOption as MenuOptions.More).canShow)
            }
            is MenuOptions.CollectWithGooglePay -> {
                setMenuOption(
                    menuText = menuOption.text,
                    image = menuOption.icon,
                    imageTint = 0
                )
            }
            is MenuOptions.DeleteRelationship -> {
                setMenuOption(
                    menuText = menuOption.text,
                    image = menuOption.icon,
                    imageTint = R.color.red_primary,
                    textColor = R.color.red_primary,
                )
            }
            is MenuOptions.AccountChat -> {
                setMenuOption(
                    menuOption.text,
                    menuOption.icon,
                    unreadChatCount = (menuOption as MenuOptions.AccountChat).unreadCount
                )
            }
            else -> {
                setMenuOption(menuOption.text, menuOption.icon)
            }
        }
        binding.root.debounceClickListener {
            listener?.onMenuItemClicked(menuOption)
        }
    }

    private fun setLoadMore(canShow: Boolean) {
        if (canShow) {
            setMenuOption(menuText = R.string.close, image = 0, drawableRight = R.drawable.ic_close)
        } else {
            setMenuOption(menuText = R.string.more, image = 0, drawableRight = R.drawable.ic_arrow_right)
        }
        binding.divider.gone()
    }

    private fun setMenuOption(
        @StringRes menuText: Int,
        @DrawableRes image: Int,
        @ColorRes imageTint: Int = R.color.grey800,
        @DrawableRes drawableRight: Int = 0,
        @ColorRes textColor: Int = R.color.grey900,
        unreadChatCount: Int = 0,
    ) {
        binding.menuItem.apply {
            text = context.getString(menuText)
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                drawableRight,
                0
            )
            setTextColor(context.getColorCompat(textColor))
        }

        binding.imageMenu.apply {
            isVisible = image != 0
            if (image != 0) {
                setImageDrawable(ContextCompat.getDrawable(context, image))
                if (imageTint != 0) {
                    ImageViewCompat.setImageTintList(this, ContextCompat.getColorStateList(context, imageTint))
                } else {
                    ImageViewCompat.setImageTintList(this, null)
                }
            }
        }
        if (unreadChatCount > 0) {
            binding.chatCount.visible()
            val countText = if (unreadChatCount > 9) {
                "9+"
            } else {
                unreadChatCount.toString()
            }
            binding.chatCount.text = countText
        } else {
            binding.chatCount.gone()
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
