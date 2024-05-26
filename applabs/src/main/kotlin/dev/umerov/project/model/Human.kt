package dev.umerov.project.model

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.readTypedObjectCompat
import dev.umerov.project.writeTypedObjectCompat
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Human : Parcelable {
    var firstName: String? = null
        private set
    var lastName: String? = null
        private set
    var gender: Boolean = true
        private set
    var birthDay: Date? = null

    @Transient
    var tempPosition: Int = -1

    @Transient
    var tempIsMenuOpen: Boolean = false

    constructor()

    constructor(parcel: Parcel) {
        firstName = parcel.readString()
        lastName = parcel.readString()
        gender = parcel.getBoolean()
        tempPosition = parcel.readInt()
        tempIsMenuOpen = parcel.getBoolean()
        birthDay = parcel.readTypedObjectCompat(Date.CREATOR)
    }

    fun setFirstName(firstName: String?): Human {
        this.firstName = firstName
        return this
    }

    fun setLastName(lastName: String?): Human {
        this.lastName = lastName
        return this
    }

    fun setGender(gender: Boolean): Human {
        this.gender = gender
        return this
    }

    val birthDayString: String
        get() {
            val tmpBirthday = birthDay ?: return "null"
            var str = ""
            val day = tmpBirthday.dayOfMonth
            str += (if ((day < 10)) "0" else "") + day + "/"
            val mon = tmpBirthday.month + 1
            str += (if ((mon < 10)) "0" else "") + mon + "/"
            str += tmpBirthday.year
            return str
        }

    fun setBirthDay(day: Int, month: Int, year: Int) {
        birthDay = Date().setYear(year).setMonth(month - 1).setDayOfMonth(day)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(firstName)
        dest.writeString(lastName)
        dest.putBoolean(gender)
        dest.writeInt(tempPosition)
        dest.putBoolean(tempIsMenuOpen)
        dest.writeTypedObjectCompat(birthDay, flags)
    }

    companion object CREATOR : Parcelable.Creator<Human> {
        override fun createFromParcel(parcel: Parcel): Human {
            return Human(parcel)
        }

        override fun newArray(size: Int): Array<Human?> {
            return arrayOfNulls(size)
        }
    }

    @Serializable
    class Date : Parcelable {
        var year: Int = 0
            private set
        var month: Int = 0
            private set
        var dayOfMonth: Int = 0
            private set

        constructor()
        constructor(parcel: Parcel) {
            year = parcel.readInt()
            month = parcel.readInt()
            dayOfMonth = parcel.readInt()
        }

        fun setYear(year: Int): Date {
            this.year = year
            return this
        }

        fun setMonth(month: Int): Date {
            this.month = month
            return this
        }

        fun setDayOfMonth(dayOfMonth: Int): Date {
            this.dayOfMonth = dayOfMonth
            return this
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(year)
            dest.writeInt(month)
            dest.writeInt(dayOfMonth)
        }

        companion object CREATOR : Parcelable.Creator<Date> {
            override fun createFromParcel(parcel: Parcel): Date {
                return Date(parcel)
            }

            override fun newArray(size: Int): Array<Date?> {
                return arrayOfNulls(size)
            }
        }
    }
}
