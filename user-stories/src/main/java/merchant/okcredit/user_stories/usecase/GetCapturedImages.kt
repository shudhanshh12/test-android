package merchant.okcredit.user_stories.usecase

import com.camera.models.models.Picture
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.camera_contract.CapturedImage
import java.io.File
import javax.inject.Inject

class GetCapturedImages @Inject constructor() {
    fun execute(images: ArrayList<Picture>): Observable<ArrayList<CapturedImage>> {
        return Observable.fromCallable {
            return@fromCallable images.map { picture ->
                return@map CapturedImage(File(picture.path))
            }.toArrayList()
        }
    }
}
