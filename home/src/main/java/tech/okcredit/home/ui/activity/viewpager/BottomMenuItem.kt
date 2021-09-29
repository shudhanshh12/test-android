package tech.okcredit.home.ui.activity.viewpager

data class BottomMenuItem(
    val navItem: NavItem,
    val drawableId: Int,
    val stringId: Int,
    val contentDescriptionId: Int, // used for backend-controlled-in-app-notifications feature
)
