package tech.okcredit.app_contract

import android.content.Context

interface AppShortcutAdder {

    enum class Shortcut(val id: String) {
        ADD_CUSTOMER("Add_Customer"),
        SEARCH_CUSTOMER("Search"),
        ADD_TRANSACTION("add_transaction")
    }

    fun addAppShortcutIfNotAdded(shortcut: Shortcut, context: Context)
}
