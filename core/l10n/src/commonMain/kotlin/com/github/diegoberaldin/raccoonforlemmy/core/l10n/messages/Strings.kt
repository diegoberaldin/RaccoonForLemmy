package com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.lyricist.LanguageTag
import cafe.adriel.lyricist.Lyricist
import cafe.adriel.lyricist.ProvideStrings

interface Strings {
    val actionBackToTop: String
    val actionChat: String
    val actionClearRead: String
    val actionCreatePost: String
    val actionReply: String
    val actionActivateZombieMode: String
    val actionDeactivateZombieMode: String
    val buttonClose: String
    val buttonConfirm: String
    val buttonLoad: String
    val buttonReset: String
    val buttonRetry: String
    val commentActionDelete: String
    val communityDetailBlock: String
    val communityDetailBlockInstance: String
    val communityDetailInfo: String
    val communityDetailInstanceInfo: String
    val communityInfoComments: String
    val communityInfoDailyActiveUsers: String
    val communityInfoMonthlyActiveUsers: String
    val communityInfoPosts: String
    val communityInfoSubscribers: String
    val communityInfoWeeklyActiveUsers: String
    val createCommentBody: String
    val createCommentTitle: String
    val createPostBody: String
    val createPostCommunity: String
    val createPostCrossPostText: String
    val createPostName: String
    val createPostNsfw: String
    val createPostTabEditor: String
    val createPostTabPreview: String
    val createPostTitle: String
    val createPostUrl: String
    val createReportPlaceholder: String
    val createReportTitleComment: String
    val createReportTitlePost: String
    val dialogRawContentText: String
    val dialogRawContentTitle: String
    val dialogRawContentUrl: String
    val dialogTitleChangeInstance: String
    val dialogTitleRawContent: String
    val dialogTitleSelectCommunity: String
    val editCommentTitle: String
    val editPostTitle: String
    val exploreResultTypeAll: String
    val exploreResultTypeComments: String
    val exploreResultTypeCommunities: String
    val exploreResultTypePosts: String
    val exploreResultTypeUsers: String
    val exploreSearchPlaceholder: String
    val homeInstanceVia: String
    val homeListingTitle: String
    val homeListingTypeAll: String
    val homeListingTypeLocal: String
    val homeListingTypeSubscribed: String
    val homeSortTitle: String
    val homeSortTypeActive: String
    val homeSortTypeControversial: String
    val homeSortTypeHot: String
    val homeSortTypeMostComments: String
    val homeSortTypeNew: String
    val homeSortTypeNewComments: String
    val homeSortTypeOld: String
    val homeSortTypeScaled: String
    val homeSortTypeTop: String
    val homeSortTypeTop12Hours: String
    val homeSortTypeTop12HoursShort: String
    val homeSortTypeTop6Hours: String
    val homeSortTypeTop6HoursShort: String
    val homeSortTypeTopDay: String
    val homeSortTypeTopDayShort: String
    val homeSortTypeTopHour: String
    val homeSortTypeTopHourShort: String
    val homeSortTypeTopMonth: String
    val homeSortTypeTopMonthShort: String
    val homeSortTypeTopWeek: String
    val homeSortTypeTopWeekShort: String
    val homeSortTypeTopYear: String
    val homeSortTypeTopYearShort: String
    val inboxChatMessage: String
    val inboxItemMention: String
    val inboxItemReplyComment: String
    val inboxItemReplyPost: String
    val inboxListingTypeAll: String
    val inboxListingTypeTitle: String
    val inboxListingTypeUnread: String
    val inboxNotLoggedMessage: String
    val inboxSectionMentions: String
    val inboxSectionMessages: String
    val inboxSectionReplies: String
    val instanceDetailCommunities: String
    val instanceDetailTitle: String
    val lang: String
    val loginFieldInstanceName: String
    val loginFieldLabelOptional: String
    val loginFieldPassword: String
    val loginFieldToken: String
    val loginFieldUserName: String
    val manageAccountsButtonAdd: String
    val manageAccountsTitle: String
    val manageSubscriptionsHeaderMulticommunities: String
    val manageSubscriptionsHeaderSubscriptions: String
    val messageEmptyComments: String
    val messageEmptyList: String
    val messageErrorLoadingComments: String
    val messageGenericError: String
    val messageImageLoadingError: String
    val messageInvalidField: String
    val messageMissingField: String
    val messageOperationSuccessful: String
    val multiCommunityEditorCommunities: String
    val multiCommunityEditorIcon: String
    val multiCommunityEditorName: String
    val multiCommunityEditorTitle: String
    val navigationDrawerAnonymous: String
    val navigationDrawerTitleBookmarks: String
    val navigationDrawerTitleSubscriptions: String
    val navigationHome: String
    val navigationInbox: String
    val navigationProfile: String
    val navigationSearch: String
    val navigationSettings: String
    val postActionCrossPost: String
    val postActionEdit: String
    val postActionHide: String
    val postActionReport: String
    val postActionSeeRaw: String
    val postActionShare: String
    val postDetailCrossPosts: String
    val postDetailLoadMoreComments: String
    val postHourShort: String
    val postMinuteShort: String
    val postSecondShort: String
    val profileButtonLogin: String
    val profileDayShort: String
    val profileMillionShort: String
    val profileMonthShort: String
    val profileNotLoggedMessage: String
    val profileSectionComments: String
    val profileSectionPosts: String
    val profileThousandShort: String
    val profileYearShort: String
    val settingsAbout: String
    val settingsAboutAppVersion: String
    val settingsAboutChangelog: String
    val settingsAboutReportGithub: String
    val settingsAboutReportEmail: String
    val settingsAboutViewGithub: String
    val settingsAboutViewLemmy: String
    val settingsAutoExpandComments: String
    val settingsAutoLoadImages: String
    val settingsBlurNsfw: String
    val settingsColorAquamarine: String
    val settingsColorBanana: String
    val settingsColorBlue: String
    val settingsColorCustom: String
    val settingsColorDialogAlpha: String
    val settingsColorDialogBlue: String
    val settingsColorDialogGreen: String
    val settingsColorDialogRed: String
    val settingsColorDialogTitle: String
    val settingsColorGray: String
    val settingsColorGreen: String
    val settingsColorOrange: String
    val settingsColorPink: String
    val settingsColorPurple: String
    val settingsColorRed: String
    val settingsColorWhite: String
    val settingsContentFontLarge: String
    val settingsContentFontLarger: String
    val settingsContentFontLargest: String
    val settingsContentFontNormal: String
    val settingsContentFontScale: String
    val settingsContentFontSmall: String
    val settingsContentFontSmaller: String
    val settingsContentFontSmallest: String
    val settingsCustomSeedColor: String
    val settingsDefaultCommentSortType: String
    val settingsDefaultListingType: String
    val settingsDefaultPostSortType: String
    val settingsDownvoteColor: String
    val settingsDynamicColors: String
    val settingsEnableCrashReport: String
    val settingsEnableDoubleTap: String
    val settingsEnableSwipeActions: String
    val settingsFullHeightImages: String
    val settingsIncludeNsfw: String
    val settingsLanguage: String
    val settingsNavigationBarTitlesVisible: String
    val settingsOpenUrlExternal: String
    val settingsPointsShort: String
    val settingsPostLayout: String
    val settingsPostLayoutCard: String
    val settingsPostLayoutCompact: String
    val settingsPostLayoutFull: String
    val settingsSectionAppearance: String
    val settingsAdvanced: String
    val settingsSectionDebug: String
    val settingsSectionGeneral: String
    val settingsSectionNsfw: String
    val settingsThemeBlack: String
    val settingsThemeDark: String
    val settingsThemeLight: String
    val settingsUiFontFamily: String
    val settingsUiFontScale: String
    val settingsUiTheme: String
    val settingsUpvoteColor: String
    val settingsHideNavigationBar: String
    val settingsZombieModeInterval: String
    val settingsZombieModeScrollAmount: String
    val settingsMarkAsReadWhileScrolling: String
    val actionQuote: String
    val modActionAllow: String
    val modActionBan: String
    val modActionOpenReports: String
    val modActionMarkAsFeatured: String
    val modActionUnmarkAsFeatured: String
    val modActionLock: String
    val modActionUnlock: String
    val modActionRemove: String
    val modActionMarkAsDistinguished: String
    val modActionUnmarkAsDistinguished: String
    val reportListTitle: String
    val reportListTypeTitle: String
    val reportListTypeAll: String
    val reportListTypeUnresolved: String
    val reportActionResolve: String
    val reportActionUnresolve: String
    val sidebarNotLoggedMessage: String
    val settingsDefaultInboxType: String
    val modActionAddMod: String
    val modActionRemoveMod: String
    val settingsVoteFormat: String
    val settingsVoteFormatAggregated: String
    val settingsVoteFormatSeparated: String
    val settingsVoteFormatPercentage: String
    val settingsFontFamilyDefault: String
    val postReplySourceAccount: String
    val settingsCommentBarTheme: String
    val settingsCommentBarThemeBlue: String
    val settingsCommentBarThemeGreen: String
    val settingsCommentBarThemeRed: String
    val settingsCommentBarThemeMulti: String
    val messageConfirmExit: String
    val communityActionUnsubscribe: String
    val settingsSearchPostsTitleOnly: String
    val settingsContentFontFamily: String
    val communityInfoModerators: String
    val communityActionAddFavorite: String
    val communityActionRemoveFavorite: String
    val communityActionViewModlog: String
    val modlogTitle: String
    val modlogItemModAdded: String
    val modlogItemModRemoved: String
    val modlogItemUserBanned: String
    val modlogItemUserUnbanned: String
    val modlogItemPostFeatured: String
    val modlogItemPostUnfeatured: String
    val modlogItemPostLocked: String
    val modlogItemPostUnlocked: String
    val modlogItemPostRemoved: String
    val modlogItemPostRestored: String
    val modlogItemCommentRemoved: String
    val modlogItemCommentRestored: String
    val modlogItemCommunityTransfer: String
    val blockActionUser: String
    val blockActionCommunity: String
    val userDetailInfo: String
    val userInfoModerates: String
    val userInfoAdmin: String
    val settingsReplyColor: String
    val settingsSectionAccount: String
    val settingsWebPreferences: String
    val settingsWebHeaderPersonal: String
    val settingsWebHeaderContents: String
    val settingsWebHeaderNotifications: String
    val settingsWebAvatar: String
    val settingsWebBanner: String
    val settingsWebBio: String
    val settingsWebBot: String
    val settingsWebDisplayName: String
    val settingsWebMatrix: String
    val settingsWebEmail: String
    val settingsWebShowBot: String
    val settingsWebShowNsfw: String
    val settingsWebShowRead: String
    val settingsWebEmailNotifications: String
    val settingsManageBan: String
    val settingsManageBanActionUnban: String
    val settingsManageBanSectionInstances: String
    val settingsEdgeToEdge: String
    val settingsPostBodyMaxLines: String
    val settingsPostBodyMaxLinesUnlimited: String
    val messageContentRemoved: String
    val postListLoadMorePosts: String
    val settingsInfiniteScrollDisabled: String
    val dialogTitleAddInstance: String
    val settingsSaveColor: String
    val settingsConfigureSwipeActions: String
    val actionUpvote: String
    val actionDownvote: String
    val actionSave: String
    val actionToggleRead: String
    val configureActionsSideStart: String
    val configureActionsSideEnd: String
    val selectActionTitle: String
    val buttonAdd: String
    val barThemeOpaque: String
    val barThemeTransparent: String
    val settingsBarTheme: String
    val settingsColorsAndFonts: String
    val settingsAboutViewGooglePlay: String
    val settingsUserManual: String
    val settingsShowScores: String
    val settingsVoteFormatHidden: String
    val settingsCommentBarThickness: String
    val settingsPreferUserNicknames: String
    val messageVideoNsfw: String
    val settingsTitleFontScale: String
    val settingsCommentFontScale: String
    val settingsAncillaryFontScale: String
    val settingsConfigureContent: String
    val settingsConfigureText: String
    val settingsConfigureCustomizations: String
    val navigationDrawerTitleDrafts: String
    val moderatorZoneTitle: String
    val moderatorZoneActionContents: String
    val messageAuthIssue: String
    val banReasonPlaceholder: String
    val banItemPermanent: String
    val banItemRemoveData: String
    val banItemDurationDays: String
    val messageUnsavedChanges: String
    val buttonNoStay: String
    val buttonYesQuit: String
    val settingsItemImageSourcePath: String
    val settingsSubtitleImageSourcePath: String
    val settingsTitleDisplay: String
    val settingsTitleReading: String
    val settingsTitlePictures: String
    val settingsTitleExperimental: String
    val communitySetCustomSort: String
    val settingsDefaultExploreType: String
    val actionSearchInCommunity: String
    val actionExitSearch: String
    val beta: String
    val actionCopyClipboard: String
    val copyTitle: String
    val copyText: String
    val copyBoth: String
    val profileUpvotesDownvotes: String
    val filteredContentsType: String
    val actionSearchInComments: String
    val advancedSettingsDefaultLanguage: String
    val undetermined: String
    val exploreResultTypeTitle: String
    val communityActionEdit: String
    val editCommunityHeaderTextual: String
    val editCommunityItemSidebar: String
    val editCommunityItemPostingRestrictedToMods: String
    val messageAreYouSure: String
    val buttonCancel: String
    val shareModeUrl: String
    val shareModeFile: String
    val modlogItemCommunityPurged: String
    val modlogItemCommentPurged: String
    val modlogItemPersonPurged: String
    val modlogItemPostPurged: String
    val modlogItemHidden: String
    val modlogItemUnhidden: String
    val settingsAboutLicences: String
    val settingsInboxBackgroundCheckPeriod: String
    val never: String
    val settingsAppIcon: String
    val requiresRestart: String
    val appIconDefault: String
    val appIconAlt1: String
    val settingsFadeReadPosts: String
    val settingsShowUnreadComments: String
    val actionLogout: String
    val settingsImport: String
    val settingsExport: String
    val settingsEnableButtonsToScrollBetweenComments: String
    val settingsUrlOpeningModeInternal: String
    val settingsUrlOpeningModeExternal: String
    val settingsUrlOpeningModeCustomTabs: String
    val settingsFullWidthImages: String
    val contentScaleFit: String
    val contentScaleFillWidth: String
    val contentScaleFillHeight: String
    val settingsCommentIndentAmount: String
    val postActionUnhide: String
    val adminActionPurge: String
    val adminActionMarkAsFeatured: String
    val adminActionUnmarkAsFeatured: String
    val communitySetPreferredLanguage: String
    val appIconClassical: String
    val settingsAboutAcknowledgements: String
    val actionCreateCommunity: String
    val editCommunityItemVisibility: String
    val communityVisibilityLocalOnly: String
    val communityVisibilityPublic: String
    val noticeCommunityLocalOnly: String
    val noticeBannedUser: String
    val settingsHiddenPosts: String
    val settingsMediaList: String
    val settingsEnableToggleFavoriteInNavDrawer: String
}

object Locales {
    const val FR = "fr"
    const val SQ = "sq"
    const val RO = "ro"
    const val GA = "ga"
    const val EN = "en"
    const val FI = "fi"
    const val ET = "et"
    const val LT = "lt"
    const val LV = "lv"
    const val PT_BR = "pt_BR"
    const val SR = "sr"
    const val HR = "hr"
    const val TR = "tr"
    const val NL = "nl"
    const val CS = "cs"
    const val BG = "bg"
    const val EO = "eo"
    const val DE = "de"
    const val HU = "hu"
    const val SK = "sk"
    const val SL = "sl"
    const val PL = "pl"
    const val IT = "it"
    const val UK = "uk"
    const val SE = "se"
    const val EL = "el"
    const val RU = "ru"
    const val ES = "es"
    const val TOK = "tok"
    const val PT = "pt"
    const val AR = "ar"
    const val DA = "da"
    const val NO = "no"
    const val MT = "mt"
    const val ZH_TW = "zh_TW"
    const val ZH_HK = "zh_HK"
}

internal val localizableStrings: Map<LanguageTag, Strings> =
    mapOf(
        Locales.FR to FrStrings,
        Locales.SQ to SqStrings,
        Locales.RO to RoStrings,
        Locales.GA to GaStrings,
        Locales.EN to EnStrings,
        Locales.FI to FiStrings,
        Locales.ET to EtStrings,
        Locales.LT to LtStrings,
        Locales.LV to LvStrings,
        Locales.PT_BR to PtBrStrings,
        Locales.SR to SrStrings,
        Locales.HR to HrStrings,
        Locales.TR to TrStrings,
        Locales.NL to NlStrings,
        Locales.CS to CsStrings,
        Locales.BG to BgStrings,
        Locales.EO to EoStrings,
        Locales.DE to DeStrings,
        Locales.HU to HuStrings,
        Locales.SK to SkStrings,
        Locales.SL to SlStrings,
        Locales.PL to PlStrings,
        Locales.IT to ItStrings,
        Locales.UK to UkStrings,
        Locales.SE to SeStrings,
        Locales.EL to ElStrings,
        Locales.RU to RuStrings,
        Locales.ES to EsStrings,
        Locales.TOK to TokStrings,
        Locales.PT to PtStrings,
        Locales.AR to ArStrings,
        Locales.DA to DaStrings,
        Locales.NO to NoStrings,
        Locales.MT to MtStrings,
        Locales.ZH_TW to ZhTwStrings,
        Locales.ZH_HK to ZhHkStrings,
    )

val LocalStrings: ProvidableCompositionLocal<Strings> =
    staticCompositionLocalOf { EnStrings }

/*
@Composable
fun rememberXmlStrings(
    defaultLanguageTag: LanguageTag = Locales.En,
    currentLanguageTag: LanguageTag = Locale.current.toLanguageTag(),
): Lyricist<XmlStrings> = rememberStrings(xmlStrings, defaultLanguageTag, currentLanguageTag)
*/

@Composable
fun ProvideStrings(
    lyricist: Lyricist<Strings>,
    content: @Composable () -> Unit,
) {
    ProvideStrings(lyricist, LocalStrings, content)
}
