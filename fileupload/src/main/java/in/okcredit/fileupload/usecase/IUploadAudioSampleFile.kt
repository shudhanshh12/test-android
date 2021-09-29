package `in`.okcredit.fileupload.usecase

import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Single

interface IUploadAudioSampleFile {

    fun schedule(
        remoteUrl: String,
        localPath: String,
        trackerProperties: Map<String, String>,
    ): Completable

    fun createLocalFile(uri: Uri, remoteUrl: String): Single<String>

    companion object {
        const val AWS_AUDIO_SAMPLES_BUCKET_NAME = "okcredit-voice-samples"
        const val AWS_AUDIO_SAMPLES_BASE_URL = "https://s3.amazonaws.com/okcredit-voice-samples"
    }
}
