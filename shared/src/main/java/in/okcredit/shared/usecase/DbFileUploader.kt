package `in`.okcredit.shared.usecase

import `in`.okcredit.shared.data.SharedRepo
import dagger.Lazy
import io.reactivex.Completable
import java.io.File
import javax.inject.Inject

class DbFileUploader @Inject constructor(
    private val sharedRepo: Lazy<SharedRepo>
) {
    fun execute(file: File): Completable {
        return sharedRepo.get().uploadDbFile(file)
    }
}
