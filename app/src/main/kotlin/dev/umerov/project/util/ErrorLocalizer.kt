package dev.umerov.project.util

import android.content.Context
import dev.umerov.project.R
import dev.umerov.project.model.exceptions.DBException
import dev.umerov.project.model.exceptions.DBExceptionType
import dev.umerov.project.nonNullNoEmpty
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorLocalizer {
    fun localizeThrowable(context: Context, throwable: Throwable?): String {
        throwable ?: return "null"
        return when (throwable) {
            is SocketTimeoutException -> {
                context.getString(R.string.error_timeout_message)
            }

            is InterruptedIOException -> {
                if ("timeout" == throwable.message || "executor rejected" == throwable.message) {
                    context.getString(R.string.error_timeout_message)
                } else {
                    throwable.message.nonNullNoEmpty({ it }, { throwable.toString() })
                }
            }

            is ConnectException, is UnknownHostException -> {
                context.getString(R.string.error_unknown_host)
            }

            is DBException -> {
                when (throwable.id) {
                    DBExceptionType.ERROR_GET_REGISTER -> context.getString(R.string.error_db_get_register)
                    DBExceptionType.GET_COIN_OPERATION_BY_ID -> context.getString(R.string.error_db_get_coin_operation_id)
                    DBExceptionType.ADD_OPERATION_SUPPORT_ONLY_NEW -> context.getString(R.string.error_db_add_operation_support_only_new)
                    DBExceptionType.BALANCE_IS_LOW -> context.getString(R.string.error_db_low_balance)
                    else -> context.getString(R.string.app_name)
                }
            }

            else -> if (throwable.message.nonNullNoEmpty()) throwable.message!! else throwable.toString()
        }
    }
}