package dev.umerov.project.db.interfaces

import androidx.annotation.IdRes
import dev.umerov.project.model.FileItem
import dev.umerov.project.model.main.labs.Lab11Film
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.model.main.labs.Lab14AudioAlbum
import kotlinx.coroutines.flow.Flow

interface IProjectDBHelperStorage {
    fun getQueries(sourceId: Int): Flow<List<String>>
    fun insertQuery(sourceId: Int, query: String?): Flow<Boolean>
    fun getFiles(parent: String): Flow<List<FileItem>>
    fun insertFiles(parent: String, files: List<FileItem>): Flow<Boolean>
    fun clearQueriesAll()
    fun clearFilesAll()
    fun deleteQuery(sourceId: Int): Flow<Boolean>

    fun getFilms(): Flow<ArrayList<Lab11Film>>
    fun getGenres(): Flow<ArrayList<Lab11Genre>>
    fun getPlaylists(@IdRes checkedItem: Int): Flow<ArrayList<Lab14AudioAlbum>>

    fun updateGenre(genre: Lab11Genre): Flow<Boolean>
    fun deleteGenre(id: Long): Flow<Boolean>

    fun updateFilm(film: Lab11Film): Flow<Boolean>
    fun deleteFilm(id: Long): Flow<Boolean>

    fun updatePlaylist(playlist: Lab14AudioAlbum): Flow<Boolean>
    fun deletePlaylist(id: Long): Flow<Boolean>

    fun drop(): Flow<Boolean>
}