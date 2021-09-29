package tech.okcredit.android.ab

import `in`.okcredit.merchant.device.Device
import org.joda.time.DateTime
import tech.okcredit.android.ab.server.Experiment

internal object TestData {
    const val ID = "lubalubadubdub"

    const val DEVICE_ID = "lubalubadubdub lubalubadubdub"
    const val BUSINESS_ID = "lubalubadubdub lubalubadubdub"
    val DEVICE = Device(DEVICE_ID, 12, 12, "", "", mutableListOf(), DateTime.now(), DateTime())

    const val FEATURE_1 = "self_reminder"
    const val FEATURE_2 = "collection"
    const val FEATURE_3 = "unknown_feature"

    val ERROR = Exception("Luba Luba Dub Dub")

    const val EXP_1 = "exp1"
    const val EXP_2 = "exp2"
    const val EXP_LANGUAGE = "lang_android-en-add_credit"

    const val LANG_EN = "en"
    const val LANG_ML = "ml"

    const val VAR_1 = "var1"
    const val VAR_2 = "var2"

    const val EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN = "ui_experiment-all-activation_and_add_txn"
    const val EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN_VARIANT = "V2"

    val TEST_AB_PROFILE = Profile(
        features = mapOf(
            FEATURE_1 to true,
            FEATURE_2 to false,
            FEATURE_3 to true
        ),
        experiments = mapOf(
            EXP_1 to Experiment(
                name = EXP_1,
                variant = VAR_1,
                status = 2,
                vars = mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3")
            ),

            EXP_2 to Experiment(
                name = EXP_2,
                variant = VAR_2,
                status = 2,
                vars = mapOf()
            ),

            EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN to Experiment(
                name = EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN,
                variant = EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN_VARIANT,
                status = 2,
                vars = mapOf()
            )
        )
    )

    val TEST_AB_PROFILE_WITHOUT_EXP_UI_EXPERIMENT_ALL_ACTIVATION_AND_ADD_TXN = Profile(
        features = mapOf(
            FEATURE_1 to true
        ),
        experiments = mapOf(
            EXP_1 to Experiment(
                name = EXP_1,
                variant = VAR_1,
                status = 2,
                vars = mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3")
            )
        )
    )

    val TEST_AB_PROFILE_WITH_LANG_EXP = Profile(
        features = mapOf(
            FEATURE_1 to true
        ),
        experiments = mapOf(
            EXP_LANGUAGE to Experiment(
                name = EXP_LANGUAGE,
                variant = VAR_1,
                status = 1,
                vars = mapOf("add_credit" to "Add Credit", "add_payment" to "Add Payment")
            )
        )
    )
}
