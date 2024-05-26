package dev.umerov.project.fragment.main.shoppinglist

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.model.main.labs.ShoppingList
import dev.umerov.project.util.AppTextUtils
import dev.umerov.project.util.Utils
import dev.umerov.project.view.RoundCornerLinearView
import dev.umerov.project.view.ShoppingListTextInputEditText

class ShoppingListAdapter(private var data: List<ShoppingList>) :
    RecyclerView.Adapter<ShoppingListAdapter.Holder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_shopping_list, parent, false)
        )
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
        holder.created.text =
            AppTextUtils.getDateFromUnixTimeShorted(holder.itemView.context, item.creationDate)
        holder.title.text = holder.itemView.context.getString(R.string.title_param, item.title)
        holder.description.text =
            holder.itemView.context.getString(R.string.description_param, item.description)
        holder.purchase.text =
            holder.itemView.context.getString(R.string.purchase_done, item.db_Purchase)
        holder.planned_purchase.text =
            holder.itemView.context.getString(R.string.all_purchase, item.db_plannedPurchase)
        holder.editTitle.setText(item.title)
        holder.editDescription.setText(item.description)
        holder.item.setViewColor(item.color)

        if (item.tempIsEditMode) {
            if (!item.tempIsAnimation) {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(340f)
                }
            } else {
                holder.editView.visibility = View.INVISIBLE
                holder.editView.alpha = 0f
                holder.item.updateLayoutParams {
                    height = Utils.dp(140f)
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
                        holder.editDescription.text.toString()
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
                    height = Utils.dp(140f)
                }
            } else {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(340f)
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

    fun setData(data: List<ShoppingList>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onClick(position: Int, value: ShoppingList)
        fun onLongClick(position: Int, value: ShoppingList): Boolean
        fun onDelete(position: Int, value: ShoppingList)
        fun onEdit(position: Int, title: String?, description: String?)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animatorHeight: ValueAnimator = ValueAnimator.ofInt(Utils.dp(140f), Utils.dp(340f))
        val animatorAlpha: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

        val animatorHeightReversed: ValueAnimator =
            ValueAnimator.ofInt(Utils.dp(340f), Utils.dp(140f))
        val animatorAlphaReversed: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)

        val created: MaterialTextView = itemView.findViewById(R.id.item_created)
        val title: MaterialTextView = itemView.findViewById(R.id.item_title)
        val description: MaterialTextView = itemView.findViewById(R.id.item_description)
        val purchase: MaterialTextView = itemView.findViewById(R.id.item_purchase)
        val planned_purchase: MaterialTextView = itemView.findViewById(R.id.item_planned_purchase)

        val editView: ViewGroup = itemView.findViewById(R.id.edit_view)
        val editTitle: ShoppingListTextInputEditText = itemView.findViewById(R.id.edit_title)
        val editDescription: ShoppingListTextInputEditText =
            itemView.findViewById(R.id.edit_description)
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
