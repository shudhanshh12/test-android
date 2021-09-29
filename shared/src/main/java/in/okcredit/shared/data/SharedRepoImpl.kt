package `in`.okcredit.shared.data

import `in`.okcredit.shared.data.server.SharedRemoteSource
import dagger.Lazy
import io.reactivex.Completable
import java.io.File
import javax.inject.Inject

class SharedRepoImpl @Inject constructor(
    private val sharedRemoteSource: Lazy<SharedRemoteSource>
) : SharedRepo {

    override fun uploadDbFile(file: File): Completable {
        return sharedRemoteSource.get().uploadDbFile(file)
    }
}
