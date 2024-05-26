package dev.umerov.project.model.db

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.kJson
import kotlinx.serialization.Serializable

@Serializable
class Register : Parcelable {
    var coinBalance: Double
    var coinTaked: Double
    var coinPasted: Double
    var operationsCount: Long

    constructor(
        coinBalance: Double,
        coinTaked: Double,
        coinPasted: Double,
        operationsCount: Long,
    ) {
        this.coinBalance = coinBalance
        this.operationsCount = operationsCount
        this.coinPasted = coinPasted
        this.coinTaked = coinTaked
    }

    constructor(parcel: Parcel) {
        coinBalance = parcel.readDouble()
        coinTaked = parcel.readDouble()
        coinPasted = parcel.readDouble()
        operationsCount = parcel.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return kJson.encodeToString(serializer(), this)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(coinBalance)
        dest.writeDouble(coinTaked)
        dest.writeDouble(coinPasted)
        dest.writeLong(operationsCount)
    }

    companion object CREATOR : Parcelable.Creator<Register> {
        override fun createFromParcel(parcel: Parcel): Register {
            return Register(parcel)
        }

        override fun newArray(size: Int): Array<Register?> {
            return arrayOfNulls(size)
        }
    }
}
