package dev.umerov.project.model

import androidx.annotation.IntDef

@IntDef(
    SectionItem.NULL,
    SectionItem.MAIN,
    SectionItem.SETTINGS
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class SectionItem {
    companion object {
        const val NULL = -1
        const val MAIN = 0
        const val SETTINGS = 1
    }
}