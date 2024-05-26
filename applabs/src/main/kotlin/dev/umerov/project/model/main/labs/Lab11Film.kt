package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.readTypedObjectCompat
import dev.umerov.project.writeTypedObjectCompat

class Lab11Film : Parcelable {
    var db_id: Long = -1
        private set
    var title: String? = null
        private set
    var genre: Lab11Genre? = null
        private set
    var year: Int = 0
        private set
    var thumbPath: String? = null
        private set
    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        title = parcel.readString()
        genre = parcel.readTypedObjectCompat(Lab11Genre.CREATOR)
        year = parcel.readInt()
        thumbPath = parcel.readString()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): Lab11Film {
        this.db_id = db_id
        return this
    }

    fun setTitle(title: String?): Lab11Film {
        this.title = title
        return this
    }

    fun setGenre(genre: Lab11Genre?): Lab11Film {
        this.genre = genre
        return this
    }

    fun setYear(year: Int): Lab11Film {
        this.year = year
        return this
    }

    fun setThumbPath(thumbPath: String?): Lab11Film {
        this.thumbPath = thumbPath
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeString(title)
        dest.writeTypedObjectCompat(genre, flags)
        dest.writeInt(year)
        dest.writeString(thumbPath)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object CREATOR : Parcelable.Creator<Lab11Film> {
        override fun createFromParcel(parcel: Parcel): Lab11Film {
            return Lab11Film(parcel)
        }

        override fun newArray(size: Int): Array<Lab11Film?> {
            return arrayOfNulls(size)
        }
    }
}
