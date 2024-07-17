package dev.umerov.project.fragment.filemanager

import android.annotation.SuppressLint
import android.os.Environment
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager_SavedState
import dev.umerov.project.Includes
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.FileItem
import dev.umerov.project.model.FileType
import dev.umerov.project.settings.Settings
import dev.umerov.project.util.Objects.safeEquals
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.umerov.project.util.coroutines.CoroutinesUtils.syncSingle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FilenameFilter
import java.util.Locale

class FileManagerPresenter(
    private var path: File,
    private val base: Boolean
) : RxSupportPresenter<IFileManagerView>() {
    private val fileList: ArrayList<FileItem> = ArrayList()
    private val fileListSearch: ArrayList<FileItem> = ArrayList()
    private var isLoading = false
    private val basePath = path.absolutePath
    private val directoryScrollPositions = HashMap<String, Parcelable>()
    private var copied: FileItem? = null

    private val filter: FilenameFilter = FilenameFilter { dir, filename ->
        val sel = File(dir, filename)
        if (sel.absolutePath == File(
                Environment.getExternalStorageDirectory(),
                "Android"
            ).absolutePath
        ) {
            return@FilenameFilter false
        }
        !sel.isHidden && sel.canRead()
    }
    private var q: String? = null

    private fun copyFile(inputFile: File, outputFile: File) {
        val source = inputFile.source().buffer()
        val sink = outputFile.sink().buffer()
        source.use { input ->
            sink.use { output ->
                output.writeAll(input)
            }
        }
    }

    private fun copyDir(fromDir: File, toDir: File, copyTo: Boolean) {
        var toDirMade = toDir
        if (copyTo) {
            toDirMade = File(toDir, fromDir.name)
        }
        if (!toDirMade.exists()) {
            toDirMade.mkdirs()
        }
        if (fromDir.exists() && fromDir.canRead() && toDirMade.exists() && toDirMade.canRead()) {
            val fList = fromDir.list(filter)
            if (fList != null) {
                for (i in fList.indices) {
                    val file = File(fromDir, fList[i])
                    if (file.isDirectory) {
                        File(toDirMade, fList[i]).mkdir()
                        copyDir(file, File(toDirMade, fList[i]), false)
                    } else {
                        copyFile(file, File(toDirMade, fList[i]))
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun fireCopy(from: File, toDir: File) {
        isLoading = true
        view?.resolveLoading(isLoading)
        flow {
            if (from.isDirectory) {
                copyDir(from, toDir, true)
            } else {
                copyFile(from, File(toDir, from.name))
            }
            emit(true)
        }.fromIOToMain({
            isLoading = false
            view?.resolveLoading(isLoading)
            view?.showMessage(R.string.success)
            loadFiles(back = false, caches = false, fromCache = false)
        }, {
            view?.showThrowable(it)
        })
    }

    fun fireSetCopied(copied: FileItem) {
        this.copied = copied
        view?.updateAddButton(this.copied != null)
    }

    fun fireStore(file: FileItem) {
        if (file.isTmpNew) {
            file.tmpNewName?.let {
                File(path, it).mkdirs()
                loadFiles(back = false, caches = false, fromCache = false)
            }
        } else {
            file.file_path?.let {
                file.tmpNewName?.let { it1 -> File(path, it1) }
                    ?.let { it2 ->
                        File(it).renameTo(it2)
                        loadFiles(back = false, caches = false, fromCache = false)
                    }
            }
        }
    }

    fun fireAddClick() {
        if (copied != null) {
            val tmpCopied = copied
            copied = null
            view?.updateAddButton(this.copied != null)
            tmpCopied?.file_path?.let { File(it) }?.let { fireCopy(it, path) }
        } else {
            view?.displayCreateDialog(
                FileItem(
                    FileType.folder,
                    null,
                    null,
                    null,
                    null,
                    0,
                    0,
                    false
                ).setIsTmpNew(true)
            )
        }
    }

    fun canRefresh(): Boolean {
        return q == null
    }

    fun doSearch(str: String?, global: Boolean) {
        if (isLoading) {
            return
        }
        val query = str?.trim { it <= ' ' }
        if (safeEquals(query, this.q) && !global) {
            return
        }
        q = if (query.isNullOrEmpty()) {
            null
        } else {
            query
        }
        if (q == null) {
            fileListSearch.clear()
            view?.resolveEmptyText(fileList.isEmpty())
            view?.displayData(fileList)
            view?.updatePathString(path.absolutePath)
        } else {
            fileListSearch.clear()
            if (!global) {
                for (i in fileList) {
                    if (i.file_name.isNullOrEmpty()) {
                        continue
                    }
                    if (i.file_name.lowercase(Locale.getDefault())
                            .contains(q?.lowercase(Locale.getDefault()).toString())
                    ) {
                        fileListSearch.add(i)
                    }
                }
                view?.resolveEmptyText(fileListSearch.isEmpty())
                view?.displayData(fileListSearch)
                view?.updatePathString(q ?: return)
            } else {
                isLoading = true
                view?.resolveEmptyText(false)
                view?.resolveLoading(isLoading)
                appendJob(rxSearchFiles().fromIOToMain({
                    fileListSearch.clear()
                    fileListSearch.addAll(it)
                    isLoading = false
                    view?.resolveEmptyText(fileListSearch.isEmpty())
                    view?.resolveLoading(isLoading)
                    view?.displayData(fileListSearch)
                    view?.updatePathString(q ?: return@fromIOToMain)
                }, {
                    view?.showThrowable(it)
                    isLoading = false
                    view?.resolveLoading(isLoading)
                }))
            }
        }
    }

    override fun onGuiCreated(viewHost: IFileManagerView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(if (q == null) fileList else fileListSearch)
        (if (q == null) path.absolutePath else q)?.let { viewHost.updatePathString(it) }
        viewHost.resolveEmptyText(if (q == null) fileList.isEmpty() else fileListSearch.isEmpty())
        viewHost.resolveLoading(isLoading)
        viewHost.updateAddButton(copied != null)
    }

    private class ItemModificationComparator : Comparator<FileItem> {
        override fun compare(lhs: FileItem, rhs: FileItem): Int {
            return rhs.modification.compareTo(lhs.modification)
        }
    }

    fun backupDirectoryScroll(scroll: Parcelable) {
        directoryScrollPositions[path.absolutePath] = scroll
    }

    fun loadUp() {
        if (isLoading) {
            if (q == null) {
                val parent = path.parentFile
                if (parent != null && parent.canRead()) {
                    view?.onBusy(parent.absolutePath)
                }
            }
            return
        }
        if (q != null) {
            q = null
            view?.updatePathString(path.absolutePath)
            view?.displayData(fileList)
            view?.resolveEmptyText(fileList.isEmpty())
            return
        }
        val parent = path.parentFile
        if (parent != null && parent.canRead()) {
            path = parent
            view?.updatePathString(path.absolutePath)
            loadFiles(back = true, caches = true, fromCache = false)
        }
    }

    fun setCurrent(file: File) {
        if (isLoading) {
            if (file.canRead()) {
                view?.onBusy(file.absolutePath)
            }
            return
        }
        if (file.canRead()) {
            path = file
            view?.updatePathString(path.absolutePath)
            loadFiles(back = false, caches = true, fromCache = false)
        }
    }

    fun canLoadUp(): Boolean {
        if (q != null) {
            return true
        }
        val parent = path.parentFile
        if (base && path.absolutePath == basePath) {
            return false
        }
        return parent != null && parent.canRead()
    }

    private fun loadCache(back: Boolean) {
        isLoading = true
        view?.resolveEmptyText(false)
        view?.resolveLoading(isLoading)
        appendJob(
            Includes.stores.projectStore().getFiles(path.absolutePath).fromIOToMain({
                fileList.clear()
                fileList.addAll(it)
                view?.resolveEmptyText(fileList.isEmpty())
                view?.notifyAllChanged()
                directoryScrollPositions.remove(path.absolutePath)?.let { scroll ->
                    view?.restoreScroll(scroll)
                } ?: view?.restoreScroll(LinearLayoutManager_SavedState())
                if (back && fileList.isEmpty() || !back) {
                    loadFiles(
                        back = false, caches = false, fromCache = true
                    )
                } else {
                    isLoading = false
                    view?.resolveLoading(isLoading)
                }
            }, {
                view?.restoreScroll(LinearLayoutManager_SavedState())
                view?.showThrowable(it)
                loadFiles(
                    back = false, caches = false, fromCache = true
                )
            })
        )
    }

    fun loadFiles(back: Boolean, caches: Boolean, fromCache: Boolean) {
        if (isLoading && !fromCache) {
            return
        }
        if (caches) {
            loadCache(back)
            return
        }
        view?.resolveEmptyText(false)
        if (!fromCache) {
            isLoading = true
            view?.resolveLoading(isLoading)
        }
        appendJob(rxLoadFileList().fromIOToMain({
            fileList.clear()
            fileList.addAll(it)
            isLoading = false
            view?.resolveEmptyText(fileList.isEmpty())
            view?.resolveLoading(isLoading)
            view?.notifyAllChanged()
        }, {
            view?.showThrowable(it)
            isLoading = false
            view?.resolveLoading(isLoading)
        }))
    }

    private fun rxSearchFiles(): Flow<ArrayList<FileItem>> {
        return flow {
            val fileListTmp = ArrayList<FileItem>()
            searchFile(fileListTmp, path)
            val dirsList = ArrayList<FileItem>()
            val flsList = ArrayList<FileItem>()
            for (i in fileListTmp) {
                if (i.type == FileType.folder) dirsList.add(i) else flsList.add(i)
            }
            dirsList.sortWith(ItemModificationComparator())
            flsList.sortWith(ItemModificationComparator())
            fileListTmp.clear()
            fileListTmp.addAll(dirsList)
            fileListTmp.addAll(flsList)
            emit(fileListTmp)
        }
    }

    private fun searchFile(files: ArrayList<FileItem>, dir: File) {
        if (dir.exists() && dir.canRead()) {
            val fList = dir.list(filter)
            if (fList != null) {
                for (i in fList.indices) {
                    // Convert into file path
                    val file = File(dir, fList[i])
                    if (file.isDirectory) {
                        searchFile(files, file)
                    }
                    val canRead = file.canRead()
                    val mod = file.lastModified()

                    if (file.name.lowercase(Locale.getDefault())
                            .contains(q?.lowercase(Locale.getDefault()).toString())
                    ) {
                        files.add(
                            FileItem(
                                getExt(file),
                                fList[i],
                                file.absolutePath,
                                dir.name,
                                dir.absolutePath,
                                mod,
                                if (file.isDirectory) getFolderFilesCount(file) else file.length(),
                                canRead
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getFolderFilesCount(file: File): Long {
        if (!Settings.get().main().isEnable_dirs_files_count) {
            return -1
        }
        return file.list()?.size?.toLong() ?: -1
    }

    private fun rxLoadFileList(): Flow<ArrayList<FileItem>> {
        return flow {
            val fileListTmp = ArrayList<FileItem>()
            if (path.exists() && path.canRead()) {
                val fList = path.list(filter)
                if (fList != null) {
                    for (i in fList.indices) {
                        // Convert into file path
                        val file = File(path, fList[i])
                        val canRead = file.canRead()
                        val mod = file.lastModified()

                        fileListTmp.add(
                            i,
                            FileItem(
                                getExt(file),
                                fList[i],
                                file.absolutePath,
                                path.name,
                                path.absolutePath,
                                mod,
                                if (file.isDirectory) getFolderFilesCount(file) else file.length(),
                                canRead
                            )
                        )
                    }
                    val dirsList = ArrayList<FileItem>()
                    val flsList = ArrayList<FileItem>()
                    for (i in fileListTmp) {
                        if (i.type == FileType.folder) dirsList.add(i) else flsList.add(i)
                    }
                    dirsList.sortWith(ItemModificationComparator())
                    flsList.sortWith(ItemModificationComparator())
                    fileListTmp.clear()
                    fileListTmp.addAll(dirsList)
                    fileListTmp.addAll(flsList)
                }
            }
            Includes.stores.projectStore().insertFiles(path.absolutePath, fileListTmp)
                .syncSingle()
            emit(fileListTmp)
        }
    }

    private fun doFixDirTime(dir: String, isRoot: Boolean) {
        val root = File(dir)
        val list: ArrayList<Long> = ArrayList()
        if (root.exists() && root.isDirectory) {
            val children = root.list()
            if (children != null) {
                for (child in children) {
                    val rem = File(root, child)
                    if (rem.isFile && !rem.isHidden && !isRoot) {
                        list.add(rem.lastModified())
                    } else if (rem.isDirectory && !rem.isHidden && rem.name != "." && rem.name != "..") {
                        doFixDirTime(rem.absolutePath, false)
                    }
                }
            }
        } else {
            return
        }
        if (isRoot) {
            return
        }

        val res = list.maxOrNull()
        res?.let {
            root.setLastModified(it)
        }
    }

    private fun fixDirTimeRx(dir: String): Flow<Boolean> {
        return flow {
            doFixDirTime(dir, true)
            emit(true)
        }
    }

    @SuppressLint("CheckResult")
    fun fireFixDirTime(dir: String) {
        if (isLoading) {
            return
        }
        isLoading = true
        view?.resolveLoading(isLoading)
        fixDirTimeRx(dir).fromIOToMain({
            view?.showMessage(R.string.success)
            isLoading = false
            loadFiles(back = false, caches = false, fromCache = false)
        }, { view?.showThrowable(it) })
    }

    private fun deleteRecursive(dir: String) {
        val fDir = File(dir)
        if (fDir.exists() && fDir.isDirectory) {
            val children = fDir.list()
            if (children != null) {
                for (child in children) {
                    val rem = File(fDir, child)
                    if (rem.isFile) {
                        rem.delete()
                    } else if (rem.isDirectory && rem.name != "." && rem.name != "..") {
                        deleteRecursive(rem.absolutePath)
                        rem.delete()
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun fireDelete(item: FileItem) {
        val tmpFl = File(item.file_path ?: return)
        when (tmpFl.absolutePath) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath -> {
                view?.showError(R.string.cannot_delete_public_dir)
                return
            }

            else -> {

            }
        }
        isLoading = true
        view?.resolveLoading(isLoading)
        flow {
            if (item.type != FileType.folder) {
                if (File(item.file_path).delete()) {
                    emit(true)
                } else {
                    throw Throwable("Can't Delete File")
                }
            } else {
                deleteRecursive(item.file_path)
                if (File(item.file_path).delete()) {
                    emit(true)
                } else {
                    throw Throwable("Can't Delete Folder")
                }
            }
        }.fromIOToMain({
            isLoading = false
            view?.resolveLoading(isLoading)
            view?.showMessage(R.string.success)
            loadFiles(back = false, caches = false, fromCache = false)
        }, {
            view?.showThrowable(it)
        })
    }

    @FileType
    private fun getExt(file: File): Int {
        if (file.isDirectory) {
            return FileType.folder
        }
        for (i in Settings.get().main().photoExt) {
            if (file.extension.contains(i, true)) {
                return FileType.photo
            }
        }
        for (i in arrayOf("json", "txt", "html")) {
            if (file.extension.contains(i, true)) {
                return FileType.text
            }
        }
        for (i in Settings.get().main().audioExt) {
            if (file.extension.contains(i, true)) {
                return FileType.audio
            }
        }
        return FileType.error
    }

    init {
        loadFiles(back = false, caches = true, fromCache = false)
    }
}
