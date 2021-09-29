package tech.okcredit.android.base.utils

import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single

fun <T> Lazy<T>.offloaded(): Single<T> = Single.fromCallable { this.get() }.subscribeOn(ThreadUtils.io())

fun <T> Single<T>.toCompletable(action: (T) -> Unit): Completable = doOnSuccess(action).ignoreElement()
