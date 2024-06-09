package dev.umerov.project.util.rxutils

import dev.umerov.project.Constants
import dev.umerov.project.util.Utils
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import java.io.Closeable

object RxUtils {
    private val DUMMMY_ACTION_0 = Action {}
    fun dummy(): Action {
        return DUMMMY_ACTION_0
    }

    inline fun <reified T : Any> ignore(): Consumer<T> {
        return Consumer { t ->
            if (t is Throwable && Constants.IS_DEBUG) {
                t.printStackTrace()
            }
        }
    }

    fun safelyCloseAction(closeable: Closeable?): Action {
        return Action { Utils.safelyClose(closeable) }
    }
}
