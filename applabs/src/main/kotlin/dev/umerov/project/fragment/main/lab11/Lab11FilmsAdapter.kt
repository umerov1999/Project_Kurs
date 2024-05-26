package dev.umerov.project.fragment.main.lab11

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso3.Picasso
import dev.umerov.project.Constants
import dev.umerov.project.R
import dev.umerov.project.model.main.labs.Lab11Film
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.nonNullNoEmpty
import dev.umerov.project.picasso.PicassoInstance
import dev.umerov.project.util.Utils
import dev.umerov.project.view.Lab11GenreSelect

class Lab11FilmsAdapter(private var data: List<Lab11Film>) :
    RecyclerView.Adapter<Lab11FilmsAdapter.Holder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_lab11_film, parent, false)
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
        holder.title.text = holder.itemView.context.getString(R.string.title_param, item.title)
        holder.genre.text =
            holder.itemView.context.getString(R.string.genre_param, item.genre?.name)
        holder.year.text = holder.itemView.context.getString(R.string.year_param, item.year)
        holder.editTitle.setText(item.title)
        holder.editGenre.setSelected(item.genre)
        holder.editYear.setText(item.year.toString())

        if (item.thumbPath.nonNullNoEmpty()) {
            PicassoInstance.with()
                .load("thumb_file://${item.thumbPath}").tag(Constants.PICASSO_TAG)
                .priority(Picasso.Priority.LOW)
                .into(holder.thumb)
        } else {
            PicassoInstance.with().cancelRequest(holder.thumb)
            holder.thumb.setImageResource(R.drawable.image)
        }

        if (item.tempIsEditMode) {
            if (!item.tempIsAnimation) {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(540f)
                }
            } else {
                holder.editView.visibility = View.INVISIBLE
                holder.editView.alpha = 0f
                holder.item.updateLayoutParams {
                    height = Utils.dp(240f)
                }
                holder.animatorHeight.start()
                item.tempIsAnimation = false
            }
            holder.delete.setOnClickListener {
                clickListener?.onDelete(holder.bindingAdapterPosition, item)
            }
            holder.selectPicture.setOnClickListener {
                clickListener?.onSelectPicture(position, item)
            }
            holder.edit.setOnClickListener {
                try {
                    clickListener?.onEdit(
                        holder.bindingAdapterPosition,
                        holder.editTitle.text.toString(),
                        holder.editGenre.selected,
                        holder.editYear.text.toString().toInt(),
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
                    height = Utils.dp(240f)
                }
            } else {
                holder.editView.visibility = View.VISIBLE
                holder.editView.alpha = 1f
                holder.item.updateLayoutParams {
                    height = Utils.dp(540f)
                }
                holder.animatorAlphaReversed.start()
                item.tempIsAnimation = false
            }
            holder.delete.setOnClickListener {

            }
            holder.edit.setOnClickListener {

            }
            holder.selectPicture.setOnClickListener {

            }
        }
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<Lab11Film>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onClick(position: Int, value: Lab11Film)
        fun onLongClick(position: Int, value: Lab11Film): Boolean
        fun onDelete(position: Int, value: Lab11Film)
        fun onEdit(position: Int, title: String?, genre: Lab11Genre?, year: Int)
        fun onSelectPicture(position: Int, value: Lab11Film)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animatorHeight: ValueAnimator = ValueAnimator.ofInt(Utils.dp(240f), Utils.dp(540f))
        val animatorAlpha: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

        val animatorHeightReversed: ValueAnimator =
            ValueAnimator.ofInt(Utils.dp(540f), Utils.dp(240f))
        val animatorAlphaReversed: ValueAnimator = ValueAnimator.ofFloat(1f, 0f)

        val title: MaterialTextView = itemView.findViewById(R.id.item_title)
        val genre: MaterialTextView = itemView.findViewById(R.id.item_genre)
        val year: MaterialTextView = itemView.findViewById(R.id.item_year)
        val thumb: ShapeableImageView = itemView.findViewById(R.id.item_thumb)

        val editView: ViewGroup = itemView.findViewById(R.id.edit_view)
        val editTitle: TextInputEditText = itemView.findViewById(R.id.edit_title)
        val editGenre: Lab11GenreSelect = itemView.findViewById(R.id.edit_genre)
        val editYear: TextInputEditText = itemView.findViewById(R.id.edit_year)
        val delete: MaterialButton = itemView.findViewById(R.id.delete)
        val edit: MaterialButton = itemView.findViewById(R.id.edit)
        val selectPicture: MaterialButton = itemView.findViewById(R.id.select_image)
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
