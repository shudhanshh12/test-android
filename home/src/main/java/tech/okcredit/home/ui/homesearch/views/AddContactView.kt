package tech.okcredit.home.ui.homesearch.views

import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.homesearch_contact_view.view.*
import kotlinx.coroutines.*
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.home.R
import tech.okcredit.home.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AddContactView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.homesearch_contact_view, this, true)
    }

    interface AddLocalContactListener {
        fun onAddContact(contact: Contact)
    }

    lateinit var contact: Contact

    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancelChildren()
    }

    @ModelProp
    fun setContacts(contact: Contact) {
        this.contact = contact
        setProfilePhoto()
        setName()
        setMobile()
    }

    private fun setProfilePhoto() {
        viewScope.launch {
            val defaultPic = TextDrawableUtils.getRoundTextDrawable(contact.name)

            withContext(Dispatchers.Main) {
                GlideApp.with(context)
                    .load(contact.picUri)
                    .placeholder(defaultPic)
                    .circleCrop()
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .thumbnail(0.25f)
                    .into(photo_image_view)
            }
        }
    }

    private fun setName() {
        name_text_view.text = contact.name
    }

    private fun setMobile() {
        tvMobile.text = contact.mobile
    }

    @CallbackProp
    fun setListener(listener: AddLocalContactListener?) {
        llAddLocalContact.setOnClickListener {
            listener?.onAddContact(contact)
        }
    }
}
