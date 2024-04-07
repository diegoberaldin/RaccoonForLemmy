package com.github.diegoberaldin.raccoonforlemmy.unit.licences

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.models.LicenceItem
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.models.LicenceItemType

class LicencesViewModel : LicencesMviModel,
    DefaultMviModel<LicencesMviModel.Intent, LicencesMviModel.State, LicencesMviModel.Effect>(
        initialState = LicencesMviModel.State()
    ) {

    init {
        populate()
    }

    private fun populate() {
        updateState {
            it.copy(
                items = buildList {
                    this += LicenceItem(
                        type = LicenceItemType.Resource,
                        title = "Charis SIL, Comfortaa, Noto Sans, Poppins",
                        subtitle = "Fonts used in the app are released under the Open Font Library (OFL)",
                        url = LicenceUrls.OFL,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Coil",
                        subtitle = "An image loading library for Android",
                        url = LicenceUrls.COIL,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Resource,
                        title = "GitHub logo",
                        url = LicenceUrls.GITHUB_LOGO,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Koin",
                        subtitle = "A pragmatic lightweight dependency injection framework",
                        url = LicenceUrls.KOIN,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "KotlinCrypto",
                        subtitle = "Cryptographic components for Kotlin Multiplatform",
                        url = LicenceUrls.KOTLIN_CRYPTO,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Ktor",
                        subtitle = "An asynchronous framework for creating microservices, web applications and more",
                        url = LicenceUrls.KTOR,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Ktorfit",
                        subtitle = "A HTTP client/Kotlin Symbol Processor for Kotlin Multiplatform",
                        url = LicenceUrls.KTORFIT,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Resource,
                        title = "Lemmy logo",
                        url = LicenceUrls.LEMMY_LOGO,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Lyricist",
                        subtitle = "The missing I18N and L10N multiplatform library for Jetpack Compose",
                        url = LicenceUrls.LYRICIST,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Resource,
                        title = "Material Design Icons",
                        subtitle = "A set of icons by Google",
                        url = LicenceUrls.MATERIAL_ICONS,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "MaterialKolor",
                        subtitle = "A Compose Multiplatform library for creating dynamic Material Design 3 color palettes",
                        url = LicenceUrls.MATERIAL_KOLOR,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Resource,
                        title = "Monochrome icon",
                        url = LicenceUrls.MONOCHROME_ICON,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Multiplatform Markdown Renderer",
                        subtitle = "A Kotlin Multiplatform Markdown Renderer powered by Compose Multiplatform",
                        url = LicenceUrls.MULTIPLATFORM_MARKDOWN_RENDERER,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Multiplatform Settings",
                        subtitle = "A Kotlin library for Multiplatform apps, so that common code can persist key-value data",
                        url = LicenceUrls.SETTINGS,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Reorderable",
                        subtitle = "A simple library that allows you to reorder items in Jetpack Compose with drag and drop",
                        url = LicenceUrls.REORDERABLE,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "SQLCipher",
                        subtitle = "A library that provides transparent, secure 256-bit AES encryption of SQLite database files",
                        url = LicenceUrls.SQLCIPHER,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "SQLDelight",
                        subtitle = "SQLDelight generates typesafe Kotlin APIs from your SQL statements",
                        url = LicenceUrls.SQLDELIGHT,
                    )
                    this += LicenceItem(
                        type = LicenceItemType.Library,
                        title = "Voyager",
                        subtitle = "A multiplatform navigation library built for, and seamlessly integrated with, Jetpack Compose",
                        url = LicenceUrls.VOYAGER,
                    )
                }
            )
        }
    }
}
