package com.github.diegoberaldin.raccoonforlemmy.unit.createreport.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportMviModel

expect fun getCreateReportViewModel(
    postId: Int? = null,
    commentId: Int? = null,
): CreateReportMviModel
