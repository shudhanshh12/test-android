package com.camera.camerax

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.camera.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.camera_interaction_container.view.*
import java.util.concurrent.TimeUnit

class CameraInteractionLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var interactor: Interactor
    private var clickSubject = PublishSubject.create<Unit>()
    private var backSubject = PublishSubject.create<Unit>()
    private var isTorchOn = false

    fun addInteractor(interactor: Interactor) {
        this.interactor = interactor
    }

    fun offTorch() {
        flash_iv.setImageDrawable(context?.getDrawable(R.drawable.ic_flash_off))
    }

    fun onTorch() {
        flash_iv.setImageDrawable(context?.getDrawable(R.drawable.ic_flash_on))
    }

    init {
        View.inflate(context, R.layout.camera_interaction_container, this)
        camera_capture_button.setOnClickListener {
            clickSubject.onNext(Unit)
        }

        flash_iv.setOnClickListener {
            isTorchOn = !isTorchOn
            interactor.onFlashClicked(isTorchOn)
        }
        back.setOnClickListener {
            backSubject.onNext(Unit)
        }
        disposable.add(
            clickSubject.throttleFirst(300, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                interactor.onClick()
            }
        )
        disposable.add(
            backSubject.throttleFirst(300, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                interactor.onBackClicked()
            }
        )
    }

    override fun onDetachedFromWindow() {
        disposable.dispose()
        super.onDetachedFromWindow()
    }

    interface Interactor {
        fun onClick()
        fun onFlashClicked(torchOn: Boolean)
        fun onBackClicked()
    }
}
