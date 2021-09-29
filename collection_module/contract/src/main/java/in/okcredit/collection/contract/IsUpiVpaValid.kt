package `in`.okcredit.collection.contract

import io.reactivex.Single

interface IsUpiVpaValid {

    fun execute(req: String): Single<Pair<Boolean, String>>
}
