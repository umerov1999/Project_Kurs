package dev.umerov.project.fragment.main.lab12

import androidx.annotation.RawRes
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.module.ProjectNative.appContext
import okio.buffer
import okio.source
import java.io.InputStream

class Lab12Presenter : RxSupportPresenter<ILab12View>() {
    private var isFirst = true
    private var raw: String? = null
    private var asset: String? = null
    override fun onGuiCreated(viewHost: ILab12View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_12)
            isFirst = false
        }
        viewHost.showTexts(raw, asset)
    }

    private fun readRes(@RawRes rawRes: Int): String? {
        var inputStream: InputStream? = null
        return try {
            inputStream = appContext.resources.openRawResource(rawRes)
            inputStream.source().buffer().readUtf8()
        } catch (e: Throwable) {
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (ignore: Throwable) {
            }
        }
    }

    private fun readAsset(file: String): String? {
        var inputStream: InputStream? = null
        return try {
            inputStream = appContext.assets.open(file)
            inputStream.source().buffer().readUtf8()
        } catch (e: Throwable) {
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (ignore: Throwable) {
            }
        }
    }

    init {
        raw = readRes(R.raw.lab_12_test)
        asset = readAsset("lab_12_test.txt")
    }
}
