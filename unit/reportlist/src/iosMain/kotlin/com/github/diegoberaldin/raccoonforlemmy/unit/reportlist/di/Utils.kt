package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.di

import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.ReportListMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getReportListViewModel(
    communityId: Int,
): ReportListMviModel = UnitReportListDiHelper.getReportListViewModel(communityId)

object UnitReportListDiHelper : KoinComponent {
    fun getReportListViewModel(
        communityId: Int,
    ): ReportListMviModel {
        val model: ReportListMviModel by inject(
            parameters = { parametersOf(communityId) }
        )
        return model
    }
}
