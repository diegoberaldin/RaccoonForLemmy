package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalSite(
    @SerialName("id") val id: LocalSiteId,
    @SerialName("site_id") val siteId: SiteId,
    @SerialName("site_setup") val siteSetup: Boolean,
    @SerialName("enable_downvotes") val enableDownvotes: Boolean,
    @SerialName("enable_nsfw") val enableNsfw: Boolean,
    @SerialName("community_creation_admin_only") val communityCreationAdminOnly: Boolean,
    @SerialName("require_email_verification") val requireEmailVerification: Boolean,
    @SerialName("application_question") val applicationQuestion: String? = null,
    @SerialName("private_instance") val privateInstance: Boolean,
    @SerialName("default_theme") val defaultTheme: String,
    @SerialName("default_post_listing_type") val defaultPostListingType: ListingType,
    @SerialName("legal_information") val legalInformation: String? = null,
    @SerialName("hide_modlog_mod_names") val hideModlogModNames: Boolean,
    @SerialName("application_email_admins") val applicationEmailAdmins: Boolean,
    @SerialName("slur_filter_regex") val slurFilterRegex: String? = null,
    @SerialName("actor_name_max_length") val actorNameMaxLength: Int,
    @SerialName("federation_enabled") val federationEnabled: Boolean,
    @SerialName("federation_worker_count") val federationWorkerCount: Int? = null,
    @SerialName("captcha_enabled") val captchaEnabled: Boolean,
    @SerialName("captcha_difficulty") val captchaDifficulty: String,
    @SerialName("published") val published: String,
    @SerialName("updated") val updated: String? = null,
    @SerialName("registration_mode") val registrationMode: RegistrationMode,
    @SerialName("reports_email_admins") val reportsEmailAdmins: Boolean,
)
