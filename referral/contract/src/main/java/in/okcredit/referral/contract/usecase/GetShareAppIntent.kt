package `in`.okcredit.referral.contract.usecase

import android.content.Intent
import io.reactivex.Single

interface GetShareAppIntent {
    fun execute(): Single<Intent>
}
