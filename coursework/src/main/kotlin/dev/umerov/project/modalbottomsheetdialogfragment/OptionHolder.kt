package dev.umerov.project.modalbottomsheetdialogfragment

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.readTypedObjectCompat
import dev.umerov.project.writeTypedObjectCompat

/**
 * Holds either the resource options or the custom option
 */
internal class OptionHolder(val resource: Int?, val optionRequest: OptionRequest?) : Parcelable {

    constructor(source: Parcel) : this(
        source.readValue(Int::class.java.classLoader) as Int?,
        source.readTypedObjectCompat(OptionRequest.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeValue(resource)
        writeTypedObjectCompat(optionRequest, 0)
    }

    companion object CREATOR : Parcelable.Creator<OptionHolder> {
        override fun createFromParcel(parcel: Parcel): OptionHolder {
            return OptionHolder(parcel)
        }

        override fun newArray(size: Int): Array<OptionHolder?> {
            return arrayOfNulls(size)
        }
    }
}
