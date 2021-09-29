package tech.okcredit.home.ui.home.views

import `in`.okcredit.backend.utils.BroadcastHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.supplier_native_player_view.view.*
import tech.okcredit.android.base.utils.PlayerStateChangeListener
import tech.okcredit.home.R
import tech.okcredit.home.ui.supplier_tab.SupplierTabContract

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class NativePlayerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var broadcastHelper: BroadcastHelper? = null

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            broadcastHelper?.onBroadcastReceived(intent)
            if (intent?.action == BroadcastHelper.IntentFilters.PlayerPause) {
                player?.playWhenReady = false
            }
        }
    }
    private var listener: NativeListener? = null
    private var player: SimpleExoPlayer? = null

    interface NativeListener {
        fun onVideoResume()
        fun onVideoPause()
        fun onVideoAttached()
        fun onVideoDetached()
        fun onVideoStarted()
        fun onVideoCompleted()
        fun onVideoErrorOccured()
    }

    @CallbackProp
    fun setListener(listener: NativeListener?) {
        this.listener = listener
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.supplier_native_player_view, this, true)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setBroadcastHelper(broadcastHelper: BroadcastHelper) {
        this.broadcastHelper = broadcastHelper
    }

    @ModelProp
    fun setState(state: String) {
        if (state == SupplierTabContract.CONFIG.PAUSE) {
            player?.playWhenReady = false
        }
        if (state == SupplierTabContract.CONFIG.RESUME) {
            player?.playWhenReady = true
        }
    }

    override fun onAttachedToWindow() {
        initializePlayer()
        broadcastHelper?.registerReceiver(context, receiver, IntentFilter(BroadcastHelper.IntentFilters.PlayerPause))
        listener?.onVideoAttached()
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        listener?.onVideoPause()
        listener?.onVideoDetached()
        broadcastHelper?.unregisterReceiver(context, receiver)
        releasePlayer()
        super.onDetachedFromWindow()
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )
            videoview?.player = player
        }
        val mediaSource = buildMediaSource()
        player?.prepare(mediaSource, true, false)
        player?.addListener(object : PlayerStateChangeListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady) {
                    listener?.onVideoResume()
                } else {
                    listener?.onVideoPause()
                }
                if (playbackState == Player.STATE_READY) {
                    listener?.onVideoStarted()
                }
                if (playbackState == Player.STATE_ENDED) {
                    listener?.onVideoCompleted()
                }
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                listener?.onVideoErrorOccured()
            }
        })
    }

    private fun buildMediaSource(): MediaSource {
        val defaultDataSourceFactory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, context.packageName))
        return ExtractorMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(RawResourceDataSource.buildRawResourceUri(R.raw.supplier))
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }
}
