package dev.umerov.project.fragment.main.lab17

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class Lab17Presenter(private val isPost: Boolean) : RxSupportPresenter<ILab17View>() {
    private var isFirst = true
    private var text: String? = null
    override fun onGuiCreated(viewHost: ILab17View) {
        super.onGuiCreated(viewHost)
        viewHost.displayAnswer(text)
        if (isFirst) {
            if (isPost) {
                viewHost.showMessage(R.string.lab_18)
            } else {
                viewHost.showMessage(R.string.lab_17)
            }
            isFirst = false
        }
    }

    private fun requestInternal(
        url: String,
        body: FormBody?,
        onlySuccessful: Boolean,
        post: Boolean
    ): Flow<String> {
        return flow {
            val request = Request.Builder()
                .url(
                    url
                )
            if (post && body != null) {
                request.post(body)
            } else {
                request.get()
                body?.let {
                    val httpBuilder = url.toHttpUrlOrNull()?.newBuilder()
                    for (i in 0..<it.size) {
                        httpBuilder?.addQueryParameter(it.name(i), it.value(i))
                    }
                    httpBuilder?.build()?.let { it1 -> request.url(it1) }
                }
            }
            val builder = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
            val call = builder.build().newCall(request.build())
            try {
                val response = call.execute()
                if (!response.isSuccessful && onlySuccessful) {
                    throw Exception(response.code.toString())
                } else {
                    emit(
                        response.body.string()
                    )
                }
                response.close()
            } catch (e: CancellationException) {
                call.cancel()
                throw e
            }
        }
    }

    private fun toSerialStr(obj: Any?): String? {
        return when (obj) {
            is String -> {
                obj
            }

            is Byte, is Short, is Int, is Long, is Float, is Double -> {
                obj.toString()
            }

            is Boolean -> {
                if (obj) "1" else "0"
            }

            else -> null
        }
    }

    private fun form(vararg pairs: Pair<String, Any?>): FormBody {
        val formBuilder = FormBody.Builder()
        for ((first, second) in pairs) {
            toSerialStr(second)?.let {
                formBuilder.add(first, it)
            }
        }
        return formBuilder.build()
    }

    fun fireSubmit(url: String, firstName: String, lastName: String) {
        appendJob(
            requestInternal(
                url,
                form("firstname" to firstName, "lastname" to lastName),
                onlySuccessful = true,
                post = isPost
            ).fromIOToMain({
                view?.displayAnswer(it)
            }, {
                view?.showThrowable(it)
            })
        )
    }
}
