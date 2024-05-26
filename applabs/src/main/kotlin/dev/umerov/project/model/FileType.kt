package dev.umerov.project.model

import androidx.annotation.IntDef

@IntDef(FileType.error, FileType.folder, FileType.photo, FileType.audio, FileType.text)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class FileType {
    companion object {
        const val error = -1
        const val folder = 0
        const val photo = 1
        const val audio = 2
        const val text = 3
    }
}