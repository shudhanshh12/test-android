package tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import tech.okcredit.BillUtils
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bill_management_ui.databinding.EnhanceImageFragmentBinding
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageScreenContract.*
import java.io.File
import java.util.concurrent.TimeUnit

class EnhanceImageFragment : BaseFragment<State, ViewEvent, Intent>(
    "EnhanceImageScreenScreen",
    R.layout.enhance_image_fragment
) {

    private var imageEnhanced: Boolean = false
    private var animator: ObjectAnimator? = null
    internal val binding: EnhanceImageFragmentBinding by viewLifecycleScoped(EnhanceImageFragmentBinding::bind)

    private val updateSubject: PublishSubject<File> = PublishSubject.create()

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            updateSubject.throttleFirst(200, TimeUnit.MILLISECONDS).map {
                Intent.UpdateImage(it)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.back.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun render(state: State) {
        if (state.canEnhanceIamge.not()) {
            state.imageList?.let {
                val intent = android.content.Intent()
                intent.putExtra("existingImages", state.imageList)
                activity?.let {
                    it.setResult(Activity.RESULT_OK, intent)
                    it.onBackPressed()
                }
            }
        }
        state.imageURL?.let { capturedPic ->
            val requestOptions = RequestOptions().transforms(
                CenterCrop(),
                RoundedCorners(DimensionUtil.dp2px(requireContext(), 4.0f).toInt())
            )

            Glide.with(requireActivity())
                .load(capturedPic.file.path)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (imageEnhanced.not()) {
                            animator = ObjectAnimator.ofFloat(
                                binding.highlighter,
                                "translationY",
                                DimensionUtil.dp2px(context!!, 340f)
                            ).apply {
                                duration = 2000
                                repeatCount = 1
                                repeatMode = ObjectAnimator.REVERSE
                                addListener(object : Animator.AnimatorListener {
                                    override fun onAnimationRepeat(animation: Animator?) {
                                    }

                                    override fun onAnimationEnd(animation: Animator?) {
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            imageEnhanced = true
                                            val drawable: BitmapDrawable = binding.container.drawable as BitmapDrawable
                                            val bitmap: Bitmap = drawable.bitmap
                                            val enhancedBitmap = BillUtils.enhanceImage(bitmap)
                                            binding.container.setImageBitmap(enhancedBitmap)
                                            Observable.timer(1000, TimeUnit.MILLISECONDS).subscribe {
                                                updateSubject.onNext(BillUtils.saveBitmapToFile(enhancedBitmap!!))
                                            }.addTo(autoDisposable)
                                        }
                                    }

                                    override fun onAnimationCancel(animation: Animator?) {
                                    }

                                    override fun onAnimationStart(animation: Animator?) {
                                    }
                                })
                                start()
                            }
                        }
                        return false
                    }
                })
                .apply(requestOptions)
                .into(binding.container)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {}

    override fun onDestroyView() {
        animator?.cancel()
        super.onDestroyView()
    }
}
