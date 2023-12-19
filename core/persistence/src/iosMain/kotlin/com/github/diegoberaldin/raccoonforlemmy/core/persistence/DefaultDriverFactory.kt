package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase

internal class DefaultDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AppDatabase.Schema, "racconforlemmy.db")
    }
}