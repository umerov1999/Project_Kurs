package dev.umerov.project.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import dev.umerov.project.Constants
import dev.umerov.project.db.column.FilesColumns
import dev.umerov.project.db.column.FinanceOperationsColumns
import dev.umerov.project.db.column.FinanceWalletsColumns
import dev.umerov.project.db.column.Lab11FilmsColumns
import dev.umerov.project.db.column.Lab11GenresColumns
import dev.umerov.project.db.column.Lab14PlaylistColumns
import dev.umerov.project.db.column.SearchRequestColumns
import dev.umerov.project.db.column.ShoppingListColumns
import dev.umerov.project.db.column.ShoppingProductColumns

class ProjectDBHelper(context: Context) :
    SQLiteOpenHelper(context, "project_db.sqlite", null, Constants.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        createSearchRequestTable(db)
        createFilesTable(db)
        createFilmsTable(db)
        createGenresTable(db)
        createPlaylistTable(db)

        createShoppingListTable(db)
        createShoppingProductTable(db)

        createWalletListTable(db)
        createFinanceOperationsListTable(db)
    }

    private fun createWalletListTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + FinanceWalletsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FinanceWalletsColumns.TITLE + "] TEXT, " +
                " [" + FinanceWalletsColumns.CREATED_DATE + "] INTEGER, " +
                " [" + FinanceWalletsColumns.COLOR + "] INTEGER, " +
                " [" + FinanceWalletsColumns.IS_CREDIT_CARD + "] BOOLEAN, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createFinanceOperationsListTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + FinanceOperationsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FinanceOperationsColumns.OWNER_ID + "] INTEGER, " +
                " [" + FinanceOperationsColumns.TITLE + "] TEXT, " +
                " [" + FinanceOperationsColumns.DESCRIPTION + "] TEXT, " +
                " [" + FinanceOperationsColumns.CREATED_DATE + "] INTEGER, " +
                " [" + FinanceOperationsColumns.COLOR + "] INTEGER, " +
                " [" + FinanceOperationsColumns.COINS + "] REAL, " +
                " [" + FinanceOperationsColumns.IS_INCOME + "] BOOLEAN, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "], [" + FinanceOperationsColumns.OWNER_ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createShoppingListTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + ShoppingListColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + ShoppingListColumns.TITLE + "] TEXT, " +
                " [" + ShoppingListColumns.DESCRIPTION + "] TEXT, " +
                " [" + ShoppingListColumns.CREATION_DATE + "] INTEGER, " +
                " [" + ShoppingListColumns.COLOR + "] INTEGER, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createShoppingProductTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + ShoppingProductColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + ShoppingProductColumns.OWNER_ID + "] INTEGER, " +
                " [" + ShoppingProductColumns.NAME + "] TEXT, " +
                " [" + ShoppingProductColumns.COUNT + "] INTEGER, " +
                " [" + ShoppingProductColumns.IS_BOUGHT + "] BOOLEAN, " +
                " [" + ShoppingProductColumns.UNIT + "] INTEGER, " +
                " [" + ShoppingProductColumns.COLOR + "] INTEGER, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "], [" + ShoppingProductColumns.OWNER_ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createSearchRequestTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + SearchRequestColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + SearchRequestColumns.SOURCE_ID + "] INTEGER, " +
                " [" + SearchRequestColumns.QUERY + "] TEXT, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createFilesTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + FilesColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + FilesColumns.PARENT_DIR + "] TEXT, " +
                " [" + FilesColumns.TYPE + "] INTEGER, " +
                " [" + FilesColumns.IS_DIR + "] INTEGER, " +
                " [" + FilesColumns.FILE_NAME + "] TEXT, " +
                " [" + FilesColumns.FILE_PATH + "] TEXT, " +
                " [" + FilesColumns.PARENT_NAME + "] TEXT, " +
                " [" + FilesColumns.PARENT_PATH + "] TEXT, " +
                " [" + FilesColumns.MODIFICATIONS + "] BIGINT, " +
                " [" + FilesColumns.SIZE + "] BIGINT, " +
                " [" + FilesColumns.CAN_READ + "] INTEGER, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createFilmsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + Lab11FilmsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + Lab11FilmsColumns.TITLE + "] TEXT, " +
                " [" + Lab11FilmsColumns.YEAR + "] INTEGER, " +
                " [" + Lab11FilmsColumns.GENRE_ID + "] INTEGER, " +
                " [" + Lab11FilmsColumns.THUMB_PATH + "] TEXT, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createGenresTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + Lab11GenresColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + Lab11GenresColumns.NAME + "] TEXT, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createPlaylistTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + Lab14PlaylistColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + Lab14PlaylistColumns.TITLE + "] TEXT, " +
                " [" + Lab14PlaylistColumns.YEAR + "] INTEGER, " +
                " [" + Lab14PlaylistColumns.COLOR + "] INTEGER, " +
                " [" + Lab14PlaylistColumns.ARTIST + "] TEXT, " +
                " [" + Lab14PlaylistColumns.THUMB_PATH + "] TEXT, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    fun purge(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + SearchRequestColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + FilesColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + Lab11FilmsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + Lab11GenresColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + Lab14PlaylistColumns.TABLENAME)

        db.execSQL("DROP TABLE IF EXISTS " + ShoppingListColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + ShoppingProductColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + FinanceWalletsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + FinanceOperationsColumns.TABLENAME)
        onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_VERSION) {
            purge(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != Constants.DATABASE_VERSION) {
            purge(db)
        }
    }
}