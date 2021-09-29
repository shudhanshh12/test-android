package `in`.okcredit.shared.utils

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class LayoutAutoDispose {
    lateinit var compositeDisposable: CompositeDisposable

    fun add(disposable: Disposable) {
        if (::compositeDisposable.isInitialized) {
            compositeDisposable.add(disposable)
        } else {
            throw NotImplementedError("must bind LayouAutoDisposable to layout lifecycle first")
        }
    }

    fun onAttached() {
        compositeDisposable = CompositeDisposable()
    }

    fun onDetached() {
        compositeDisposable.dispose()
    }
}
