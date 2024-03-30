package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

internal class DefaultDriverFactory(
    private val context: Context,
    private val keyProvider: DatabaseKeyProvider,
) : DriverFactory {

    companion object {
        private const val DATABASE_NAME = "raccoonforlemmy.db"
    }

    override fun createDriver(): SqlDriver {
        System.loadLibrary("sqlcipher")
        val key = keyProvider.getKey()
        val supportFactory = SupportOpenHelperFactory(key)
        return AndroidSqliteDriver(
            factory = supportFactory,
            schema = AppDatabase.Schema,
            context = context,
            name = DATABASE_NAME,
        )
    }
}
