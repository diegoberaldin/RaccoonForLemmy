package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import app.cash.sqldelight.db.SqlDriver

interface DriverFactory {
    fun createDriver(): SqlDriver
}
