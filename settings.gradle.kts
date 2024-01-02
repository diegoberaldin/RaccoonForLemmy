pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Raccoon_for_Lemmy"
include(":androidApp")
include(":shared")
include(":resources")

include(":core:api")
include(":core:appearance")
include(":core:architecture")
include(":core:commonui:components")
include(":core:commonui:detailopener-api")
include(":core:commonui:detailopener-impl")
include(":core:commonui:lemmyui")
include(":core:commonui:modals")
include(":core:md")
include(":core:navigation")
include(":core:notifications")
include(":core:persistence")
include(":core:preferences")
include(":core:utils")

include(":domain:identity")
include(":domain:inbox")
include(":domain:lemmy")
include(":domain:lemmy:data")
include(":domain:lemmy:repository")

include(":feature:home")
include(":feature:inbox")
include(":feature:profile")
include(":feature:search")
include(":feature:settings")

include(":unit:about")
include(":unit:ban")
include(":unit:chat")
include(":unit:communitydetail")
include(":unit:communityinfo")
include(":unit:createcomment")
include(":unit:createpost")
include(":unit:createreport")
include(":unit:drawer")
include(":unit:instanceinfo")
include(":unit:login")
include(":unit:manageaccounts")
include(":unit:managesubscriptions")
include(":unit:multicommunity")
include(":unit:myaccount")
include(":unit:postdetail")
include(":unit:postlist")
include(":unit:remove")
include(":unit:reportlist")
include(":unit:saveditems")
include(":unit:selectcommunity")
include(":unit:userdetail")
include(":unit:web")
include(":unit:zoomableimage")
include(":unit:replies")
include(":unit:mentions")
include(":unit:messages")
include(":unit:modlog")
include(":unit:userinfo")
include(":unit:rawcontent")
include(":unit:accountsettings")
include(":unit:manageban")
