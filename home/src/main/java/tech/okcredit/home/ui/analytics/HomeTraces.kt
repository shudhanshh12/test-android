package tech.okcredit.home.ui.analytics

/**
 * Names for custom traces and custom metrics must meet the following requirements:
 * 1. no leading or trailing whitespace
 * 2. no leading underscore
 * 3. max length is 32 characters
 **/

object HomeTraces {
    const val OnViewCreated_Home = "OnViewCreatedHome"
    const val OnCreateView_HomeCustomerTab = "OnCreateViewHomeCustomerTab"
    const val OnViewCreated_HomeCustomerTab = "OnViewCreatedHomeCustomerTab"

    const val HomeSearchFiltering = "HomeSearchFiltering"
    const val HomeSearchUseCase = "HomeSearchUseCase"

    const val RenderSearch = "RenderSearch"
    const val RENDER_HOME = "RenderHome"
    const val RENDER_HOME_CUSTOMER_TAB = "RenderHomeCustomerTab"
    const val RENDER_HOME_SUPPLIER_TAB = "RenderHomeSupplierTab"
}
