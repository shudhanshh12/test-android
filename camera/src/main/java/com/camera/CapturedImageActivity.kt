package com.camera

import `in`.okcredit.analytics.Tracker
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.DisplayMetrics
import com.bumptech.glide.Glide
import com.camera.models.models.Picture
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_selected_image.*
import tech.okcredit.android.base.BaseLanguageActivity
import javax.inject.Inject

class CapturedImageActivity : BaseLanguageActivity() {
    private var flow: String? = null
    private var relation: String? = null
    private var type: String? = null
    private var screen: String? = null
    private var mobile: String? = null
    private var account: String? = null
    private var txnId: String? = null
    private lateinit var photos: Picture

    @Inject
    internal lateinit var tracker: Tracker

    companion object {
        fun createIntent(
            context: Context,
            picture: Picture,
            flow: String?,
            relation: String?,
            type: String?,
            screen: String?,
            mobile: String?,
            account: String?,
            txnId: String? = null
        ): Intent? {
            val intent = Intent(context, CapturedImageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("photoPath", picture)
            intent.putExtra("flow", flow)
            intent.putExtra("relation", relation)
            intent.putExtra("type", type)
            intent.putExtra("screen", screen)
            intent.putExtra("mobile", mobile)
            intent.putExtra("account", account)
            intent.putExtra("txnId", txnId)
            return intent
        }
    }

    override fun onResume() {
        super.onResume()
        makeFullscreen(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_image)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        getDataFromIntent()
        ic_big_selected_pics.animate("y", 1000)
        add_more.animate("y", 1000)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        Glide.with(this).load(photos.path).into(image)
        add_more.setOnClickListener {
            onBackPressed()
        }
        ic_big_selected_pics.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        delete.setOnClickListener {
            tracker.trackDeleteReceipt(flow, relation, type, "Add Screen", account, txnId)
            val intent = Intent()
            intent.putExtra("deleted_photo", photos)
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
        back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("deleted_photo", photos)
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }

    private fun getDataFromIntent() {
        photos = intent.getSerializableExtra("photoPath") as Picture
        flow = intent.getStringExtra("flow")
        relation = intent.getStringExtra("relation")
        type = intent.getStringExtra("type")
        screen = intent.getStringExtra("screen")
        mobile = intent.getStringExtra("mobile")
        txnId = intent.getStringExtra("txnId")
    }
}
