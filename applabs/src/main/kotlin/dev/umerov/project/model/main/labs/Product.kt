package dev.umerov.project.model.main.labs

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.util.StepArrayList

class Product : Parcelable {
    var db_id: Long = -1
        private set
    var db_owner_id: Long = -1
        private set
    var name: String? = null
        private set
    var count: Int = 0
        private set

    var isBought: Boolean = false
        private set

    @ProductUnit
    var unit: Int = 0
        private set

    @ColorInt
    var color: Int = Color.parseColor("#2D566B")
        private set

    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        db_owner_id = parcel.readLong()
        name = parcel.readString()
        count = parcel.readInt()
        unit = parcel.readInt()
        isBought = parcel.getBoolean()
        color = parcel.readInt()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): Product {
        this.db_id = db_id
        return this
    }

    fun setDBOwnerId(db_owner_id: Long): Product {
        this.db_owner_id = db_owner_id
        return this
    }

    fun fetchColor(): Product {
        color = colors.getNext() ?: Color.parseColor("#2D566B")
        return this
    }

    fun setColor(@ColorInt color: Int): Product {
        this.color = color
        return this
    }

    fun setName(name: String?): Product {
        this.name = name
        return this
    }

    fun setIsBought(isBought: Boolean): Product {
        this.isBought = isBought
        return this
    }

    fun setCount(count: Int): Product {
        this.count = count
        return this
    }

    fun setUnit(@ProductUnit unit: Int): Product {
        this.unit = unit
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeLong(db_owner_id)
        dest.writeString(name)
        dest.writeInt(count)
        dest.writeInt(unit)
        dest.putBoolean(isBought)
        dest.writeInt(color)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object {
        private val colors = StepArrayList(
            arrayListOf(
                Color.parseColor("#AA1656"),
                Color.parseColor("#861E6A"),
                Color.parseColor("#227585"),
                Color.parseColor("#2D566B")
            )
        )

        @JvmField
        val CREATOR: Parcelable.Creator<Product> =
            object : Parcelable.Creator<Product> {
                override fun createFromParcel(parcel: Parcel): Product {
                    return Product(parcel)
                }

                override fun newArray(size: Int): Array<Product?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
