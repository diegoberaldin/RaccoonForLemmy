package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase

class DefaultDatabaseProvider(
    private val driverFactory: DriverFactory,
) : DatabaseProvider {

    override fun getDatabase(): AppDatabase {
        val driver = driverFactory.createDriver()
        return AppDatabase(driver)
    }
}