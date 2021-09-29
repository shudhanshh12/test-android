include(":storesms")
include(":business_health_dashboard:contract")
include(":business_health_dashboard")
include(":voice_first:contract")
include(":voice_first")
include(":okstream:contract")
include(":okstream:sdk")
include(":okstream")
include(":cashback:contract")
include(":cashback")
include(":secure_keys")
include(":user_migration:contract")
include(":app:app_contract")
include(":app")
include(":accounting")
include(":accounting:contract")
include(":bill_management:contract")
include(":camera:camera_contract")
include(":bill_management:sdk")
include(":bill_management:ui")
include(":bill_management")
include(":customer:contract")
include(":supplier:contract")
include(":applock:contract")
//include(":lending:contract")
include(":business:contract")
include(":gamification")
include(":gamification:ipl")
//include(":lending")
//include(":lending:contract")
include(":web_features")
include(":supplier")
include(":device:installedpackges")
include(":home:contract")
include(":rewards:contract")
//include(":storesms")
include(":payment")
include(":payment:contract")
include(":feature_help:sdk")
include(":feature_help:ui")
include("feature_help:contract")
include(":ab")
include(":auth")
include(":backend")
include(":base")
include(":frontend")
include(":resources")
include(":fileupload")
include(":analytics")
include(":rewards")
include(":camera")
include(":suppliercredit")
include(":suppliercredit:contract")
include(":business")
include(":device")
include(":referral")
include(":shared")
include(":communication")
include(":referral:contract")
include(":frontend:contract")
include(":frontend:contract")
include(":expense")
include(":communication")
include(":contacts")
include(":contacts:contract")
include(":home")
include(":dynamicview")
include(":dynamicview:contract")
include(":collection_module")
include(":collection_module:collection_ui")
include(":collection_module:collection_sdk")
include(":collection_module:contract")
include(":sales_module")
include(":sales_module:sales_sdk")
include(":sales_module:sales_ui")
include(":onboarding")
include(":onboarding:contract")
include(":backend:contract")
include(":account_chat:account_chat_sdk")
include(":account_chat:account_chat_ui")
include(":account_chat:account_chat_contract")
include(":communication_inappnotification")
include(":communication_inappnotification:contract")
include(":web")
include(":web:contract")
include(":user_migration")
include(":customer:customer_ui")
include(":applock")
include(":accounting_core")
include(":accounting_core:contract")
include(":user-stories")
include(":user-stories:contract")
include(":ok-doc")
include(":ok-doc:contract")
include(":individual")
include(":individual:contract")

// Keep this at the end of settings.gradle
rootProject.children.forEach { module ->
    module.buildFileName = "${module.name}.gradle"
    module.children.forEach { submodule -> submodule.buildFileName = "${module.name}.${submodule.name}.gradle" }
}