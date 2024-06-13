package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.repository

import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource.Acknowledgement
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource.AcknowledgementsRemoteDataSource
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.models.AcknowledgementModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultAcknowledgementsRepository(
    private val dataSource: AcknowledgementsRemoteDataSource,
) : AcknowledgementsRepository {
    override suspend fun getAcknowledgements(): List<AcknowledgementModel>? =
        withContext(Dispatchers.IO) {
            dataSource.getAcknowledgements()?.map { it.toModel() }
        }
}

private fun Acknowledgement.toModel() =
    AcknowledgementModel(
        title = title,
        url = url,
        avatar = avatar,
        subtitle = subtitle,
    )
