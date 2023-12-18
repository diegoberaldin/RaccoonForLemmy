package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.di

import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.ReportListMviModel

expect fun getReportListViewModel(
    communityId: Int,
): ReportListMviModel
