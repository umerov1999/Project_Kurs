package dev.umerov.project.fragment.main.coin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import dev.umerov.project.R
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import java.text.SimpleDateFormat
import java.util.Date

class CoinOperationAdapter(
    private val mContext: Context,
    private var data: List<CoinOperation>
) : RecyclerView.Adapter<CoinOperationAdapter.Holder>() {
    private val DF_TODAY = SimpleDateFormat("HH:mm", Utils.appLocale)
    private val DF_OLD = SimpleDateFormat("dd/MM", Utils.appLocale)
    private var mStartOfToday: Long = 0
    private var mClickListener: ClickListener? = null
    private val mDataObserver: RecyclerView.AdapterDataObserver
    fun setItems(data: List<CoinOperation>) {
        this.data = data
        notifyDataSetChanged()
    }

    internal fun initStartOfTodayDate() {
        mStartOfToday = Utils.startOfTodayMillis()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(mContext).inflate(R.layout.item_coin_operation, parent, false)
        )
    }

    private fun findPrevious(pos: Int): CoinOperation? {
        for (i in pos downTo 0) {
            return data[i]
        }
        return null
    }

    private fun getStatus(operation: CoinOperation): Int {
        val time = operation.date * 1000
        if (time >= mStartOfToday) {
            return DIV_TODAY
        }
        if (time >= mStartOfToday - 86400000) {
            return DIV_YESTERDAY
        }
        return if (time >= mStartOfToday - 864000000) {
            DIV_THIS_WEEK
        } else DIV_OLD
    }

    private fun getDivided(operation: CoinOperation, previous: CoinOperation?): Int {
        val stCurrent = getStatus(operation)
        return if (previous == null) {
            stCurrent
        } else {
            val stPrevious = getStatus(previous)
            if (stCurrent == stPrevious) {
                DIV_DISABLE
            } else {
                stCurrent
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = data[position]
        holder.operationCoin.text =
            mContext.getString(R.string.rub, String.format("%.2f", item.coin))
        holder.operationTittle.text = item.tittle
        holder.operationComment.text = item.comment
        holder.operationIcon.setImageResource(if (item.type == CoinOperationType.TAKE) R.drawable.money_transfer_outline else R.drawable.money_request_outline)
        val lastMessageJavaTime = item.date * 1000

        val previous = if (position == 0) null else findPrevious(position - 1)
        val headerStatus = getDivided(item, previous)
        when (headerStatus) {
            DIV_DISABLE -> holder.mHeaderTitle.visibility = View.GONE

            DIV_TODAY -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.operation_day_today)
            }

            DIV_OLD -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.operation_day_older)
            }

            DIV_YESTERDAY -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.operation_day_yesterday)
            }

            DIV_THIS_WEEK -> {
                holder.mHeaderTitle.visibility = View.VISIBLE
                holder.mHeaderTitle.setText(R.string.operation_day_ten_days)
            }
        }
        DATE.time = lastMessageJavaTime
        if (lastMessageJavaTime < mStartOfToday) {
            holder.operationDate.setTextColor(CurrentTheme.getSecondaryTextColorCode(mContext))
            if (getStatus(item) == DIV_YESTERDAY) holder.operationDate.text =
                DF_TODAY.format(
                    DATE
                ) else holder.operationDate.text = DF_OLD.format(DATE)
        } else {
            holder.operationDate.text = DF_TODAY.format(DATE)
            holder.operationDate.setTextColor(CurrentTheme.getColorPrimary(mContext))
        }
        holder.itemView.setOnClickListener {
            mClickListener?.onClick(holder.bindingAdapterPosition, item)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setClickListener(clickListener: ClickListener?) {
        mClickListener = clickListener
    }

    fun cleanup() {
        unregisterAdapterDataObserver(mDataObserver)
    }

    interface ClickListener {
        fun onClick(position: Int, operation: CoinOperation)
    }

    companion object {
        private val DATE = Date()
        private const val DIV_DISABLE = 0
        private const val DIV_TODAY = 1
        private const val DIV_YESTERDAY = 2
        private const val DIV_THIS_WEEK = 3
        private const val DIV_OLD = 4
    }

    init {
        mDataObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                initStartOfTodayDate()
            }
        }
        registerAdapterDataObserver(mDataObserver)
        initStartOfTodayDate()
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val operationIcon: AppCompatImageView = itemView.findViewById(R.id.item_operation_type)
        val operationCoin: TextView = itemView.findViewById(R.id.item_operation_coin)
        val operationTittle: TextView = itemView.findViewById(R.id.item_operation_tittle)
        val operationDate: TextView = itemView.findViewById(R.id.item_operation_date)
        val operationComment: TextView = itemView.findViewById(R.id.item_operation_comment)
        val mHeaderTitle: TextView = itemView.findViewById(R.id.item_operation_header_title)
    }
}
