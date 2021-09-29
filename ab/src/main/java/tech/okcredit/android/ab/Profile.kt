package tech.okcredit.android.ab

import tech.okcredit.android.ab.server.Experiment

data class Profile(
    val features: Map<String, Boolean> = hashMapOf(),
    val experiments: Map<String, Experiment> = hashMapOf()
)
