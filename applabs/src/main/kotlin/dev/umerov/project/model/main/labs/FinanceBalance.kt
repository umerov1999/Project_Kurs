package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable

class FinanceBalance : Parcelable {
    var fullBalance: Double = 0.0
        private set
    var walletBalance: Double = 0.0
        private set
    var creditBalance: Double = 0.0
        private set

    constructor()
    constructor(parcel: Parcel) {
        fullBalance = parcel.readDouble()
        walletBalance = parcel.readDouble()
        creditBalance = parcel.readDouble()
    }

    fun setFullBalance(fullBalance: Double): FinanceBalance {
        this.fullBalance = fullBalance
        return this
    }

    fun setWalletBalance(walletBalance: Double): FinanceBalance {
        this.walletBalance = walletBalance
        return this
    }

    fun setCreditBalance(creditBalance: Double): FinanceBalance {
        this.creditBalance = creditBalance
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(fullBalance)
        dest.writeDouble(walletBalance)
        dest.writeDouble(creditBalance)
    }

    companion object CREATOR : Parcelable.Creator<FinanceBalance> {
        override fun createFromParcel(parcel: Parcel): FinanceBalance {
            return FinanceBalance(parcel)
        }

        override fun newArray(size: Int): Array<FinanceBalance?> {
            return arrayOfNulls(size)
        }
    }
}
