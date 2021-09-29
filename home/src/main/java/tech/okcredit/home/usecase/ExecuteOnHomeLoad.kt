package tech.okcredit.home.usecase

import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class ExecuteOnHomeLoad @Inject constructor(
    private val trackMixpanelUserPropertiesOnHome: Lazy<TrackMixpanelUserPropertiesOnHome>,
) {

    fun execute(): Completable {
        return Completable.timer(3, TimeUnit.SECONDS)
            .andThen(trackMixpanelUserPropertiesOnHome.get().execute())
    }
}
