package tech.okcredit.home.dialogs

import `in`.okcredit.analytics.PropertyValue.HOME_PAGE
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.animation.Animator
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.transition.Explode
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.home.R
import tech.okcredit.home.ui.analytics.HomeEventTracker
import java.util.concurrent.TimeUnit

class BottomSheetInAppRating : ExpandedBottomSheetDialogFragment() {

    private var listener: OnBottomSheetFragmentListener? = null
    private var tracker: Tracker? = null
    private var rootView: CoordinatorLayout? = null
    private var isSmileyType = false

    private var starContainer: LinearLayout? = null
    private var ratingContainer: LinearLayout? = null

    private var rating1: LottieAnimationView? = null
    private var rating2: LottieAnimationView? = null
    private var rating3: LottieAnimationView? = null
    private var rating4: LottieAnimationView? = null
    private var rating5: LottieAnimationView? = null

    private var star1: ImageView? = null
    private var star2: ImageView? = null
    private var star3: ImageView? = null
    private var star4: ImageView? = null
    private var star5: ImageView? = null

    private var smiley: LottieAnimationView? = null

    private var textComment: TextInputLayout? = null
    private var bottomButton: MaterialButton? = null
    private var feedbackTextInput: EditText? = null

    private var ratingQuestion: TextView? = null
    private var labelRating: TextView? = null
    private var contentContainer: LinearLayout? = null

    private var selectedRating = 0

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboardStateHidden)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.inapp_rating_picker_bottom_sheet, container, false)

        rootView = v.findViewById(R.id.root_view)

        ratingQuestion = v.findViewById(R.id.rating_question)
        labelRating = v.findViewById(R.id.label_rating)
        contentContainer = v.findViewById(R.id.content_container)
        textComment = v.findViewById(R.id.text_comment)
        bottomButton = v.findViewById(R.id.bottom_button)
        feedbackTextInput = v.findViewById(R.id.feedback_textinput)

        starContainer = v.findViewById(R.id.star_container)
        ratingContainer = v.findViewById(R.id.rating_container)

        if (isSmileyType) {

            ratingContainer?.visibility = View.VISIBLE

            rating1 = v.findViewById(R.id.rating_1)
            rating2 = v.findViewById(R.id.rating_2)
            rating3 = v.findViewById(R.id.rating_3)
            rating4 = v.findViewById(R.id.rating_4)
            rating5 = v.findViewById(R.id.rating_5)

            rating1?.setOnClickListener {
                tracker?.trackSelectRating(1, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 1
                rating1?.playAnimationWithDisableOthers()
                changeLabel(getString(R.string.hated_it))
            }

            rating2?.setOnClickListener {
                tracker?.trackSelectRating(2, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 2
                rating2?.playAnimationWithDisableOthers()
                changeLabel(getString(R.string.did_not_like_it))
            }

            rating3?.setOnClickListener {
                tracker?.trackSelectRating(3, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 3
                rating3?.playAnimationWithDisableOthers()
                changeLabel(getString(R.string.liked_it))
            }

            rating4?.setOnClickListener {
                tracker?.trackSelectRating(4, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 4
                rating4?.playAnimationWithDisableOthers()
                changeLabel(getString(R.string.enjoyedit))
            }

            rating5?.setOnClickListener {
                tracker?.trackSelectRating(5, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 5
                rating5?.playAnimationWithDisableOthers()
                changeLabel(getString(R.string.loved_it))
                enableRateUsOnPlaystore()
                hideSoftKeyboard()
            }

            val animationListenerForSubmitFeedback = object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    enableSubmit()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            }

            rating1?.addAnimatorListener(animationListenerForSubmitFeedback)
            rating2?.addAnimatorListener(animationListenerForSubmitFeedback)
            rating3?.addAnimatorListener(animationListenerForSubmitFeedback)
            rating4?.addAnimatorListener(animationListenerForSubmitFeedback)
        } else {
            starContainer?.visibility = View.VISIBLE

            star1 = v.findViewById(R.id.star_1)
            star2 = v.findViewById(R.id.star_2)
            star3 = v.findViewById(R.id.star_3)
            star4 = v.findViewById(R.id.star_4)
            star5 = v.findViewById(R.id.star_5)

            smiley = v.findViewById(R.id.smiley)

            star1?.setOnClickListener {
                tracker?.trackSelectRating(1, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 1
                star1?.highlightStar()
                changeLabel(getString(R.string.hated_it))
            }

            star2?.setOnClickListener {
                tracker?.trackSelectRating(2, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 2
                star2?.highlightStar()
                changeLabel(getString(R.string.did_not_like_it))
            }

            star3?.setOnClickListener {
                tracker?.trackSelectRating(3, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 3
                star3?.highlightStar()
                changeLabel(getString(R.string.liked_it))
            }

            star4?.setOnClickListener {
                tracker?.trackSelectRating(4, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 4
                star4?.highlightStar()
                changeLabel(getString(R.string.liked_it))
            }

            star5?.setOnClickListener {
                tracker?.trackSelectRating(5, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
                selectedRating = 5
                star5?.highlightStar()
                changeLabel(getString(R.string.loved_it))
                enableRateUsOnPlaystore()
                hideSoftKeyboard()
            }
        }

        bottomButton?.clicks()?.throttleFirst(300, TimeUnit.MILLISECONDS)?.doOnNext {
            if (selectedRating == 5) {
                tracker?.trackInAppClicked(HomeEventTracker.RATING, true, HomeEventTracker.RATEUS_SOURCE)
                tracker?.trackSubmitFeedback(
                    HomeEventTracker.RATING,
                    5,
                    false,
                    HOME_PAGE,
                    HomeEventTracker.RATING,
                    HomeEventTracker.RATEUS_SOURCE
                )
                listener?.submitRatingAndFeedback(feedbackTextInput?.text.toString(), selectedRating)
                listener?.goToPlayStoreForRateUs()
                dismiss()
            } else {
                tracker?.trackInAppClicked(HomeEventTracker.RATING, true, HomeEventTracker.RATEUS_SOURCE)
                tracker?.trackSubmitFeedback(
                    "Feedback",
                    selectedRating,
                    feedbackTextInput?.text.toString().isNotEmpty(),
                    HOME_PAGE,
                    HomeEventTracker.RATING,
                    HomeEventTracker.RATEUS_SOURCE
                )
                hideSoftKeyboard()
                listener?.submitRatingAndFeedback(feedbackTextInput?.text.toString(), selectedRating)
                dismiss()
            }
        }?.subscribe()

        val btnCancel = v.findViewById<ImageView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            tracker?.trackInAppClicked(HomeEventTracker.RATING, false, HomeEventTracker.RATEUS_SOURCE)
            dismiss()
        }

        return v
    }

    fun initialise(mListener: OnBottomSheetFragmentListener, tracker: Tracker, isSmileyType: Boolean) {
        this.listener = mListener
        this.tracker = tracker
        this.isSmileyType = isSmileyType
    }

    fun ImageView.highlightStar() {
        TransitionManager.beginDelayedTransition(contentContainer!!, TransitionSet().addTransition(Explode()))
        smiley?.visibility = View.VISIBLE
        if (this.id == star1?.id) {
            star1?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star2?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            star3?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            star4?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            star5?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            smiley?.setAnimation(R.raw.angry)
            enableSubmit()
        } else if (this.id == star2?.id) {
            star1?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star2?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star3?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            star4?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            star5?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            smiley?.setAnimation(R.raw.didnotlikeit)
            enableSubmit()
        } else if (this.id == star3?.id) {
            star1?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star2?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star3?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star4?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            star5?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            smiley?.setAnimation(R.raw.likedit)
            enableSubmit()
        } else if (this.id == star4?.id) {
            star1?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star2?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star3?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star4?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star5?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_disable))
            smiley?.setAnimation(R.raw.enjoyedit)
            enableSubmit()
        } else if (this.id == star5?.id) {
            star1?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star2?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star3?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star4?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            star5?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rating_enable))
            smiley?.setAnimation(R.raw.lovedit)
        }

        smiley?.playAnimation()
    }

    fun LottieAnimationView.playAnimationWithDisableOthers() {
        rating1?.progress = 0F
        rating2?.progress = 0F
        rating3?.progress = 0F
        rating4?.progress = 0F
        rating5?.progress = 0F

        rating1?.cancelAnimation()
        rating2?.cancelAnimation()
        rating3?.cancelAnimation()
        rating4?.cancelAnimation()
        rating5?.cancelAnimation()

        this.playAnimation()
    }

    fun changeLabel(text: String) {
        TransitionManager.beginDelayedTransition(contentContainer!!)
        ratingQuestion?.visibility = View.GONE
        labelRating?.text = text
        labelRating?.visibility = View.VISIBLE
    }

    fun enableSubmit() {
        TransitionManager.beginDelayedTransition(
            contentContainer!!,
            TransitionSet().addTransition(Slide(Gravity.BOTTOM))
        )
        textComment?.visibility = View.VISIBLE
        bottomButton?.visibility = View.VISIBLE
        bottomButton?.text = this.getString(R.string.share_feedback)
    }

    fun enableRateUsOnPlaystore() {
        textComment?.visibility = View.GONE
        bottomButton?.visibility = View.VISIBLE
        bottomButton?.text = this.getString(R.string.rate_us_playstore)
    }

    override fun onStart() {
        super.onStart()

        try {
            val dialog = dialog ?: return
            val bottomSheet = dialog.findViewById<View>(R.id.root_view)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val view = view ?: return
            view.post {
                val parent = view.parent as View
                val params = parent.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = params.behavior
                val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.peekHeight = view.measuredHeight
                    (rootView?.parent as View).setBackgroundColor(Color.TRANSPARENT)
                }

                bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        } catch (e: Exception) {
        }
    }

    interface OnBottomSheetFragmentListener {
        fun goToPlayStoreForRateUs()
        fun submitRatingAndFeedback(feedback: String, rating: Int)
    }

    companion object {
        fun newInstance(): BottomSheetInAppRating {
            return BottomSheetInAppRating()
        }
    }
}
