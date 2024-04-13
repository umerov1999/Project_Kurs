package dev.umerov.project.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import dev.umerov.project.Constants
import dev.umerov.project.db.column.CoinOperationsColumns
import dev.umerov.project.db.column.RegisterColumns
import dev.umerov.project.model.db.RegisterType

class ProjectDBHelper(context: Context) :
    SQLiteOpenHelper(context, "project_db.sqlite", null, Constants.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        createCoinOperationsTable(db)
        createRegisterTable(db)
    }

    private fun createCoinOperationsTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + CoinOperationsColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " [" + CoinOperationsColumns.DATE + "] INTEGER, " +
                " [" + CoinOperationsColumns.COIN + "] REAL, " +
                " [" + CoinOperationsColumns.TITTLE + "] TEXT, " +
                " [" + CoinOperationsColumns.COMMENT + "] TEXT, " +
                " [" + CoinOperationsColumns.TYPE + "] INTEGER, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
    }

    private fun createRegisterTable(db: SQLiteDatabase) {
        val sql = "CREATE TABLE IF NOT EXISTS [" + RegisterColumns.TABLENAME + "] (\n" +
                " [" + BaseColumns._ID + "] INTEGER NOT NULL UNIQUE, " +
                " [" + RegisterColumns.COIN_BALANCE + "] REAL, " +
                " [" + RegisterColumns.COIN_TAKED + "] REAL, " +
                " [" + RegisterColumns.COIN_PASTED + "] REAL, " +
                " [" + RegisterColumns.OPERATIONS_COUNT + "] INTEGER, " +
                " CONSTRAINT [] UNIQUE ([" + BaseColumns._ID + "]) ON CONFLICT REPLACE);"
        db.execSQL(sql)
        db.execSQL("INSERT INTO ${RegisterColumns.TABLENAME} (${BaseColumns._ID}, ${RegisterColumns.COIN_BALANCE}, ${RegisterColumns.COIN_TAKED}, ${RegisterColumns.COIN_PASTED}, ${RegisterColumns.OPERATIONS_COUNT}) VALUES (${RegisterType.BALANCE}, 0, 0, 0, 0)")
    }

    private fun purge(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + CoinOperationsColumns.TABLENAME)
        db.execSQL("DROP TABLE IF EXISTS " + RegisterColumns.TABLENAME)
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