package `in`.okcredit.fileupload.usecase

import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.InputStream

interface IAwsService {
    fun uploadFile(photoType: String?, remoteUrl: String, inputStream: InputStream, flowId: String): Completable
    fun uploadAudioSampleFile(remoteUrl: String, file: File): Completable
    fun createLocalCopy(stream: InputStream): Single<File>
}
