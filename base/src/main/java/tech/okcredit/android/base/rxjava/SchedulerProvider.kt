package tech.okcredit.android.base.rxjava

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SchedulerProvider @Inject constructor() {

    fun ui(): Scheduler = AndroidSchedulers.mainThread()

    fun io(): Scheduler = Schedulers.io()
}
