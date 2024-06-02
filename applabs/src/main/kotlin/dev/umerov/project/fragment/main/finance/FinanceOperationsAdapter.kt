package dev.umerov.project.fragment.main.finance

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.model.main.labs.FinanceOperation
import dev.umerov.project.util.AppTextUtils
import dev.umerov.project.util.Utils
import dev.umerov.project.view.RoundCornerLinearView

class FinanceOperationsAdapter(private var data: List<FinanceOperation>) :
    RecyclerView.Adapter<FinanceOperationsAdapter.Holder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_finance_operation, parent, false)
        )
    }

    private fun fixNumeric(str: String): String {
        return str.replace(",", ".")
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.isBinding = true
        holder.animatorAlpha.cancel()
        holder.animatorAlphaReversed.cancel()
        holder.animatorHeight.cancel()
        holder.animatorHeightReversed.cancel()
        holder.isBinding = false

        val item = data[position]
        holder.item.setOnClickListener {
            clickListener?.onClick(holder.bindingAdapterPosition, item)
        }
        holder.item.setOnLongClickListener {
            clickListener?.onLongClick(holder.bindingAdapterPosition, item) == true
        }
        holder.title.text = holder.itemView.context.getString(R.string.title_param, item.title)
        holder.description.text =
            holder.itemView.context.getString(R.string.description_param, item.description)
        holder.coins.text = holder.itemView.context.getString(
            R.string.coin_param_operation,
            String.format(
                Utils.appLocale,
                "%.2f",
                if (item.isIncome) item.coins else item.coins * -1
            )
        )
        holder.editTitle.setText(item.title)
        holder.editDescription.setText(item.description)
        holder.editIsIncome.isChecked = item.isIncome
        holder.createDate.text = holder.itemView.context.getString(
            R.string.created_param,
            AppTextUtils.getDateFromUnixTimeShorted(holder.itemView.context, item.createDate)
        )
        holder.editCoins.setText(fixNumeric(String.format(Utils.appLocale, "%.2f", item.coins)))
        holder.item.setViewColor(item.color)

        if (item.tempIsEditMode) {
            if (!item.tempIsAnimation) {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(500f)
                }
            } else {
                holder.editView.visibility = View.INVISIBLE
                holder.editView.alpha = 0f
                holder.item.updateLayoutParams {
                    height = Utils.dp(150f)
                }
                holder.animatorHeight.start()
                item.tempIsAnimation = false
            }
            holder.delete.setOnClickListener {
                clickListener?.onDelete(holder.bindingAdapterPosition, item)
            }
            holder.edit.setOnClickListener {
                try {
                    clickListener?.onEdit(
                        holder.bindingAdapterPosition,
                        holder.editTitle.text.toString(),
                        holder.editDescription.text.toString(),
                        fixNumeric(holder.editCoins.text.toString()).toDouble(),
                        holder.editIsIncome.isChecked
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            if (!item.tempIsAnimation) {
                holder.editView.visibility = View.INVISIBLE
                holder.editView.alpha = 0f
                holder.item.updateLayoutParams {
                    height = Utils.dp(150f)
                }
            } else {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(500f)
                }
                holder.animatorAlphaReversed.start()
                item.tempIsAnimation = false
            }
            holder.delete.setOnClickListener {

            }
            holder.edit.setOnClickListener {

            }
        }
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<FinanceOperation>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onClick(position: Int, value: FinanceOperation)
        fun onLongClick(position: Int, value: FinanceOperation): Boolean
        fun onDelete(position: Int, value: FinanceOperation)
        fun onEdit(
            position: Int,
            title: String?,
            description: String?,
            coins: Double,
            isIncoming: Boolean
        )
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animatorHeight: ValueAnimator = ValueAnimator.ofInt(Utils.dp(150f), Utils.dp(500f))
        val animatorAlpha: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

        val animatorHeightReversed: ValueAnimator =
            ValueAnimator.ofInt(Utils.dp(500f), Utils.dp(150f))
        val animatorAlphaReversed: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)

        val title: MaterialTextView = itemView.findViewById(R.id.item_title)
        val description: MaterialTextView = itemView.findViewById(R.id.item_description)
        val coins: MaterialTextView = itemView.findViewById(R.id.item_coins)
        val createDate: MaterialTextView = itemView.findViewById(R.id.item_created)

        val editView: ViewGroup = itemView.findViewById(R.id.edit_view)
        val editTitle: TextInputEditText = itemView.findViewById(R.id.edit_title)
        val editDescription: TextInputEditText = itemView.findViewById(R.id.edit_description)
        val editCoins: TextInputEditText = itemView.findViewById(R.id.edit_coin)
        val editIsIncome: MaterialSwitch = itemView.findViewById(R.id.edit_income)
        val delete: MaterialButton = itemView.findViewById(R.id.delete)
        val edit: MaterialButton = itemView.findViewById(R.id.edit)
        val item: RoundCornerLinearView = itemView.findViewById(R.id.item_view)

        var isBinding = false


        init {
            animatorHeight.setDuration(300)
            animatorHeightReversed.setDuration(300)

            animatorAlpha.setDuration(300)
            animatorAlphaReversed.setDuration(300)

            animatorHeight.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    if (isBinding) {
                        return
                    }
                    animatorAlpha.start()
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animatorAlpha.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    if (isBinding) {
                        return
                    }
                    editView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animatorAlphaReversed.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    if (isBinding) {
                        return
                    }
                    editView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (isBinding) {
                        return
                    }
                    editView.visibility = View.INVISIBLE
                    animatorHeightReversed.start()
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animatorHeightReversed.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })

            animatorHeight.addUpdateListener { animation ->
                if (isBinding) {
                    return@addUpdateListener
                }
                item.updateLayoutParams {
                    height = animation.animatedValue as Int
                }
            }
            animatorHeightReversed.addUpdateListener { animation ->
                if (isBinding) {
                    return@addUpdateListener
                }
                item.updateLayoutParams {
                    height = animation.animatedValue as Int
                }
            }
            animatorAlpha.addUpdateListener { animation ->
                if (isBinding) {
                    return@addUpdateListener
                }
                editView.alpha = animation.animatedValue as Float
            }
            animatorAlphaReversed.addUpdateListener { animation ->
                if (isBinding) {
                    return@addUpdateListener
                }
                editView.alpha = animation.animatedValue as Float
            }
        }
    }
}
