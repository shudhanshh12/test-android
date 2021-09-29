package `in`.okcredit.shared.data.server

import dagger.Lazy
import io.reactivex.Completable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class SharedRemoteSourceImpl @Inject constructor(
    private val okDocsClient: Lazy<OkDocsClient>
) : SharedRemoteSource {

    override fun uploadDbFile(file: File): Completable {
        val partFile = getMultiPartFile(file)
        return okDocsClient.get()
            .uploadDBFile(partFile)
            .ignoreElement()
    }

    private fun getMultiPartFile(dbFile: File): MultipartBody.Part {
        val contentType = "multipart/form-data".toMediaTypeOrNull()
        return MultipartBody.Part.createFormData(
            "file",
            dbFile.name,
            dbFile.asRequestBody(contentType)
        )
    }
}
