package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.repository

import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.models.AcknowledgementModel

interface AcknowledgementsRepository {
    suspend fun getAcknowledgements(): List<AcknowledgementModel>?
}
