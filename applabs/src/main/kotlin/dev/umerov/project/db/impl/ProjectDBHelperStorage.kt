package dev.umerov.project.db.impl

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import androidx.annotation.IdRes
import dev.umerov.project.R
import dev.umerov.project.db.ProjectDBHelper
import dev.umerov.project.db.column.FilesColumns
import dev.umerov.project.db.column.Lab11FilmsColumns
import dev.umerov.project.db.column.Lab11GenresColumns
import dev.umerov.project.db.column.Lab14PlaylistColumns
import dev.umerov.project.db.column.SearchRequestColumns
import dev.umerov.project.db.interfaces.IProjectDBHelperStorage
import dev.umerov.project.getBoolean
import dev.umerov.project.getInt
import dev.umerov.project.getLong
import dev.umerov.project.getString
import dev.umerov.project.model.FileItem
import dev.umerov.project.model.FileType
import dev.umerov.project.model.main.labs.Lab11Film
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.model.main.labs.Lab14AudioAlbum
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class ProjectDBHelperStorage internal constructor(context: Context) :
    IProjectDBHelperStorage {
    private val app: Context = context.applicationContext
    private val helper: ProjectDBHelper by lazy {
        ProjectDBHelper(app)
    }

    override fun getQueries(sourceId: Int): Single<List<String>> {
        return Single.fromCallable {
            val where = SearchRequestColumns.SOURCE_ID + " = ?"
            val args = arrayOf(sourceId.toString())
            val cursor = helper.writableDatabase.query(
                SearchRequestColumns.TABLENAME,
                QUERY_PROJECTION, where, args, null, null, BaseColumns._ID + " DESC"
            )
            val data: MutableList<String> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    data.add(it.getString(SearchRequestColumns.QUERY) ?: return@use)
                }
            }
            data
        }
    }

    override fun insertQuery(sourceId: Int, query: String?): Completable {
        if (query == null) {
            return Completable.complete()
        }
        val queryClean = query.trim { it <= ' ' }
        return if (queryClean.isEmpty()) {
            Completable.complete()
        } else Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            db.delete(
                SearchRequestColumns.TABLENAME,
                SearchRequestColumns.QUERY + " = ?", arrayOf(queryClean)
            )
            try {
                val cv = ContentValues()
                cv.put(SearchRequestColumns.SOURCE_ID, sourceId)
                cv.put(SearchRequestColumns.QUERY, queryClean)
                db.insert(SearchRequestColumns.TABLENAME, null, cv)
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun getFiles(parent: String): Single<List<FileItem>> {
        return Single.fromCallable {
            val where = FilesColumns.PARENT_DIR + " = ?"
            val args = arrayOf(parent)
            val cursor = helper.writableDatabase.query(
                FilesColumns.TABLENAME,
                FILES_PROJECTION,
                where,
                args,
                null,
                null,
                FilesColumns.IS_DIR + " DESC," + FilesColumns.MODIFICATIONS + " DESC"
            )
            val data: MutableList<FileItem> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    data.add(
                        FileItem(
                            it.getInt(FilesColumns.TYPE),
                            it.getString(FilesColumns.FILE_NAME),
                            it.getString(FilesColumns.FILE_PATH),
                            it.getString(FilesColumns.PARENT_NAME),
                            it.getString(FilesColumns.PARENT_PATH),
                            it.getLong(FilesColumns.MODIFICATIONS),
                            it.getLong(FilesColumns.SIZE),
                            it.getBoolean(FilesColumns.CAN_READ)
                        )
                    )
                }
            }
            data
        }
    }

    override fun insertFiles(parent: String, files: List<FileItem>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            db.delete(
                FilesColumns.TABLENAME,
                FilesColumns.PARENT_DIR + " = ?", arrayOf(parent)
            )
            try {
                for (i in files) {
                    val cv = ContentValues()
                    cv.put(FilesColumns.PARENT_DIR, parent)
                    cv.put(FilesColumns.TYPE, i.type)
                    cv.put(FilesColumns.IS_DIR, if (i.type == FileType.folder) 1 else 0)
                    cv.put(FilesColumns.FILE_NAME, i.file_name)
                    cv.put(FilesColumns.FILE_PATH, i.file_path)
                    cv.put(FilesColumns.PARENT_NAME, i.parent_name)
                    cv.put(FilesColumns.PARENT_PATH, i.parent_path)
                    cv.put(FilesColumns.MODIFICATIONS, i.modification)
                    cv.put(FilesColumns.SIZE, i.size)
                    cv.put(FilesColumns.CAN_READ, if (i.isCanRead) 1 else 0)
                    db.insert(FilesColumns.TABLENAME, null, cv)
                }
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun clearQueriesAll() {
        helper.writableDatabase.delete(SearchRequestColumns.TABLENAME, null, null)
    }

    override fun clearFilesAll() {
        helper.writableDatabase.delete(FilesColumns.TABLENAME, null, null)
    }

    override fun deleteQuery(sourceId: Int): Completable {
        return Completable.fromAction {
            helper.writableDatabase.delete(
                SearchRequestColumns.TABLENAME,
                SearchRequestColumns.SOURCE_ID + " = ?", arrayOf(sourceId.toString())
            )
        }
    }

    override fun getFilms(): Single<ArrayList<Lab11Film>> {
        return Single.create { emitter: SingleEmitter<ArrayList<Lab11Film>> ->
            val db = helper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT ${Lab11FilmsColumns.FULL_ID}, " +
                        "${Lab11FilmsColumns.FULL_TITLE}, " +
                        "${Lab11FilmsColumns.FULL_YEAR}, " +
                        "${Lab11FilmsColumns.THUMB_PATH}, " +
                        "${Lab11GenresColumns.FULL_ID}, " +
                        "${Lab11GenresColumns.FULL_NAME} " +
                        "FROM ${Lab11FilmsColumns.TABLENAME} " +
                        "LEFT JOIN ${Lab11GenresColumns.TABLENAME} ON ${Lab11FilmsColumns.FULL_GENRE_ID} = ${Lab11GenresColumns.FULL_ID} " +
                        "ORDER BY ${Lab11FilmsColumns.FULL_ID} DESC;",
                emptyArray()
            )
            val ret = ArrayList<Lab11Film>()
            cursor.use {
                while (it.moveToNext()) {
                    val s = Lab11Film()
                    s.setDBId(it.getLong(0))
                        .setTitle(it.getString(1))
                        .setYear(it.getInt(2))
                        .setThumbPath(it.getString(3))
                    if (!it.isNull(4)) {
                        s.setGenre(Lab11Genre().setDBId(it.getLong(4)).setName(it.getString(5)))
                    }
                    ret.add(s)
                }
            }
            emitter.onSuccess(ret)
        }
    }

    override fun getGenres(): Single<ArrayList<Lab11Genre>> {
        return Single.create { emitter: SingleEmitter<ArrayList<Lab11Genre>> ->
            val db = helper.readableDatabase

            val cursor = db.query(
                Lab11GenresColumns.TABLENAME,
                GENRE_PROJECTION,
                null,
                null,
                null,
                null,
                BaseColumns._ID + " DESC"
            )
            val ret = ArrayList<Lab11Genre>()
            cursor.use {
                while (it.moveToNext()) {
                    ret.add(
                        Lab11Genre().setDBId(it.getLong(BaseColumns._ID))
                            .setName(it.getString(Lab11GenresColumns.NAME))
                    )
                }
            }
            emitter.onSuccess(ret)
        }
    }

    override fun getPlaylists(@IdRes checkedItem: Int): Single<ArrayList<Lab14AudioAlbum>> {
        return Single.create { emitter: SingleEmitter<ArrayList<Lab14AudioAlbum>> ->
            val db = helper.readableDatabase
            val cursor = when (checkedItem) {
                R.id.sort_by_artist -> {
                    db.query(
                        Lab14PlaylistColumns.TABLENAME,
                        PLAYLIST_PROJECTION,
                        null,
                        null,
                        null,
                        null,
                        Lab14PlaylistColumns.ARTIST
                    )
                }

                R.id.sort_by_year -> {
                    db.query(
                        Lab14PlaylistColumns.TABLENAME,
                        PLAYLIST_PROJECTION,
                        null,
                        null,
                        null,
                        null,
                        Lab14PlaylistColumns.YEAR + " DESC"
                    )
                }

                else -> {
                    db.query(
                        Lab14PlaylistColumns.TABLENAME,
                        PLAYLIST_PROJECTION,
                        null,
                        null,
                        null,
                        null,
                        Lab14PlaylistColumns.TITLE
                    )
                }
            }
            val ret = ArrayList<Lab14AudioAlbum>()
            cursor.use {
                while (it.moveToNext()) {
                    ret.add(
                        Lab14AudioAlbum().setDBId(it.getLong(BaseColumns._ID))
                            .setTitle(it.getString(Lab14PlaylistColumns.TITLE))
                            .setArtist(it.getString(Lab14PlaylistColumns.ARTIST))
                            .setColor(it.getInt(Lab14PlaylistColumns.COLOR))
                            .setYear(it.getInt(Lab14PlaylistColumns.YEAR))
                            .setThumbPath(it.getString(Lab14PlaylistColumns.THUMB_PATH))
                    )
                }
            }
            emitter.onSuccess(ret)
        }
    }

    override fun updateGenre(genre: Lab11Genre): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            try {
                if (genre.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(Lab11GenresColumns.NAME, genre.name)
                    val ins = db.insert(Lab11GenresColumns.TABLENAME, null, cv)
                    genre.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(genre.db_id.toString())

                    val cv = ContentValues()
                    cv.put(Lab11GenresColumns.NAME, genre.name)
                    db.update(Lab11GenresColumns.TABLENAME, cv, where, args)
                }
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun deleteGenre(id: Long): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            try {
                val where = BaseColumns._ID + " = ?"
                val args = arrayOf(id.toString())

                db.delete(Lab11GenresColumns.TABLENAME, where, args)
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun updateFilm(film: Lab11Film): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            try {
                if (film.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(Lab11FilmsColumns.TITLE, film.title)
                    cv.put(Lab11FilmsColumns.GENRE_ID, film.genre?.db_id ?: -1)
                    cv.put(Lab11FilmsColumns.YEAR, film.year)
                    cv.put(Lab11FilmsColumns.THUMB_PATH, film.thumbPath)
                    val ins = db.insert(Lab11FilmsColumns.TABLENAME, null, cv)
                    film.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(film.db_id.toString())

                    val cv = ContentValues()
                    cv.put(Lab11FilmsColumns.TITLE, film.title)
                    cv.put(Lab11FilmsColumns.GENRE_ID, film.genre?.db_id ?: -1)
                    cv.put(Lab11FilmsColumns.YEAR, film.year)
                    cv.put(Lab11FilmsColumns.THUMB_PATH, film.thumbPath)
                    db.update(Lab11FilmsColumns.TABLENAME, cv, where, args)
                }
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun deleteFilm(id: Long): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            try {
                val where = BaseColumns._ID + " = ?"
                val args = arrayOf(id.toString())

                db.delete(Lab11FilmsColumns.TABLENAME, where, args)
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun updatePlaylist(playlist: Lab14AudioAlbum): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            try {
                if (playlist.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(Lab14PlaylistColumns.TITLE, playlist.title)
                    cv.put(Lab14PlaylistColumns.ARTIST, playlist.artist)
                    cv.put(Lab14PlaylistColumns.COLOR, playlist.color)
                    cv.put(Lab14PlaylistColumns.YEAR, playlist.year)
                    cv.put(Lab14PlaylistColumns.THUMB_PATH, playlist.thumbPath)
                    val ins = db.insert(Lab14PlaylistColumns.TABLENAME, null, cv)
                    playlist.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(playlist.db_id.toString())

                    val cv = ContentValues()
                    cv.put(Lab14PlaylistColumns.TITLE, playlist.title)
                    cv.put(Lab14PlaylistColumns.ARTIST, playlist.artist)
                    cv.put(Lab14PlaylistColumns.COLOR, playlist.color)
                    cv.put(Lab14PlaylistColumns.YEAR, playlist.year)
                    cv.put(Lab14PlaylistColumns.THUMB_PATH, playlist.thumbPath)
                    db.update(Lab14PlaylistColumns.TABLENAME, cv, where, args)
                }
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun deletePlaylist(id: Long): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val db = helper.writableDatabase
            db.beginTransaction()
            if (emitter.isDisposed) {
                db.endTransaction()
                emitter.onComplete()
                return@create
            }
            try {
                val where = BaseColumns._ID + " = ?"
                val args = arrayOf(id.toString())

                db.delete(Lab14PlaylistColumns.TABLENAME, where, args)
                if (!emitter.isDisposed) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emitter.onComplete()
        }
    }

    override fun drop(): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            helper.purge(helper.writableDatabase)
            emitter.onComplete()
        }
    }

    private val QUERY_PROJECTION = arrayOf(
        BaseColumns._ID, SearchRequestColumns.SOURCE_ID, SearchRequestColumns.QUERY
    )

    private val FILES_PROJECTION = arrayOf(
        BaseColumns._ID, FilesColumns.PARENT_DIR,
        FilesColumns.TYPE,
        FilesColumns.IS_DIR,
        FilesColumns.FILE_NAME,
        FilesColumns.FILE_PATH,
        FilesColumns.PARENT_NAME,
        FilesColumns.PARENT_PATH,
        FilesColumns.MODIFICATIONS,
        FilesColumns.SIZE,
        FilesColumns.CAN_READ
    )

    private val GENRE_PROJECTION = arrayOf(
        BaseColumns._ID,
        Lab11GenresColumns.NAME
    )

    private val PLAYLIST_PROJECTION = arrayOf(
        BaseColumns._ID,
        Lab14PlaylistColumns.TITLE,
        Lab14PlaylistColumns.ARTIST,
        Lab14PlaylistColumns.COLOR,
        Lab14PlaylistColumns.YEAR,
        Lab14PlaylistColumns.THUMB_PATH
    )
}
