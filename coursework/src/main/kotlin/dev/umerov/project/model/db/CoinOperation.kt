package dev.umerov.project.model.db

import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.kJson
import kotlinx.serialization.Serializable

@Serializable
class CoinOperation : Parcelable {
    var dbId: Long
    var date: Long
    var title: String?
    var comment: String?

    @CoinOperationType
    var type: Int
    var coin: Double

    constructor(
        date: Long,
        title: String?,
        comment: String?,
        @CoinOperationType type: Int,
        coin: Double
    ) {
        dbId = -1
        this.date = date
        this.title = title
        this.comment = comment
        this.type = type
        this.coin = coin
    }

    constructor(parcel: Parcel) {
        dbId = parcel.readLong()
        date = parcel.readLong()
        title = parcel.readString()
        comment = parcel.readString()
        type = parcel.readInt()
        coin = parcel.readDouble()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(dbId)
        dest.writeLong(date)
        dest.writeString(title)
        dest.writeString(comment)
        dest.writeInt(type)
        dest.writeDouble(coin)
    }

    override fun toString(): String {
        return kJson.encodeToString(serializer(), this)
    }

    companion object CREATOR : Parcelable.Creator<CoinOperation> {
        override fun createFromParcel(parcel: Parcel): CoinOperation {
            return CoinOperation(parcel)
        }

        override fun newArray(size: Int): Array<CoinOperation?> {
            return arrayOfNulls(size)
        }
    }
}
