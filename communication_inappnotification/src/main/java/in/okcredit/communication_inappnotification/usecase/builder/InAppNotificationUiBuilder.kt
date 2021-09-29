package `in`.okcredit.communication_inappnotification.usecase.builder

import android.graphics.Typeface

interface InAppNotificationUiBuilder {

    fun setPrimaryText(title: String): InAppNotificationUiBuilder

    fun setPrimaryTextSize(textSize: Float): InAppNotificationUiBuilder

    fun setBackgroundColour(colorId: Int? = null): InAppNotificationUiBuilder

    fun setPrimaryTextTypeFace(typeFace: Typeface, style: Int? = null): InAppNotificationUiBuilder

    fun setRadius(radius: Float): InAppNotificationUiBuilder

    fun setPadding(padding: Float): InAppNotificationUiBuilder

    fun setPrimaryTextGravity(gravity: Int): InAppNotificationUiBuilder

    fun show(): InAppNotificationUiBuilder

    /**
     * Responsible to clear all the references which can potentially leak memory
     */
    fun removeReferences()
}
