package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReportListViewModel(
    private val communityId: Long?,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticFeedback: HapticFeedback,
    private val notificationCenter: NotificationCenter,
) : ReportListMviModel,
    DefaultMviModel<ReportListMviModel.Intent, ReportListMviModel.UiState, ReportListMviModel.Effect>(
        initialState = ReportListMviModel.UiState(),
    ) {

    private val currentPage = mutableMapOf<ReportListSection, Int>()

    init {
        screenModelScope.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        swipeActionsEnabled = settings.enableSwipeActions,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeReportListType::class)
                .onEach { evt ->
                    changeUnresolvedOnly(evt.unresolvedOnly)
                }.launchIn(this)

            if (uiState.value.postReports.isEmpty()) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: ReportListMviModel.Intent) {
        when (intent) {
            is ReportListMviModel.Intent.ChangeSection -> changeSection(intent.value)
            is ReportListMviModel.Intent.ChangeUnresolvedOnly -> changeUnresolvedOnly(intent.value)
            ReportListMviModel.Intent.Refresh -> refresh()
            ReportListMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }

            is ReportListMviModel.Intent.ResolveComment -> uiState.value.commentReports
                .firstOrNull { it.id == intent.id }?.also {
                    resolve(it)
                }

            is ReportListMviModel.Intent.ResolvePost -> uiState.value.postReports
                .firstOrNull { it.id == intent.id }?.also {
                    resolve(it)
                }

            ReportListMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
        }
    }

    private fun changeSection(section: ReportListSection) {
        updateState {
            it.copy(
                section = section,
            )
        }
    }

    private fun changeUnresolvedOnly(value: Boolean) {
        updateState {
            it.copy(unresolvedOnly = value)
        }
        refresh(initial = true)
    }

    private fun refresh(initial: Boolean = false) {
        currentPage[ReportListSection.Posts] = 1
        currentPage[ReportListSection.Comments] = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
                loading = false,
            )
        }
        screenModelScope.launch {
            loadNextPage()
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value.orEmpty()
        val refreshing = currentState.refreshing
        val section = currentState.section
        val unresolvedOnly = currentState.unresolvedOnly
        if (section == ReportListSection.Posts) {
            val page = currentPage[ReportListSection.Posts] ?: 1
            coroutineScope {
                val itemList = async {
                    postRepository.getReports(
                        auth = auth,
                        communityId = communityId,
                        page = page,
                        unresolvedOnly = unresolvedOnly,
                    )
                }.await()
                val commentReports = async {
                    if (page == 1 && (currentState.commentReports.isEmpty() || refreshing)) {
                        // this is needed because otherwise on first selector change
                        // the lazy column scrolls back to top (it must have an empty data set)
                        commentRepository.getReports(
                            auth = auth,
                            communityId = communityId,
                            page = 1,
                            unresolvedOnly = unresolvedOnly,
                        ).orEmpty()
                    } else {
                        currentState.commentReports
                    }
                }.await()
                updateState {
                    val postReports = if (refreshing) {
                        itemList.orEmpty()
                    } else {
                        it.postReports + itemList.orEmpty()
                    }
                    it.copy(
                        postReports = postReports,
                        commentReports = commentReports,
                        loading = false,
                        canFetchMore = itemList?.isEmpty() != true,
                        refreshing = false,
                        initial = false,
                    )
                }
                if (!itemList.isNullOrEmpty()) {
                    currentPage[ReportListSection.Posts] = page + 1
                }
            }
        } else {
            val page = currentPage[ReportListSection.Comments] ?: 1
            val itemList = commentRepository.getReports(
                auth = auth,
                communityId = communityId,
                page = page,
                unresolvedOnly = unresolvedOnly,
            )

            updateState {
                val commentReports = if (refreshing) {
                    itemList.orEmpty()
                } else {
                    it.commentReports + itemList.orEmpty()
                }
                it.copy(
                    commentReports = commentReports,
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                    initial = false,
                )
            }
            if (!itemList.isNullOrEmpty()) {
                currentPage[ReportListSection.Comments] = page + 1
            }
        }
    }

    private fun resolve(report: PostReportModel) {
        screenModelScope.launch {
            updateState { it.copy(asyncInProgress = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val newReport = postRepository.resolveReport(
                reportId = report.id,
                auth = auth,
                resolved = !report.resolved
            )
            updateState { it.copy(asyncInProgress = false) }
            if (newReport != null) {
                if (uiState.value.unresolvedOnly && newReport.resolved) {
                    handleReporDelete(newReport)
                } else {
                    handleReportUpdate(newReport)
                }
            }
        }
    }

    private fun resolve(report: CommentReportModel) {
        screenModelScope.launch {
            updateState { it.copy(asyncInProgress = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val newReport = commentRepository.resolveReport(
                reportId = report.id,
                auth = auth,
                resolved = !report.resolved
            )
            updateState { it.copy(asyncInProgress = false) }
            if (newReport != null) {
                if (uiState.value.unresolvedOnly && newReport.resolved) {
                    handleReporDelete(newReport)
                } else {
                    handleReportUpdate(newReport)
                }
            }
        }
    }

    private fun handleReportUpdate(report: PostReportModel) {
        updateState {
            it.copy(
                postReports = it.postReports.map { r ->
                    if (r.id == report.id) {
                        report
                    } else {
                        r
                    }
                }
            )
        }
    }

    private fun handleReportUpdate(report: CommentReportModel) {
        updateState {
            it.copy(
                commentReports = it.commentReports.map { r ->
                    if (r.id == report.id) {
                        report
                    } else {
                        r
                    }
                }
            )
        }
    }

    private fun handleReporDelete(report: PostReportModel) {
        updateState {
            it.copy(
                postReports = it.postReports.filter { r -> r.id != report.id }
            )
        }
    }

    private fun handleReporDelete(report: CommentReportModel) {
        updateState {
            it.copy(
                commentReports = it.commentReports.filter { r -> r.id != report.id }
            )
        }
    }
}
