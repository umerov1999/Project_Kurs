package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.toColor
import dev.umerov.project.util.StepArrayList
import java.util.Calendar

class FinanceWallet : Parcelable {
    var db_id: Long = -1
        private set
    var title: String? = null
        private set
    var createDate: Long = 0
        private set
    var coins: Double = 0.0
        private set
    var isCreditCard: Boolean = false
        private set

    @ColorInt
    var color: Int = "#2D566B".toColor()
        private set
    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        title = parcel.readString()
        createDate = parcel.readLong()
        coins = parcel.readDouble()
        isCreditCard = parcel.getBoolean()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): FinanceWallet {
        this.db_id = db_id
        return this
    }

    fun setTitle(title: String?): FinanceWallet {
        this.title = title
        return this
    }

    fun fetchColor(): FinanceWallet {
        color = colors.getNext() ?: "#2D566B".toColor()
        return this
    }

    fun setColor(@ColorInt color: Int): FinanceWallet {
        this.color = color
        return this
    }

    fun setCreateDate(createDate: Long): FinanceWallet {
        this.createDate = createDate
        return this
    }

    fun fetchCreateDate(): FinanceWallet {
        this.createDate = Calendar.getInstance().timeInMillis / 1000
        return this
    }

    fun makeCreditCard(): FinanceWallet {
        this.isCreditCard = true
        return this
    }

    fun setCreditCard(isCreditCard: Boolean): FinanceWallet {
        this.isCreditCard = isCreditCard
        return this
    }

    fun setCoins(coins: Double): FinanceWallet {
        this.coins = coins
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeString(title)
        dest.writeLong(createDate)
        dest.writeDouble(coins)
        dest.putBoolean(isCreditCard)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object {
        private val colors = StepArrayList(
            arrayListOf(
                "#FFA000".toColor(),
                "#861E6A".toColor(),
                "#AA1656".toColor(),
                "#227585".toColor(),
                "#2D566B".toColor(),
                "#795548".toColor()
            ), StepArrayList.FINANCE_WALLET
        )

        @JvmField
        val CREATOR: Parcelable.Creator<FinanceWallet> =
            object : Parcelable.Creator<FinanceWallet> {
                override fun createFromParcel(parcel: Parcel): FinanceWallet {
                    return FinanceWallet(parcel)
                }

                override fun newArray(size: Int): Array<FinanceWallet?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
