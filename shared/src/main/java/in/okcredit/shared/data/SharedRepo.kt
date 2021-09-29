package `in`.okcredit.shared.data

import io.reactivex.Completable
import java.io.File

interface SharedRepo {
    fun uploadDbFile(file: File): Completable
}
