package dev.umerov.project.model.main.labs

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.util.StepArrayList

class Lab14AudioAlbum : Parcelable {
    var db_id: Long = -1
        private set
    var title: String? = null
        private set
    var artist: String? = null
        private set
    var year: Int = 0
        private set
    var thumbPath: String? = null
        private set

    @ColorInt
    var color: Int = Color.parseColor("#2D566B")
        private set
    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        title = parcel.readString()
        artist = parcel.readString()
        year = parcel.readInt()
        thumbPath = parcel.readString()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): Lab14AudioAlbum {
        this.db_id = db_id
        return this
    }

    fun setTitle(title: String?): Lab14AudioAlbum {
        this.title = title
        return this
    }

    fun fetchColor(): Lab14AudioAlbum {
        color = colors.getNext() ?: Color.parseColor("#2D566B")
        return this
    }

    fun setColor(@ColorInt color: Int): Lab14AudioAlbum {
        this.color = color
        return this
    }

    fun setArtist(artist: String?): Lab14AudioAlbum {
        this.artist = artist
        return this
    }

    fun setYear(year: Int): Lab14AudioAlbum {
        this.year = year
        return this
    }

    fun setThumbPath(thumbPath: String?): Lab14AudioAlbum {
        this.thumbPath = thumbPath
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeString(title)
        dest.writeString(artist)
        dest.writeInt(year)
        dest.writeString(thumbPath)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object {
        private val colors = StepArrayList(
            arrayListOf(
                Color.parseColor("#2D566B"),
                Color.parseColor("#227585"),
                Color.parseColor("#861E6A"),
                Color.parseColor("#AA1656")
            )
        )

        @JvmField
        val CREATOR: Parcelable.Creator<Lab14AudioAlbum> =
            object : Parcelable.Creator<Lab14AudioAlbum> {
                override fun createFromParcel(parcel: Parcel): Lab14AudioAlbum {
                    return Lab14AudioAlbum(parcel)
                }

                override fun newArray(size: Int): Array<Lab14AudioAlbum?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
