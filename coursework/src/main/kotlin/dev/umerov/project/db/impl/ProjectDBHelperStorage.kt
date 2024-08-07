package dev.umerov.project.db.impl

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import dev.umerov.project.db.ProjectDBHelper
import dev.umerov.project.db.column.CoinOperationsColumns
import dev.umerov.project.db.column.RegisterColumns
import dev.umerov.project.db.interfaces.IProjectDBHelperStorage
import dev.umerov.project.getDouble
import dev.umerov.project.getInt
import dev.umerov.project.getLong
import dev.umerov.project.getString
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.model.db.Register
import dev.umerov.project.model.db.RegisterType
import dev.umerov.project.model.exceptions.DBException
import dev.umerov.project.model.exceptions.DBExceptionType
import dev.umerov.project.util.coroutines.CoroutinesUtils.isActive
import dev.umerov.project.util.coroutines.CoroutinesUtils.toFlowThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProjectDBHelperStorage internal constructor(context: Context) :
    IProjectDBHelperStorage {
    private val app: Context = context.applicationContext
    private val helper: ProjectDBHelper by lazy {
        ProjectDBHelper(app)
    }

    private fun getRegister(db: SQLiteDatabase, @RegisterType type: Int): Register {
        val where = BaseColumns._ID + " = ?"
        val args = arrayOf(type.toString())

        val cursor = db.query(
            RegisterColumns.TABLENAME,
            REGISTER_PROJECTION,
            where,
            args,
            null,
            null,
            BaseColumns._ID + " DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                return Register(
                    it.getDouble(RegisterColumns.COIN_BALANCE),
                    it.getDouble(RegisterColumns.COIN_TAKED),
                    it.getDouble(RegisterColumns.COIN_PASTED),
                    it.getLong(RegisterColumns.OPERATIONS_COUNT)
                )
            }
        }
        throw DBException(DBExceptionType.ERROR_GET_REGISTER)
    }

    private fun getCoinOperationsAllForBackup(
        db: SQLiteDatabase
    ): ArrayList<CoinOperation> {
        val cursor = db.query(
            CoinOperationsColumns.TABLENAME,
            OPERATION_PROJECTION,
            null,
            null,
            null,
            null,
            CoinOperationsColumns.DATE + " DESC"
        )
        val ret = ArrayList<CoinOperation>()
        cursor.use {
            while (it.moveToNext()) {
                val s = CoinOperation(
                    it.getLong(CoinOperationsColumns.DATE),
                    it.getString(CoinOperationsColumns.TITLE),
                    it.getString(CoinOperationsColumns.COMMENT),
                    it.getInt(CoinOperationsColumns.TYPE),
                    it.getDouble(CoinOperationsColumns.COIN),
                )
                ret.add(s)
            }
        }
        return ret
    }

    private fun getCoinOperations(
        db: SQLiteDatabase,
        @CoinOperationType type: Int
    ): ArrayList<CoinOperation> {
        val where = CoinOperationsColumns.TYPE + " = ?"
        val args = arrayOf(type.toString())

        val cursor = db.query(
            CoinOperationsColumns.TABLENAME,
            OPERATION_PROJECTION,
            where,
            args,
            null,
            null,
            CoinOperationsColumns.DATE + " DESC"
        )
        val ret = ArrayList<CoinOperation>()
        cursor.use {
            while (it.moveToNext()) {
                val s = CoinOperation(
                    it.getLong(CoinOperationsColumns.DATE),
                    it.getString(CoinOperationsColumns.TITLE),
                    it.getString(CoinOperationsColumns.COMMENT),
                    it.getInt(CoinOperationsColumns.TYPE),
                    it.getDouble(CoinOperationsColumns.COIN),
                )
                s.dbId = it.getLong(BaseColumns._ID)
                ret.add(s)
            }
        }
        return ret
    }

    private fun getCoinOperationById(db: SQLiteDatabase, id: Long): CoinOperation {
        val where = BaseColumns._ID + " = ?"
        val args = arrayOf(id.toString())

        val cursor = db.query(
            CoinOperationsColumns.TABLENAME,
            OPERATION_PROJECTION,
            where,
            args,
            null,
            null,
            BaseColumns._ID + " DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val s = CoinOperation(
                    it.getLong(CoinOperationsColumns.DATE),
                    it.getString(CoinOperationsColumns.TITLE),
                    it.getString(CoinOperationsColumns.COMMENT),
                    it.getInt(CoinOperationsColumns.TYPE),
                    it.getDouble(CoinOperationsColumns.COIN),
                )
                s.dbId = it.getLong(BaseColumns._ID)
                return s
            }
        }
        throw DBException(DBExceptionType.GET_COIN_OPERATION_BY_ID)
    }

    private fun insertRegister(db: SQLiteDatabase, @RegisterType type: Int, register: Register) {
        val cvReg = ContentValues()
        cvReg.put(BaseColumns._ID, type)
        cvReg.put(RegisterColumns.COIN_BALANCE, register.coinBalance)
        cvReg.put(RegisterColumns.COIN_TAKED, register.coinTaked)
        cvReg.put(RegisterColumns.COIN_PASTED, register.coinPasted)
        cvReg.put(RegisterColumns.OPERATIONS_COUNT, register.operationsCount)
        db.insert(RegisterColumns.TABLENAME, null, cvReg)
    }

    private fun insertCoinOperation(db: SQLiteDatabase, operation: CoinOperation) {
        val cv = ContentValues()
        cv.put(CoinOperationsColumns.COIN, operation.coin)
        cv.put(CoinOperationsColumns.COMMENT, operation.comment)
        cv.put(CoinOperationsColumns.DATE, operation.date)
        cv.put(CoinOperationsColumns.TYPE, operation.type)
        cv.put(CoinOperationsColumns.TITLE, operation.title)
        val ins = db.insert(CoinOperationsColumns.TABLENAME, null, cv)
        operation.dbId = ins
    }

    private fun updateCoinOperation(db: SQLiteDatabase, operation: CoinOperation) {
        val where = BaseColumns._ID + " = ?"
        val args = arrayOf(operation.dbId.toString())

        val cv = ContentValues()
        cv.put(CoinOperationsColumns.COIN, operation.coin)
        cv.put(CoinOperationsColumns.COMMENT, operation.comment)
        cv.put(CoinOperationsColumns.DATE, operation.date)
        cv.put(CoinOperationsColumns.TYPE, operation.type)
        cv.put(CoinOperationsColumns.TITLE, operation.title)
        db.update(CoinOperationsColumns.TABLENAME, cv, where, args)
    }

    private fun removeCoinOperation(db: SQLiteDatabase, operation: CoinOperation) {
        val where = BaseColumns._ID + " = ?"
        val args = arrayOf(operation.dbId.toString())

        db.delete(CoinOperationsColumns.TABLENAME, where, args)
    }

    override fun addOperation(operation: CoinOperation): Flow<Boolean> {
        if (operation.dbId != -1L) {
            return toFlowThrowable(DBException(DBExceptionType.ADD_OPERATION_SUPPORT_ONLY_NEW))
        }
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                val register = getRegister(db, RegisterType.BALANCE)
                if (operation.type == CoinOperationType.TAKE && register.coinBalance - operation.coin < 0) {
                    throw DBException(DBExceptionType.BALANCE_IS_LOW)
                }
                register.operationsCount++
                if (operation.type == CoinOperationType.TAKE) {
                    register.coinTaked += operation.coin
                    register.coinBalance -= operation.coin
                } else if (operation.type == CoinOperationType.PASTE) {
                    register.coinPasted += operation.coin
                    register.coinBalance += operation.coin
                }

                insertCoinOperation(db, operation)
                insertRegister(db, RegisterType.BALANCE, register)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun updateOperation(operation: CoinOperation): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                val register = getRegister(db, RegisterType.BALANCE)
                val oldOperation = getCoinOperationById(db, operation.dbId)

                if (oldOperation.type == CoinOperationType.TAKE) {
                    register.coinTaked -= oldOperation.coin
                    register.coinBalance += oldOperation.coin
                } else if (oldOperation.type == CoinOperationType.PASTE) {
                    register.coinPasted += oldOperation.coin
                    register.coinBalance -= oldOperation.coin
                }

                if (operation.type == CoinOperationType.TAKE) {
                    register.coinTaked += operation.coin
                    register.coinBalance -= operation.coin
                } else if (operation.type == CoinOperationType.PASTE) {
                    register.coinPasted += operation.coin
                    register.coinBalance += operation.coin
                }

                if (register.coinBalance < 0) {
                    throw DBException(DBExceptionType.BALANCE_IS_LOW)
                }

                updateCoinOperation(db, operation)
                insertRegister(db, RegisterType.BALANCE, register)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun removeOperation(dbId: Long): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                val register = getRegister(db, RegisterType.BALANCE)
                val oldOperation = getCoinOperationById(db, dbId)

                if (oldOperation.type == CoinOperationType.TAKE) {
                    register.coinTaked -= oldOperation.coin
                    register.coinBalance += oldOperation.coin
                } else if (oldOperation.type == CoinOperationType.PASTE) {
                    register.coinPasted -= oldOperation.coin
                    register.coinBalance -= oldOperation.coin
                }

                if (register.coinBalance < 0) {
                    throw DBException(DBExceptionType.BALANCE_IS_LOW)
                }

                removeCoinOperation(db, oldOperation)
                register.operationsCount--
                insertRegister(db, RegisterType.BALANCE, register)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun fetchRegister(@RegisterType type: Int): Flow<Register> {
        return flow {
            val db = helper.readableDatabase
            emit(getRegister(db, type))
        }
    }

    override fun fetchCoinOperations(@CoinOperationType type: Int): Flow<ArrayList<CoinOperation>> {
        return flow {
            val db = helper.readableDatabase
            emit(getCoinOperations(db, type))
        }
    }

    override fun fetchCoinOperationsAllForBackup(): Flow<ArrayList<CoinOperation>> {
        return flow {
            val db = helper.readableDatabase
            emit(getCoinOperationsAllForBackup(db))
        }
    }

    private val OPERATION_PROJECTION = arrayOf(
        BaseColumns._ID,
        CoinOperationsColumns.COIN,
        CoinOperationsColumns.COMMENT,
        CoinOperationsColumns.DATE,
        CoinOperationsColumns.TYPE,
        CoinOperationsColumns.TITLE
    )

    private val REGISTER_PROJECTION = arrayOf(
        BaseColumns._ID,
        RegisterColumns.COIN_BALANCE,
        RegisterColumns.COIN_PASTED,
        RegisterColumns.COIN_TAKED,
        RegisterColumns.OPERATIONS_COUNT
    )
}
