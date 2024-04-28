package com.github.diegoberaldin.raccoonforlemmy.core.utils.fs

import androidx.compose.runtime.Composable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultFileSystemManager : FileSystemManager {

    override val isSupported = false

    @Composable
    override fun readFromFile(mimeTypes: Array<String>, callback: (String?) -> Unit) {
        callback(null)
    }

    @Composable
    override fun writeToFile(mimeType: String, name: String, data: String, callback: (Boolean) -> Unit) {
        callback(false)
    }
}

object FileSystemManagerDiHelper : KoinComponent {
    val helper: FileSystemManager by inject()
}

actual fun getFileSystemManager(): FileSystemManager = FileSystemManagerDiHelper.helper
