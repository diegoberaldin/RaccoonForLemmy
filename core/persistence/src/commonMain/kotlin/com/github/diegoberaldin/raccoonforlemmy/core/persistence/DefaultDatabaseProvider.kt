package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase

class DefaultDatabaseProvider(
    private val driverFactory: DriverFactory,
) : DatabaseProvider {
    private val db: AppDatabase by lazy {
        val driver = driverFactory.createDriver()
        AppDatabase(driver)
    }

    override fun getDatabase(): AppDatabase = db
}
