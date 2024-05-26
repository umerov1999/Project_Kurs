package dev.umerov.project.fragment.filemanager

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso3.Picasso
import dev.umerov.project.Constants
import dev.umerov.project.R
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.FileItem
import dev.umerov.project.model.FileType
import dev.umerov.project.model.menu.options.FileManagerOption
import dev.umerov.project.picasso.PicassoInstance
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import java.io.File

class FileManagerAdapter(private var context: Context, private var data: List<FileItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var clickListener: ClickListener? = null
    private val messageBubbleColor = CurrentTheme.getMessageBubbleColor(context)
    private val primaryColor = CurrentTheme.getColorPrimary(context)

    fun setItems(data: List<FileItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            FileType.error, FileType.photo, FileType.text, FileType.audio -> return FileHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_file, parent, false)
            )

            FileType.folder -> return FileHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_folder, parent, false)
            )
        }
        return FileHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_manager_file, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    private fun fixNumerical(context: Context, num: Int): String? {
        if (num < 0) {
            return null
        }
        val preLastDigit = num % 100 / 10
        if (preLastDigit == 1) {
            return context.getString(R.string.files_count_c, num)
        }
        return when (num % 10) {
            1 -> context.getString(R.string.files_count_a, num)
            2, 3, 4 -> context.getString(R.string.files_count_b, num)
            else -> context.getString(R.string.files_count_c, num)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindFileHolder(holder as FileHolder, position)
    }

    private fun doFileMenu(position: Int, file: FileItem) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                FileManagerOption.open_with_item,
                context.getString(R.string.open_with),
                R.drawable.ic_external,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.share_item,
                context.getString(R.string.share),
                R.drawable.ic_share,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.update_file_time_item,
                context.getString(R.string.update_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.delete_item,
                context.getString(R.string.delete),
                R.drawable.ic_outline_delete,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.copy_item,
                context.getString(R.string.copy),
                R.drawable.content_copy,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.rename_item,
                context.getString(R.string.rename),
                R.drawable.pencil,
                false
            )
        )
        menus.header(
            file.file_name,
            R.drawable.file,
            "thumb_file://" + file.file_path
        )
        menus.columns(2)
        menus.show(
            (context as FragmentActivity).supportFragmentManager,
            "file_options", { _, option ->
                notifyItemChanged(position)
                when (option.id) {
                    FileManagerOption.delete_item -> {
                        clickListener?.onDelete(file)
                    }

                    FileManagerOption.open_with_item -> {
                        val intent_open = Intent(Intent.ACTION_VIEW)
                        intent_open.setDataAndType(
                            FileProvider.getUriForFile(
                                context,
                                Constants.FILE_PROVIDER_AUTHORITY,
                                File(file.file_path ?: return@show)
                            ), MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(File(file.file_path).extension)
                        ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent_open)
                    }

                    FileManagerOption.share_item -> {
                        val intent_send = Intent(Intent.ACTION_SEND)
                        intent_send.type = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(File(file.file_path ?: return@show).extension)
                        intent_send.putExtra(
                            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                context,
                                Constants.FILE_PROVIDER_AUTHORITY,
                                File(file.file_path)
                            )
                        ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent_send)
                    }

                    FileManagerOption.update_file_time_item -> {
                        clickListener?.onUpdateTimeFile(file)
                    }

                    FileManagerOption.copy_item -> {
                        clickListener?.onCopy(file)
                    }

                    FileManagerOption.rename_item -> {
                        clickListener?.onRename(file)
                    }

                    else -> {}
                }
            }, {
                notifyItemChanged(position)
            })
    }

    private fun doFolderMenu(position: Int, file: FileItem) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                FileManagerOption.delete_item,
                context.getString(R.string.delete),
                R.drawable.ic_outline_delete,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.fix_dir_time_item,
                context.getString(R.string.fix_dir_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.copy_item,
                context.getString(R.string.copy),
                R.drawable.content_copy,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileManagerOption.rename_item,
                context.getString(R.string.rename),
                R.drawable.pencil,
                false
            )
        )
        menus.columns(1)
        menus.show(
            (context as FragmentActivity).supportFragmentManager,
            "folder_options", { _, option ->
                notifyItemChanged(position)
                when (option.id) {
                    FileManagerOption.fix_dir_time_item -> {
                        clickListener?.onFixDir(file)
                    }

                    FileManagerOption.delete_item -> {
                        clickListener?.onDelete(file)
                    }

                    FileManagerOption.copy_item -> {
                        clickListener?.onCopy(file)
                    }

                    FileManagerOption.rename_item -> {
                        clickListener?.onRename(file)
                    }

                    else -> {}
                }
            }, {
                notifyItemChanged(position)
            })
    }

    private fun onBindFileHolder(holder: FileHolder, position: Int) {
        val item = data[position]

        if (item.type == FileType.text) {
            holder.icon.setBackgroundResource(R.drawable.document_text_outline_24)
        } else if (item.type != FileType.folder) {
            holder.icon.setBackgroundResource(R.drawable.file_fm)
        }
        holder.fileInfo.setBackgroundColor(messageBubbleColor)

        PicassoInstance.with()
            .load("thumb_file://${item.file_path}").tag(Constants.PICASSO_TAG)
            .priority(Picasso.Priority.LOW)
            .into(holder.icon)
        holder.fileName.text = item.file_name
        holder.fileDetails.text =
            if (item.type != FileType.folder) Utils.BytesToSize(item.size) else fixNumerical(
                holder.fileDetails.context,
                item.size.toInt()
            )
        holder.itemView.setOnClickListener {
            holder.fileInfo.setBackgroundColor(messageBubbleColor)
            clickListener?.onClick(holder.bindingAdapterPosition, item)
        }
        holder.itemView.setOnLongClickListener {
            holder.fileInfo.setBackgroundColor(primaryColor)
            if (item.type != FileType.folder) {
                doFileMenu(holder.bindingAdapterPosition, item)
            } else {
                doFolderMenu(holder.bindingAdapterPosition, item)
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onClick(position: Int, item: FileItem)
        fun onFixDir(item: FileItem)
        fun onUpdateTimeFile(item: FileItem)
        fun onDelete(item: FileItem)
        fun onCopy(item: FileItem)
        fun onRename(item: FileItem)
    }

    class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.item_file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.item_file_details)
        val icon: ImageView = itemView.findViewById(R.id.item_file_icon)
        val fileInfo: LinearLayout = itemView.findViewById(R.id.item_file_info)
    }
}