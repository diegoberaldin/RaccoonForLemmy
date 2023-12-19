package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase

interface DatabaseProvider {
    fun getDatabase(): AppDatabase
}

