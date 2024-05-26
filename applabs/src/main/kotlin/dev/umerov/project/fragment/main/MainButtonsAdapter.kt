package dev.umerov.project.fragment.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.Constants
import dev.umerov.project.R
import dev.umerov.project.model.main.MainButton

class MainButtonsAdapter(private var data: List<MainButton>) :
    RecyclerView.Adapter<MainButtonsAdapter.ButtonHolder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonHolder {
        return ButtonHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_main_button, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ButtonHolder, position: Int) {
        val button = data[position]
        if (button.textRes == Constants.DISABLED_RESOURCE_ID) {
            holder.button.text = button.text
        } else {
            holder.button.setText(button.textRes)
        }

        if (button.mainTextRes == Constants.DISABLED_RESOURCE_ID) {
            holder.title.text = button.mainText
            if (button.mainText.isNullOrEmpty()) {
                holder.title.visibility = View.GONE
            } else {
                holder.title.visibility = View.VISIBLE
            }
        } else {
            holder.title.setText(button.mainTextRes)
            holder.title.visibility = View.VISIBLE
        }
        holder.button.setOnClickListener {
            clickListener?.onClick(button)
        }
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: Array<MainButton>) {
        this.data = listOf(*data)
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onClick(value: MainButton)
    }

    class ButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: MaterialButton = itemView.findViewById(R.id.item_button)
        val title: MaterialTextView = itemView.findViewById(R.id.item_title)
    }
}
