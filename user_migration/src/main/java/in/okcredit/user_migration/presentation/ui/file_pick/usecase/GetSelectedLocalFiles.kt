package `in`.okcredit.user_migration.presentation.ui.file_pick.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetSelectedLocalFiles @Inject constructor() {

    fun execute(filePath: String, selectedLocalFiles: MutableList<String>): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            getFiles(filePath, selectedLocalFiles)
        )
    }

    private fun getFiles(filePath: String, selectedLocalFiles: MutableList<String>): Observable<Response> {
        if (selectedLocalFiles.contains(filePath)) {
            selectedLocalFiles.remove(filePath)
        } else {
            selectedLocalFiles.add(filePath)
        }
        val show = selectedLocalFiles.isNotEmpty()
        return Observable.just(
            Response(
                show,
                selectedLocalFiles
            )
        )
    }

    data class Response(val show: Boolean, val filePaths: List<String>)
}
