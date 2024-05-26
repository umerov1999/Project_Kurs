package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean

class Lab11Genre : Parcelable {
    var db_id: Long = -1
        private set
    var name: String? = null
        private set
    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        name = parcel.readString()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): Lab11Genre {
        this.db_id = db_id
        return this
    }

    fun setName(name: String?): Lab11Genre {
        this.name = name
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeString(name)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object CREATOR : Parcelable.Creator<Lab11Genre> {
        override fun createFromParcel(parcel: Parcel): Lab11Genre {
            return Lab11Genre(parcel)
        }

        override fun newArray(size: Int): Array<Lab11Genre?> {
            return arrayOfNulls(size)
        }
    }
}
