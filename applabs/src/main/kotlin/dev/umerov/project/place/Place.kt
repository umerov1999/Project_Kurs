package dev.umerov.project.place

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import dev.umerov.project.util.Utils

open class Place : Parcelable {
    val type: Int
    var isNeedFinishMain = false
        private set
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var args: Bundle? = null

    constructor(type: Int) {
        this.type = type
    }

    protected constructor(p: Parcel) {
        type = p.readInt()
        args = p.readBundle(javaClass.classLoader)
    }

    fun tryOpenWith(context: Context) {
        if (context is PlaceProvider) {
            (context as PlaceProvider).openPlace(this)
        }
    }

    fun setActivityResultLauncher(activityResultLauncher: ActivityResultLauncher<Intent>): Place {
        this.activityResultLauncher = activityResultLauncher
        return this
    }

    fun setNeedFinishMain(needFinishMain: Boolean): Place {
        isNeedFinishMain = needFinishMain
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        dest.writeBundle(args)
    }

    fun setArguments(arguments: Bundle?): Place {
        args = arguments
        return this
    }

    fun withStringExtra(name: String, value: String?): Place {
        prepareArguments().putString(name, value)
        return this
    }

    fun withParcelableExtra(name: String, parcelableExtra: Parcelable?): Place {
        prepareArguments().putParcelable(name, parcelableExtra)
        return this
    }

    fun withIntExtra(name: String, value: Int): Place {
        prepareArguments().putInt(name, value)
        return this
    }

    fun withLongExtra(name: String, value: Long): Place {
        prepareArguments().putLong(name, value)
        return this
    }

    fun prepareArguments(): Bundle {
        if (args == null) {
            args = Bundle()
        }
        return args!!
    }

    fun safeArguments(): Bundle {
        return args ?: Bundle()
    }

    fun launchActivityForResult(context: Activity, intent: Intent) {
        if (activityResultLauncher != null && !isNeedFinishMain) {
            activityResultLauncher?.launch(intent)
        } else {
            context.startActivity(intent)
            if (isNeedFinishMain) {
                Utils.finishActivityImmediate(context)
            }
        }
    }

    companion object {
        const val MAIN = 1
        const val PREFERENCES = 2
        const val SETTINGS_THEME = 3
        const val SECURITY = 4
        const val FILE_MANAGER = 5
        const val STAFF = 6

        const val LAB_1 = 7
        const val LAB_2 = 8
        const val LAB_3 = 9
        const val LAB_4 = 10
        const val LAB_4_1 = 11

        const val LAB_5 = 12
        const val LAB_6 = 13
        const val LAB_7 = 14
        const val LAB_8 = 15

        const val LAB_9 = 16
        const val LAB_10 = 17
        const val LAB_11 = 18

        const val LAB_12 = 19
        const val LAB_13 = 20

        const val LAB_14 = 21

        const val LAB_15 = 22
        const val LAB_16 = 23
        const val LAB_17 = 24
        const val LAB_18 = 25

        const val LAB_19 = 26

        const val SNAKE = 27

        const val SHOPPING_LIST = 28
        const val SHOPPING_PRODUCTS = 29

        @JvmField
        val CREATOR: Parcelable.Creator<Place> = object : Parcelable.Creator<Place> {
            override fun createFromParcel(p: Parcel): Place {
                return Place(p)
            }

            override fun newArray(size: Int): Array<Place?> {
                return arrayOfNulls(size)
            }
        }
    }
}
