package `in`.okcredit.shared.data.server

import io.reactivex.Completable
import java.io.File

interface SharedRemoteSource {
    fun uploadDbFile(file: File): Completable
}
