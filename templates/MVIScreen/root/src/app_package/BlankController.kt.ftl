package ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}

import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.views.*
import com.airbnb.epoxy.EpoxyController
import javax.inject.Inject

class ${featureName}Controller @Inject
constructor(private val screen: ${featureName}Screen) : AsyncEpoxyController() {
    private lateinit var state: ${featureName}Contract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: ${featureName}Contract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        ${featureName?lower_case}View {
            id("${featureName?lower_case}View")
            //property("sample_value")
            listener(screen)
        }
    }
}
