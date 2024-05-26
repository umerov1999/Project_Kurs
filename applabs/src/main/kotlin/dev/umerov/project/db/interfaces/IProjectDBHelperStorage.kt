package dev.umerov.project.db.interfaces

import androidx.annotation.IdRes
import dev.umerov.project.model.FileItem
import dev.umerov.project.model.main.labs.Lab11Film
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.model.main.labs.Lab14AudioAlbum
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IProjectDBHelperStorage {
    fun getQueries(sourceId: Int): Single<List<String>>
    fun insertQuery(sourceId: Int, query: String?): Completable
    fun getFiles(parent: String): Single<List<FileItem>>
    fun insertFiles(parent: String, files: List<FileItem>): Completable
    fun clearQueriesAll()
    fun clearFilesAll()
    fun deleteQuery(sourceId: Int): Completable

    fun getFilms(): Single<ArrayList<Lab11Film>>
    fun getGenres(): Single<ArrayList<Lab11Genre>>
    fun getPlaylists(@IdRes checkedItem: Int): Single<ArrayList<Lab14AudioAlbum>>

    fun updateGenre(genre: Lab11Genre): Completable
    fun deleteGenre(id: Long): Completable

    fun updateFilm(film: Lab11Film): Completable
    fun deleteFilm(id: Long): Completable

    fun updatePlaylist(playlist: Lab14AudioAlbum): Completable
    fun deletePlaylist(id: Long): Completable

    fun drop(): Completable
}