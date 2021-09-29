package tech.okcredit.account_chat_contract

import io.reactivex.Completable

interface SignOutFirebaseAndRemoveChatListener {
    fun execute(): Completable
}
