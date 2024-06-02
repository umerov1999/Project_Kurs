package dev.umerov.project.model.main.labs

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.toColor
import dev.umerov.project.util.StepArrayList
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
class ShoppingList : Parcelable {
    var db_id: Long = -1
        private set
    var title: String? = null
        private set
    var description: String? = null
        private set
    var creationDate: Long = Calendar.getInstance().timeInMillis / 1000
        private set

    @ColorInt
    var color: Int = "#2D566B".toColor()
        private set

    var db_plannedPurchase: Int = 0
        private set

    var db_Purchase: Int = 0
        private set

    var tempIsAnimation = false
    var tempIsEditMode = false

    constructor()
    constructor(parcel: Parcel) {
        db_id = parcel.readLong()
        db_Purchase = parcel.readInt()
        db_plannedPurchase = parcel.readInt()
        title = parcel.readString()
        description = parcel.readString()
        creationDate = parcel.readLong()
        color = parcel.readInt()
        tempIsAnimation = parcel.getBoolean()
        tempIsEditMode = parcel.getBoolean()
    }

    fun setDBId(db_id: Long): ShoppingList {
        this.db_id = db_id
        return this
    }

    fun setDBPurchase(db_Purchase: Int): ShoppingList {
        this.db_Purchase = db_Purchase
        return this
    }

    fun setDBPlannedPurchase(db_plannedPurchase: Int): ShoppingList {
        this.db_plannedPurchase = db_plannedPurchase
        return this
    }

    fun fetchColor(): ShoppingList {
        color = colors.getNext() ?: "#2D566B".toColor()
        return this
    }

    fun setColor(@ColorInt color: Int): ShoppingList {
        this.color = color
        return this
    }

    fun setTitle(title: String?): ShoppingList {
        this.title = title
        return this
    }

    fun setDescription(description: String?): ShoppingList {
        this.description = description
        return this
    }

    fun setCreationDate(creationDate: Long): ShoppingList {
        this.creationDate = creationDate
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(db_id)
        dest.writeInt(db_Purchase)
        dest.writeInt(db_plannedPurchase)
        dest.writeString(title)
        dest.writeString(description)
        dest.writeLong(creationDate)
        dest.writeInt(color)
        dest.putBoolean(tempIsAnimation)
        dest.putBoolean(tempIsEditMode)
    }

    companion object {
        private val colors = StepArrayList(
            arrayListOf(
                "#2D566B".toColor(),
                "#227585".toColor(),
                "#861E6A".toColor(),
                "#AA1656".toColor()
            ), StepArrayList.SHOPPING_LIST
        )

        @JvmField
        val CREATOR: Parcelable.Creator<ShoppingList> =
            object : Parcelable.Creator<ShoppingList> {
                override fun createFromParcel(parcel: Parcel): ShoppingList {
                    return ShoppingList(parcel)
                }

                override fun newArray(size: Int): Array<ShoppingList?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
