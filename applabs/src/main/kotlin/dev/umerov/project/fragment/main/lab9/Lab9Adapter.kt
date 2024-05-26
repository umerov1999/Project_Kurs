package dev.umerov.project.fragment.main.lab9

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.model.main.labs.Lab9Contact
import dev.umerov.project.util.Utils


class Lab9Adapter(private var data: List<Lab9Contact>) :
    RecyclerView.Adapter<Lab9Adapter.Holder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_lab9_contact, parent, false)
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
        holder.phone.text = holder.itemView.context.getString(R.string.phone_param, item.contact)
        holder.mail.text = holder.itemView.context.getString(R.string.mail_param, item.email)

        holder.editPhone.setText(item.contact)
        holder.editMail.setText(item.email)

        if (item.tempIsEditMode) {
            if (!item.tempIsAnimation) {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(280f)
                }
            } else {
                holder.editView.visibility = View.INVISIBLE
                holder.editView.alpha = 0f
                holder.item.updateLayoutParams {
                    height = Utils.dp(70f)
                }
                holder.animatorHeight.start()
                item.tempIsAnimation = false
            }
            holder.delete.setOnClickListener {
                clickListener?.onDelete(holder.bindingAdapterPosition, item)
            }
            holder.edit.setOnClickListener {
                clickListener?.onEdit(
                    holder.bindingAdapterPosition,
                    holder.editPhone.text.toString(),
                    holder.editMail.text.toString()
                )
            }
        } else {
            if (!item.tempIsAnimation) {
                holder.editView.visibility = View.INVISIBLE
                holder.editView.alpha = 0f
                holder.item.updateLayoutParams {
                    height = Utils.dp(70f)
                }
            } else {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(280f)
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

    fun setData(data: List<Lab9Contact>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onClick(position: Int, value: Lab9Contact)
        fun onDelete(position: Int, value: Lab9Contact)
        fun onEdit(position: Int, contact: String?, mail: String?)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animatorHeight: ValueAnimator = ValueAnimator.ofInt(Utils.dp(70f), Utils.dp(280f))
        val animatorAlpha: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

        val animatorHeightReversed: ValueAnimator =
            ValueAnimator.ofInt(Utils.dp(280f), Utils.dp(70f))
        val animatorAlphaReversed: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)

        val phone: MaterialTextView = itemView.findViewById(R.id.item_contact)
        val mail: MaterialTextView = itemView.findViewById(R.id.item_mail)

        val editView: ViewGroup = itemView.findViewById(R.id.edit_view)
        val editPhone: TextInputEditText = itemView.findViewById(R.id.edit_phone)
        val editMail: TextInputEditText = itemView.findViewById(R.id.edit_mail)
        val delete: MaterialButton = itemView.findViewById(R.id.delete)
        val edit: MaterialButton = itemView.findViewById(R.id.edit)
        val item: ViewGroup = itemView.findViewById(R.id.item_view)

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