package com.github.diegoberaldin.raccoonforlemmy.unit.createreport.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getCreateReportViewModel(
    postId: Int?,
    commentId: Int?,
): CreateReportMviModel = UnitCreateReportDiHelper.getCreateReportModel(postId, commentId)

object UnitCreateReportDiHelper : KoinComponent {
    fun getCreateReportModel(
        postId: Int?,
        commentId: Int?,
    ): CreateReportMviModel {
        val model: CreateReportMviModel by inject(
            parameters = { parametersOf(postId, commentId) }
        )
        return model
    }
}