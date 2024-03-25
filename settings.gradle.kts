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

include(":core:api")
include(":core:appearance")
include(":core:architecture")
include(":core:commonui:components")
include(":core:commonui:detailopener-api")
include(":core:commonui:detailopener-impl")
include(":core:commonui:lemmyui")
include(":core:commonui:modals")
include(":core:l10n")
include(":core:markdown")
include(":core:navigation")
include(":core:notifications")
include(":core:persistence")
include(":core:preferences")
include(":core:resources")
include(":core:testutils")
include(":core:utils")

include(":domain:identity")
include(":domain:inbox")
include(":domain:lemmy:data")
include(":domain:lemmy:repository")

include(":feature:home")
include(":feature:inbox")
include(":feature:profile")
include(":feature:search")
include(":feature:settings")

include(":unit:about")
include(":unit:accountsettings")
include(":unit:ban")
include(":unit:chat")
include(":unit:choosecolor")
include(":unit:choosefont")
include(":unit:communitydetail")
include(":unit:communityinfo")
include(":unit:configurecontentview")
include(":unit:configureswipeactions")
include(":unit:createcomment")
include(":unit:createpost")
include(":unit:createreport")
include(":unit:drafts")
include(":unit:drawer")
include(":unit:editcommunity")
include(":unit:explore")
include(":unit:filteredcontents")
include(":unit:instanceinfo")
include(":unit:login")
include(":unit:manageaccounts")
include(":unit:manageban")
include(":unit:managesubscriptions")
include(":unit:mentions")
include(":unit:messages")
include(":unit:modlog")
include(":unit:multicommunity")
include(":unit:myaccount")
include(":unit:postdetail")
include(":unit:postlist")
include(":unit:rawcontent")
include(":unit:remove")
include(":unit:replies")
include(":unit:reportlist")
include(":unit:saveditems")
include(":unit:selectcommunity")
include(":unit:selectinstance")
include(":unit:userdetail")
include(":unit:userinfo")
include(":unit:web")
include(":unit:zoomableimage")
