package dev.umerov.project.model.db

import androidx.annotation.IntDef

@IntDef(CoinOperationType.NULL, CoinOperationType.TAKE, CoinOperationType.PASTE)
@Retention(AnnotationRetention.SOURCE)
annotation class CoinOperationType {
    companion object {
        const val NULL = 0
        const val TAKE = 1
        const val PASTE = 2
    }
}