package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.di

import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.ReportListMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getReportListViewModel(
    communityId: Int,
): ReportListMviModel {
    val res: ReportListMviModel by KoinJavaComponent.inject(
        ReportListMviModel::class.java,
        parameters = {
            parametersOf(communityId)
        })
    return res
}
