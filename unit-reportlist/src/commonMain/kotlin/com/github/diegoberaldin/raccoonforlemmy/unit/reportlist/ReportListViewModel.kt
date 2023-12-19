package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReportListViewModel(
    private val communityId: Int,
    private val mvi: DefaultMviModel<ReportListMviModel.Intent, ReportListMviModel.UiState, ReportListMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticFeedback: HapticFeedback,
    private val notificationCenter: NotificationCenter,
) : ReportListMviModel,
    MviModel<ReportListMviModel.Intent, ReportListMviModel.UiState, ReportListMviModel.Effect> by mvi {

    private var currentPage = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
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
            ReportListMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            is ReportListMviModel.Intent.ResolveComment -> mvi.uiState.value.commentReports
                .firstOrNull { it.id == intent.id }?.also {
                    resolve(it)
                }

            is ReportListMviModel.Intent.ResolvePost -> mvi.uiState.value.postReports
                .firstOrNull { it.id == intent.id }?.also {
                    resolve(it)
                }

            ReportListMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
        }
    }

    private fun changeSection(section: ReportListSection) {
        mvi.updateState {
            it.copy(
                section = section,
            )
        }
    }

    private fun changeUnresolvedOnly(value: Boolean) {
        mvi.updateState {
            it.copy(unresolvedOnly = value)
        }
        refresh(initial = true)
    }

    private fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        mvi.scope?.launch {
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val refreshing = currentState.refreshing
            val section = currentState.section
            val unresolvedOnly = currentState.unresolvedOnly
            if (section == ReportListSection.Posts) {
                val itemList = postRepository.getReports(
                    auth = auth,
                    communityId = communityId,
                    page = currentPage,
                    unresolvedOnly = unresolvedOnly,
                )
                val commentReports =
                    if (currentPage == 1 && currentState.commentReports.isEmpty()) {
                        // this is needed because otherwise on first selector change
                        // the lazy column scrolls back to top (it must have an empty data set)
                        commentRepository.getReports(
                            auth = auth,
                            communityId = communityId,
                            page = currentPage,
                            unresolvedOnly = unresolvedOnly,
                        ).orEmpty()
                    } else {
                        currentState.commentReports
                    }
                mvi.updateState {
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
                    currentPage++
                }
            } else {
                val itemList = commentRepository.getReports(
                    auth = auth,
                    communityId = communityId,
                    page = currentPage,
                    unresolvedOnly = unresolvedOnly,
                )

                mvi.updateState {
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
                    currentPage++
                }
            }
        }
    }

    private fun resolve(report: PostReportModel) {
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(asyncInProgress = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val newReport = postRepository.resolveReport(
                reportId = report.id,
                auth = auth,
                resolved = !report.resolved
            )
            mvi.updateState { it.copy(asyncInProgress = false) }
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
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(asyncInProgress = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val newReport = commentRepository.resolveReport(
                reportId = report.id,
                auth = auth,
                resolved = !report.resolved
            )
            mvi.updateState { it.copy(asyncInProgress = false) }
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
        mvi.updateState {
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
        mvi.updateState {
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
        mvi.updateState {
            it.copy(
                postReports = it.postReports.filter { r -> r.id != report.id }
            )
        }
    }

    private fun handleReporDelete(report: CommentReportModel) {
        mvi.updateState {
            it.copy(
                commentReports = it.commentReports.filter { r -> r.id != report.id }
            )
        }
    }
}
