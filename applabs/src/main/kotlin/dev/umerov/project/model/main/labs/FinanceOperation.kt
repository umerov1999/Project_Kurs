package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.toColor
import dev.umerov.project.util.StepArrayList
import java.util.Calendar

class FinanceOperation : Parcelable {
    var db_id: Long = -1
        private set
    var db_owner_id: Long = -1
        private set
    var title: String? = null
        private set
    var description: String? = null
        private set
    var createDate: Long = 0
        private set
    var coins: Double = 0.0
        private set
    var isIncome: Boolean = false
        private set

    @ColorInt
    var color: Int = "#2D566B".toColor()
        private set
    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        db_owner_id = parcel.readLong()
        title = parcel.readString()
        description = parcel.readString()
        createDate = parcel.readLong()
        coins = parcel.readDouble()
        isIncome = parcel.getBoolean()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): FinanceOperation {
        this.db_id = db_id
        return this
    }

    fun setOwnerId(db_owner_id: Long): FinanceOperation {
        this.db_owner_id = db_owner_id
        return this
    }

    fun setTitle(title: String?): FinanceOperation {
        this.title = title
        return this
    }

    fun setDescription(description: String?): FinanceOperation {
        this.description = description
        return this
    }

    fun fetchColor(): FinanceOperation {
        color = colors.getNext() ?: "#2D566B".toColor()
        return this
    }

    fun setColor(@ColorInt color: Int): FinanceOperation {
        this.color = color
        return this
    }

    fun setCreateDate(createDate: Long): FinanceOperation {
        this.createDate = createDate
        return this
    }

    fun fetchCreateDate(): FinanceOperation {
        this.createDate = Calendar.getInstance().timeInMillis / 1000
        return this
    }

    fun setIsIncome(isIncome: Boolean): FinanceOperation {
        this.isIncome = isIncome
        return this
    }

    fun setCoins(coins: Double): FinanceOperation {
        this.coins = coins
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeLong(db_owner_id)
        dest.writeString(title)
        dest.writeString(description)
        dest.writeLong(createDate)
        dest.writeDouble(coins)
        dest.putBoolean(isIncome)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object {
        private val colors = StepArrayList(
            arrayListOf(
                "#AA1656".toColor(),
                "#861E6A".toColor(),
                "#227585".toColor(),
                "#2D566B".toColor(),
                "#FFA000".toColor(),
                "#795548".toColor()
            ), StepArrayList.FINANCE_OPERATION
        )

        @JvmField
        val CREATOR: Parcelable.Creator<FinanceOperation> =
            object : Parcelable.Creator<FinanceOperation> {
                override fun createFromParcel(parcel: Parcel): FinanceOperation {
                    return FinanceOperation(parcel)
                }

                override fun newArray(size: Int): Array<FinanceOperation?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
