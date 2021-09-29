package `in`.okcredit.collection_ui.ui.home.adoption

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class CollectionAdoptionItem(
    val position: Int,
    @StringRes val title: Int,
    @DrawableRes val illustration: Int,
)
