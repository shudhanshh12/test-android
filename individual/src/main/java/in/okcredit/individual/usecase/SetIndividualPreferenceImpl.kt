package `in`.okcredit.individual.usecase

import `in`.okcredit.individual.IndividualRepositoryImpl
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.individual.contract.model.Individual
import `in`.okcredit.individual.data.remote.IndividualUser
import `in`.okcredit.individual.data.remote.UpdateIndividualRequest
import `in`.okcredit.individual.usecase.SetIndividualPreferenceWorker.Companion.BUSINESS_ID
import `in`.okcredit.individual.usecase.SetIndividualPreferenceWorker.Companion.KEY
import `in`.okcredit.individual.usecase.SetIndividualPreferenceWorker.Companion.VALUE
import `in`.okcredit.individual.usecase.SetIndividualPreferenceWorker.Companion.WORKER_NAME
import android.content.Context
import androidx.work.*
import dagger.Lazy
import kotlinx.coroutines.flow.first
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import `in`.okcredit.individual.data.remote.Individual as ApiIndividual

class SetIndividualPreferenceImpl @Inject constructor(
    private val repository: Lazy<IndividualRepositoryImpl>,
    private val workManager: Lazy<OkcWorkManager>,
) : SetIndividualPreference {
    override suspend fun execute(key: String, value: String, businessId: String) {
        val individual = repository.get().getIndividualFromLocalSource().first()
        repository.get().setIndividualPreference(buildUpdateIndividualRequest(individual, key, value), businessId)
        repository.get().setPreference(key, value)
    }

    private fun buildUpdateIndividualRequest(
        individual: Individual,
        key: String,
        value: String,
    ): UpdateIndividualRequest {
        return when (key) {
            PreferenceKey.WHATSAPP.key -> {
                UpdateIndividualRequest(
                    individual_user_id = individual.id,
                    individual_user = ApiIndividual(
                        whatsapp_opt_in = value.toBoolean(),
                        user = IndividualUser(id = individual.id, mobile = individual.mobile)
                    ),
                    update_whatsapp_opt_in = true
                )
            }
            PreferenceKey.PAYMENT_PASSWORD.key -> {
                UpdateIndividualRequest(
                    individual_user_id = individual.id,
                    individual_user = ApiIndividual(
                        payment_password_enabled = value.toBoolean(),
                        user = IndividualUser(id = individual.id, mobile = individual.mobile)
                    ),
                    update_payment_password_enabled = true
                )
            }
            PreferenceKey.APP_LOCK.key -> {
                UpdateIndividualRequest(
                    individual_user_id = individual.id,
                    individual_user = ApiIndividual(
                        app_lock_opt_in = value.toBoolean(),
                        user = IndividualUser(id = individual.id, mobile = individual.mobile)
                    ),
                    update_app_lock_opt_in = true
                )
            }
            PreferenceKey.FINGER_PRINT_LOCK.key -> {
                UpdateIndividualRequest(
                    individual_user_id = individual.id,
                    individual_user = ApiIndividual(
                        fingerprint_lock_opt_in = value.toBoolean(),
                        user = IndividualUser(id = individual.id, mobile = individual.mobile)
                    ),
                    update_fingerprint_lock_opt_in = true
                )
            }
            PreferenceKey.FOUR_DIGIT_PIN.key -> {
                UpdateIndividualRequest(
                    individual_user_id = individual.id,
                    individual_user = ApiIndividual(
                        four_digit_pin_in = value.toBoolean(),
                        user = IndividualUser(id = individual.id, mobile = individual.mobile)
                    ),
                    update_four_digit_pin_opt_in = true
                )
            }
            PreferenceKey.LANGUAGE.key -> {
                UpdateIndividualRequest(
                    individual_user_id = individual.id,
                    individual_user = ApiIndividual(
                        user = IndividualUser(id = individual.id, mobile = individual.mobile, lang = value)
                    ),
                    update_lang = true
                )
            }
            else -> throw IllegalArgumentException("Unknown key : $key")
        }
    }

    override suspend fun schedule(key: String, value: String, businessId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(SetIndividualPreferenceWorker::class.java)
            .addTag(WORKER_NAME)
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    KEY to key,
                    VALUE to value,
                    BUSINESS_ID to businessId
                )
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()
            .enableWorkerLogging()
        workManager.get().schedule(
            WORKER_NAME,
            Scope.Individual,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
    }
}

class SetIndividualPreferenceWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val setIndividualPreferenceImpl: Lazy<SetIndividualPreferenceImpl>,
) : BaseCoroutineWorker(context, params) {
    companion object {
        const val BUSINESS_ID = "business_id"
        const val KEY = "key"
        const val VALUE = "value"

        const val WORKER_NAME = "set_individual_preference"
    }

    class Factory @Inject constructor(
        private val setIndividualPreferenceImpl: Lazy<SetIndividualPreferenceImpl>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return SetIndividualPreferenceWorker(
                context = context,
                params = params,
                setIndividualPreferenceImpl = setIndividualPreferenceImpl,
            )
        }
    }

    override suspend fun doActualWork() {
        val businessId = inputData.getString(BUSINESS_ID)!!
        val key = inputData.getString(KEY)!!
        val value = inputData.getString(VALUE)!!
        setIndividualPreferenceImpl.get().execute(key, value, businessId)
    }
}
