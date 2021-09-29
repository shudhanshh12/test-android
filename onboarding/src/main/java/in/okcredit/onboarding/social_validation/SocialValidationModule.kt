package `in`.okcredit.onboarding.social_validation

import `in`.okcredit.onboarding.BuildConfig
import `in`.okcredit.onboarding.social_validation.data.SocialValidationService
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.base.network.DefaultOkHttpClient
import javax.inject.Provider

@Module
abstract class SocialValidationModule {

    @Binds
    abstract fun activity(activity: SocialValidationActivity): AppCompatActivity

    companion object {

        @Provides
        fun initialState(): SocialValidationContract.State = SocialValidationContract.State()

        @Provides
        fun viewModel(
            activity: SocialValidationActivity,
            viewModelProvider: Provider<SocialValidationViewModel>,
        ): MviViewModel<SocialValidationContract.State> = activity.createViewModel(viewModelProvider)

        @Provides
        fun socialValidationService(
            @DefaultOkHttpClient okHttpClient: OkHttpClient,
        ): SocialValidationService {
            val loggingInterceptor = HttpLoggingInterceptor()
                .apply { level = HttpLoggingInterceptor.Level.BODY }

            val okHttpClientBuilder = okHttpClient
                .newBuilder()
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.AUI_BASE_URL)
                .callFactory(okHttpClientBuilder)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .build()
                .create(SocialValidationService::class.java)
        }
    }
}
