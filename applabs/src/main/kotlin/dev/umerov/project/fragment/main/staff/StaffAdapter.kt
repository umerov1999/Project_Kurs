package dev.umerov.project.fragment.main.staff

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.model.Human
import dev.umerov.project.module.BufferWriteNative
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.module.thorvg.ThorVGRender
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.view.RoundCornerLinearView

class StaffAdapter(mContext: Context, private var data: List<Human>) :
    RecyclerView.Adapter<StaffAdapter.Holder>() {
    private var clickListener: ClickListener? = null
    private var maleIcon: BitmapDrawable? = null
    private var femaleIcon: BitmapDrawable? = null
    private val colorSurface = CurrentTheme.getColorSurface(mContext)
    private val colorPrimary = CurrentTheme.getColorPrimary(mContext)

    init {
        val bufferMale = BufferWriteNative(8192)
        bufferMale.putStream(ProjectNative.appContext.assets.open("male-svgrepo-com.svg_lz4"))
        maleIcon =
            BitmapDrawable(mContext.resources, ThorVGRender.createBitmap(bufferMale, 100, 100))

        val bufferFemale = BufferWriteNative(8192)
        bufferFemale.putStream(ProjectNative.appContext.assets.open("female-svgrepo-com.svg_lz4"))
        femaleIcon =
            BitmapDrawable(mContext.resources, ThorVGRender.createBitmap(bufferFemale, 100, 100))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_staff, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = data[position]
        holder.container.setOnClickListener {
            clickListener?.onClick(holder.bindingAdapterPosition, item)
        }
        holder.gender.setImageDrawable(if (item.gender) maleIcon else femaleIcon)
        holder.firstname.text = item.firstName
        holder.lastname.text = item.lastName
        holder.born.text = item.birthDayString
        holder.container.setViewColor(if (item.tempIsMenuOpen) colorPrimary else colorSurface)
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<Human>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onClick(position: Int, value: Human)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: RoundCornerLinearView = itemView.findViewById(R.id.item_view)
        val gender: ShapeableImageView = itemView.findViewById(R.id.item_gender)
        val firstname: MaterialTextView = itemView.findViewById(R.id.item_firstname)
        val lastname: MaterialTextView = itemView.findViewById(R.id.item_lastname)
        val born: MaterialTextView = itemView.findViewById(R.id.item_born)
    }
}
