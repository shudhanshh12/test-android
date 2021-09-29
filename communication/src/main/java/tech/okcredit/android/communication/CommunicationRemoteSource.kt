package tech.okcredit.android.communication

import io.reactivex.Completable

interface CommunicationRemoteSource {

    fun acknowledge(msgId: String, businessId: String): Completable
}
