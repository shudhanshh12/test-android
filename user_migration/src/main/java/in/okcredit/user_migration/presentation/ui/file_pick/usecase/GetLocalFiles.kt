package `in`.okcredit.user_migration.presentation.ui.file_pick.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.os.Environment
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class GetLocalFiles @Inject constructor() {

    val dir = Environment.getExternalStorageDirectory().absolutePath

    fun execute(): Observable<Result<List<String>>> {
        return UseCase.wrapSingle(
            readLocalFiles()
                .flatMap { filteredOut(it) }
        )
    }

    private fun readLocalFiles(): Single<List<String>> {
        return Single.create { single ->
            val ANDROID_DIR = File("$dir/Android")
            val DATA_DIR = File("$dir/data")
            val list = File(dir).walk()
                // befor entering this dir check if
                .onEnter {
                    !it.isHidden && // it is not hidden
                        it != ANDROID_DIR && // it is not Android directory
                        it != DATA_DIR && // it is not data directory
                        !File(it, ".nomedia").exists() // there is no .nomedia file inside
                }.filter { it.extension == FILE_TYPE_PDF }
                .map {
                    it.absolutePath
                }
                .toList()
            single.onSuccess(list)
        }
    }

    private fun filteredOut(files: List<String>): Single<List<String>> {
        return Single.create { single ->

            val filteredList = files
                .filter {
                    File(it).length() / (1024 * 1024) <= MAX_FILE_SIZE_MB
                }
            val orderBy = compareBy<String> { it.contains("kb") }
                .thenBy { it.contains("txn") }
                .thenBy { it.contains("khatabook") }
                .thenBy { it.contains("customer") }
                .thenBy { it.contains("transaction") }

            val finalList = filteredList.sortedWith(orderBy).asReversed()

            single.onSuccess(finalList)
        }
    }

    companion object {
        const val FILE_TYPE_PDF = "pdf"
        const val MAX_FILE_SIZE_MB = 1
        const val KHATABOOK = "khatabook"
    }
}
