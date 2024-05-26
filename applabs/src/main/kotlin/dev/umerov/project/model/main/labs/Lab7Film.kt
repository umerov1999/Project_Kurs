package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable

class Lab7Film : Parcelable {
    var title: String? = null
    var genre: String? = null
    var year: Int = 0

    constructor(
        title: String?,
        genre: String?,
        year: Int = 0
    ) {
        this.title = title
        this.genre = genre
        this.year = year
    }

    constructor(parcel: Parcel) {
        title = parcel.readString()
        genre = parcel.readString()
        year = parcel.readInt()
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            Pair("Title", title.orEmpty()),
            Pair("Genre", genre.orEmpty()),
            Pair("Year", year.toString())
        )
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(genre)
        dest.writeInt(year)
    }

    companion object CREATOR : Parcelable.Creator<Lab7Film> {
        override fun createFromParcel(parcel: Parcel): Lab7Film {
            return Lab7Film(parcel)
        }

        override fun newArray(size: Int): Array<Lab7Film?> {
            return arrayOfNulls(size)
        }
    }
}
