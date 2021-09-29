package `in`.okcredit.fileupload.utils

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Inject

interface ISchedulerProvider {
    fun database(): Scheduler
    fun api(): Scheduler
    fun download(): Scheduler
    fun upload(): Scheduler
    fun files(): Scheduler
    fun newThread(): Scheduler
    fun worker(): Scheduler
    fun computation(): Scheduler
}

class SchedulerProvider @Inject constructor() : ISchedulerProvider {
    override fun database() = Schedulers.from(Executors.newCachedThreadPool())

    override fun api() = Schedulers.from(Executors.newCachedThreadPool())

    override fun download() = Schedulers.from(Executors.newFixedThreadPool(2))

    override fun upload() = Schedulers.from(Executors.newFixedThreadPool(2))

    override fun files() = Schedulers.from(Executors.newCachedThreadPool())

    override fun newThread() = Schedulers.newThread()

    override fun worker() = Schedulers.from(Executors.newCachedThreadPool())

    override fun computation() = Schedulers.computation()
}
