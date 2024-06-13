package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource

interface AcknowledgementsRemoteDataSource {
    suspend fun getAcknowledgements(): List<Acknowledgement>?
}
