package dev.umerov.project.model.main

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import dev.umerov.project.Constants
import dev.umerov.project.place.Place
import dev.umerov.project.readTypedObjectCompat
import dev.umerov.project.writeTypedObjectCompat

class MainButton : Parcelable {
    var text: String? = null

    @StringRes
    var textRes: Int = Constants.DISABLED_RESOURCE_ID

    var mainText: String? = null

    @StringRes
    var mainTextRes: Int = Constants.DISABLED_RESOURCE_ID

    var place: Place? = null

    constructor(place: Place?) {
        this.place = place
    }

    constructor(parcel: Parcel) {
        place = parcel.readTypedObjectCompat(Place.CREATOR)
        text = parcel.readString()
        textRes = parcel.readInt()
        mainText = parcel.readString()
        mainTextRes = parcel.readInt()
    }

    fun setPlace(place: Place?): MainButton {
        this.place = place
        return this
    }

    fun setText(text: String?): MainButton {
        this.text = text
        return this
    }

    fun setText(@StringRes textRes: Int): MainButton {
        this.textRes = textRes
        return this
    }

    fun setMainText(text: String?): MainButton {
        this.mainText = text
        return this
    }

    fun setMainText(@StringRes textRes: Int): MainButton {
        this.mainTextRes = textRes
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedObjectCompat(place, flags)
        dest.writeString(text)
        dest.writeInt(textRes)
        dest.writeString(mainText)
        dest.writeInt(mainTextRes)
    }

    companion object CREATOR : Parcelable.Creator<MainButton> {
        override fun createFromParcel(parcel: Parcel): MainButton {
            return MainButton(parcel)
        }

        override fun newArray(size: Int): Array<MainButton?> {
            return arrayOfNulls(size)
        }
    }
}
