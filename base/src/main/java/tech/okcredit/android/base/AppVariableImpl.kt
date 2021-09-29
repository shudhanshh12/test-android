package tech.okcredit.android.base

interface AppVariable {
    var appCreated: Boolean
}

object AppVariableImpl : AppVariable {

    private var onCreateCalled: Boolean = false

    override var appCreated: Boolean
        get() {
            return onCreateCalled
        }
        set(value) {
            onCreateCalled = value
        }
}
