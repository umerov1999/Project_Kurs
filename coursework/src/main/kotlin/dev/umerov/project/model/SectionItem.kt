package dev.umerov.project.model

import androidx.annotation.IntDef

@IntDef(
    SectionItem.NULL,
    SectionItem.BALANCE,
    SectionItem.PASTE,
    SectionItem.TAKE,
    SectionItem.SETTINGS
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class SectionItem {
    companion object {
        const val NULL = -1
        const val BALANCE = 0
        const val PASTE = 1
        const val TAKE = 2
        const val SETTINGS = 3
    }
}