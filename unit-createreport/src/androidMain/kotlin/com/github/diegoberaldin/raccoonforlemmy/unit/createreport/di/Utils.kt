package com.github.diegoberaldin.raccoonforlemmy.unit.createreport.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getCreateReportViewModel(
    postId: Int?,
    commentId: Int?,
): CreateReportMviModel {
    val res: CreateReportMviModel by KoinJavaComponent.inject(
        CreateReportMviModel::class.java,
        parameters = {
            parametersOf(postId, commentId)
        })
    return res
}
