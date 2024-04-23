package com.github.diegoberaldin.raccoonforlemmy.core.utils.fs

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

interface FileSystemManager {

    val isSupported: Boolean

    @Composable
    fun readFromFile(mimeTypes: Array<String>, callback: (String?) -> Unit)

    @Composable
    fun writeToFile(mimeType: String, name: String, data: String, callback: (Boolean) -> Unit)
}

expect val fileSystemModule: Module

expect fun getFileSystemManager(): FileSystemManager
