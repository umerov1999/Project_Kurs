package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.getBoolean
import dev.umerov.project.kJson
import dev.umerov.project.putBoolean
import kotlinx.serialization.Serializable

@Serializable
class Lab9Contact : Parcelable {
    var contact: String? = null
    var email: String? = null
    var tempPosition: Int = -1
    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()

    constructor(
        contact: String?,
        email: String?
    ) {
        this.contact = contact
        this.email = email
    }

    constructor(parcel: Parcel) {
        contact = parcel.readString()
        email = parcel.readString()
        tempPosition = parcel.readInt()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(contact)
        dest.writeString(email)
        dest.writeInt(tempPosition)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    override fun toString(): String {
        return kJson.encodeToString(serializer(), this)
    }

    companion object CREATOR : Parcelable.Creator<Lab9Contact> {
        override fun createFromParcel(parcel: Parcel): Lab9Contact {
            return Lab9Contact(parcel)
        }

        override fun newArray(size: Int): Array<Lab9Contact?> {
            return arrayOfNulls(size)
        }
    }
}
