package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

interface CommunitySortRepository {
    fun getSort(handle: String): Int?

    fun saveSort(
        handle: String,
        value: Int,
    )

    fun clear()
}
