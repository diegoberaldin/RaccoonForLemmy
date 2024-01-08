package com.github.diegoberaldin.raccoonforlemmy.core.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

internal class DefaultTemporaryKeyStore(
    private val settings: Settings,
) : TemporaryKeyStore {

    override fun containsKey(key: String): Boolean = settings.keys.contains(key)

    override fun save(key: String, value: Boolean) {
        settings[key] = value
    }

    override fun get(key: String, default: Boolean): Boolean = settings[key, default]

    override fun save(key: String, value: String) {
        settings[key] = value
    }

    override fun get(key: String, default: String): String = settings[key, default]

    override fun save(key: String, value: Int) {
        settings[key] = value
    }

    override fun get(key: String, default: Int): Int = settings[key, default]

    override fun save(key: String, value: Float) {
        settings[key] = value
    }

    override fun get(key: String, default: Float): Float = settings[key, default]

    override fun save(key: String, value: Double) {
        settings[key] = value
    }

    override fun get(key: String, default: Double): Double = settings[key, default]

    override fun save(key: String, value: Long) {
        settings[key] = value
    }

    override fun get(key: String, default: Long): Long = settings[key, default]

    override fun get(key: String, default: List<String>, delimiter: String): List<String> {
        if (!settings.hasKey(key)) {
            return default
        }
        val joined = settings[key, ""]
        return joined.split(delimiter)
    }

    override fun save(key: String, value: List<String>, delimiter: String) {
        settings[key] = value.joinToString(delimiter)
    }

    override fun remove(key: String) {
        settings.remove(key)
    }
}
