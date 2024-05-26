package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable

class Lab8Currency : Parcelable {
    var title: String? = null
    var sale: Double = 0.0
    var purchase: Double = 0.0

    constructor(
        title: String?,
        sale: Double,
        purchase: Double
    ) {
        this.title = title
        this.sale = sale
        this.purchase = purchase
    }

    constructor(parcel: Parcel) {
        title = parcel.readString()
        sale = parcel.readDouble()
        purchase = parcel.readDouble()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeDouble(sale)
        dest.writeDouble(purchase)
    }

    companion object CREATOR : Parcelable.Creator<Lab8Currency> {
        override fun createFromParcel(parcel: Parcel): Lab8Currency {
            return Lab8Currency(parcel)
        }

        override fun newArray(size: Int): Array<Lab8Currency?> {
            return arrayOfNulls(size)
        }
    }
}
