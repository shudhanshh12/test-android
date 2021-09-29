package `in`.okcredit.frontend.widget.searchview

import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.utils.DimensionUtil
import `in`.okcredit.frontend.widget.searchview.SearchAnimationUtils.hideOrFadeOut
import `in`.okcredit.frontend.widget.searchview.SearchAnimationUtils.revealOrFadeIn
import `in`.okcredit.frontend.widget.searchview.SearchAnimationUtils.verticalSlideView
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.widget.ImageViewCompat
import com.google.android.material.tabs.TabLayout
import com.google.common.base.Strings
import kotlinx.android.synthetic.main.search_view.view.*
import tech.okcredit.android.base.utils.KeyboardUtil

/**
 * @Forked from https://github.com/Ferfalk/SimpleSearchView
 */
class SimpleSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var animationDuration = SearchAnimationUtils.ANIMATION_DURATION_DEFAULT

    private var revealAnimationCenter: Point? = null
        get() {
            if (field != null) {
                return field
            }
            val centerX: Int =
                width - DimensionUtil.dp2px(context, ANIMATION_CENTER_PADDING.toFloat()).toInt()
            val centerY = height / 2
            field = Point(centerX, centerY)
            return field
        }
    private var query: CharSequence? = null
    private var oldQuery: CharSequence? = null
    private var isSearchOpen = false
    private var isClearingFocus = false
    private val tabLayout: TabLayout? = null
    private val tabLayoutInitialHeight = 0
    private var onQueryChangeListener: OnQueryTextListener? = null
    private var searchViewListener: SearchViewListener? = null
    private var searchIsClosing = false
    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.search_view, this, true)
    }

    private fun initSearchEditText() {
        searchEditText?.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? -> true }
        searchEditText?.addTextChangedListener(object :
                TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (!searchIsClosing) {
                        this@SimpleSearchView.onTextChanged(s)
                    }
                }
            })
        searchEditText?.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                KeyboardUtil.showKeyboard(context, searchEditText!!)
            }
        }
    }

    private fun initClickListeners() {
        buttonBack?.setOnClickListener { v: View? -> closeSearchOrClearText() }
        buttonClear?.setOnClickListener { v: View? -> closeSearchOrClearText() }
    }

    override fun clearFocus() {
        isClearingFocus = true
        super.clearFocus()
        searchEditText?.clearFocus()
        isClearingFocus = false
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (isClearingFocus) {
            return false
        }
        return if (!isFocusable) {
            false
        } else searchEditText!!.requestFocus(direction, previouslyFocusedRect)
    }

    private fun clearSearch() {
        searchEditText?.text = null
        if (onQueryChangeListener != null) {
            onQueryChangeListener?.onQueryTextCleared()
        }
    }

    private fun onTextChanged(newText: CharSequence) {
        if (newText.isEmpty()) {
            buttonClear?.visibility = View.GONE
        } else {
            buttonClear?.visibility = View.VISIBLE
        }
        if (onQueryChangeListener != null && !TextUtils.equals(newText, oldQuery)) {
            onQueryChangeListener?.onQueryTextChange(newText.toString())
        }
        oldQuery = newText.toString()
    }

    private fun onSubmitQuery() {
        val submittedQuery: CharSequence? = searchEditText?.text
        if (submittedQuery != null && TextUtils.getTrimmedLength(submittedQuery) > 0) {
            if (onQueryChangeListener == null || !onQueryChangeListener!!.onQueryTextSubmit(submittedQuery.toString())) {
                closeSearch()
                searchIsClosing = true
                searchEditText?.text = null
                searchIsClosing = false
            }
        }
    }

    @JvmOverloads
    fun showSearch(animate: Boolean = true) {
        if (isSearchOpen) {
            return
        }
        searchEditText?.setText(query)
        searchEditText?.requestFocus()
        if (animate) {
            val animationListener: SearchAnimationUtils.AnimationListener =
                object : SearchAnimationUtils.AnimationListener {
                    override fun onAnimationStart(view: View): Boolean {
                        return false
                    }

                    override fun onAnimationEnd(view: View): Boolean {
                        searchViewListener?.onSearchViewShownAnimation()
                        return false
                    }

                    override fun onAnimationCancel(view: View): Boolean {
                        return false
                    }
                }
            revealOrFadeIn(this, animationDuration, animationListener, revealAnimationCenter)
                .start()
        } else {
            visibility = View.VISIBLE
        }
        hideTabLayout(animate)
        isSearchOpen = true
        if (searchViewListener != null) {
            searchViewListener?.onSearchViewShown()
        }
    }

    fun closeSearchOrClearText() {
        if (!Strings.isNullOrEmpty(searchEditText?.text.toString())) {
            searchEditText?.text = null
        } else {
            KeyboardUtil.hideKeyboard(context, searchEditText)
            closeSearch()
        }
    }

    @JvmOverloads
    fun closeSearch(animate: Boolean = true) {
        if (!isSearchOpen) {
            return
        }
        searchIsClosing = true
        searchEditText?.text = null
        searchIsClosing = false
        clearFocus()
        if (animate) {
            val animationListener: SearchAnimationUtils.AnimationListener =
                object : SearchAnimationUtils.AnimationListener {
                    override fun onAnimationStart(view: View): Boolean {
                        return false
                    }

                    override fun onAnimationEnd(view: View): Boolean {
                        searchViewListener?.onSearchViewClosedAnimation()
                        return false
                    }

                    override fun onAnimationCancel(view: View): Boolean {
                        return false
                    }
                }
            hideOrFadeOut(this, animationDuration, animationListener, revealAnimationCenter)
                .start()
        } else {
            visibility = View.INVISIBLE
        }
        showTabLayout(animate)
        isSearchOpen = false
        if (searchViewListener != null) {
            searchViewListener?.onSearchViewClosed()
        }
    }

    @JvmOverloads
    fun showTabLayout(animate: Boolean = true) {
        if (tabLayout == null) {
            return
        }
        if (animate) {
            verticalSlideView(tabLayout, 0, tabLayoutInitialHeight, animationDuration).start()
        } else {
            tabLayout.visibility = View.VISIBLE
        }
    }

    @JvmOverloads
    fun hideTabLayout(animate: Boolean = true) {
        if (tabLayout == null) {
            return
        }
        if (animate) {
            verticalSlideView(tabLayout, tabLayout.height, 0, animationDuration).start()
        } else {
            tabLayout.visibility = View.GONE
        }
    }

    fun onBackPressed(): Boolean {
        if (isSearchOpen) {
            closeSearch()
            return true
        }
        return false
    }

    fun setIconsColor(@ColorInt color: Int) {
        ImageViewCompat.setImageTintList(buttonClear!!, ColorStateList.valueOf(color))
    }

    fun setSearchBackground(background: Drawable?) {
        searchContainer?.background = background
    }

    fun setTextColor(@ColorInt color: Int) {
        searchEditText?.setTextColor(color)
    }

    fun setHintTextColor(@ColorInt color: Int) {
        searchEditText?.setHintTextColor(color)
    }

    fun setHint(hint: CharSequence?) {
        searchEditText?.hint = hint
    }

    fun setInputType(inputType: Int) {
        searchEditText?.inputType = inputType
    }

    fun setQuery(query: CharSequence?, submit: Boolean) {
        searchEditText?.setText(query)
        if (query != null) {
            searchEditText?.setSelection(searchEditText!!.length())
            this.query = query
        }
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery()
        }
    }

    fun getQuery(): String {
        return searchEditText?.text.toString()
    }

    fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        onQueryChangeListener = listener
    }

    fun setOnSearchViewListener(listener: SearchViewListener?) {
        searchViewListener = listener
    }

    interface OnQueryTextListener {
        fun onQueryTextSubmit(query: String?): Boolean
        fun onQueryTextChange(newText: String?): Boolean
        fun onQueryTextCleared(): Boolean
    }

    interface SearchViewListener {
        fun onSearchViewShown()
        fun onSearchViewClosed()
        fun onSearchViewShownAnimation()
        fun onSearchViewClosedAnimation()
    }

    companion object {
        const val ANIMATION_CENTER_PADDING = 26
    }

    init {
        inflate()
        initSearchEditText()
        initClickListeners()
        if (!isInEditMode) {
            visibility = View.INVISIBLE
        }
    }
}
