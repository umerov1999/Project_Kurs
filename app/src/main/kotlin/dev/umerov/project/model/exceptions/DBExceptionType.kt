package dev.umerov.project.model.exceptions

import androidx.annotation.IntDef

@IntDef(
    DBExceptionType.ERROR_GET_REGISTER,
    DBExceptionType.GET_COIN_OPERATION_BY_ID,
    DBExceptionType.ADD_OPERATION_SUPPORT_ONLY_NEW,
    DBExceptionType.BALANCE_IS_LOW
)
@Retention(AnnotationRetention.SOURCE)
annotation class DBExceptionType {
    companion object {
        const val ERROR_GET_REGISTER = 0
        const val GET_COIN_OPERATION_BY_ID = 1
        const val ADD_OPERATION_SUPPORT_ONLY_NEW = 2
        const val BALANCE_IS_LOW = 3
    }
}
