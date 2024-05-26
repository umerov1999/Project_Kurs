package dev.umerov.project.model.menu.options

import androidx.annotation.IntDef

@IntDef(
    FileManagerOption.share_item,
    FileManagerOption.open_with_item,
    FileManagerOption.fix_dir_time_item,
    FileManagerOption.update_file_time_item,
    FileManagerOption.delete_item,
    FileManagerOption.copy_item,
    FileManagerOption.rename_item
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class FileManagerOption {
    companion object {
        const val share_item = 1
        const val open_with_item = 2
        const val fix_dir_time_item = 3
        const val update_file_time_item = 4
        const val delete_item = 5
        const val copy_item = 6
        const val rename_item = 7
    }
}