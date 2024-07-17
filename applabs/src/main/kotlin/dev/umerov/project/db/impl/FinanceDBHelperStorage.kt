package dev.umerov.project.db.impl

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import dev.umerov.project.db.ProjectDBHelper
import dev.umerov.project.db.column.FinanceOperationsColumns
import dev.umerov.project.db.column.FinanceWalletsColumns
import dev.umerov.project.db.interfaces.IFinanceDBHelperStorage
import dev.umerov.project.getBoolean
import dev.umerov.project.getDouble
import dev.umerov.project.getInt
import dev.umerov.project.getLong
import dev.umerov.project.getString
import dev.umerov.project.model.main.labs.FinanceBalance
import dev.umerov.project.model.main.labs.FinanceOperation
import dev.umerov.project.model.main.labs.FinanceWallet
import dev.umerov.project.util.coroutines.CoroutinesUtils.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FinanceDBHelperStorage internal constructor(context: Context) :
    IFinanceDBHelperStorage {
    private val app: Context = context.applicationContext
    private val helper: ProjectDBHelper by lazy {
        ProjectDBHelper(app)
    }

    override fun getWallets(isCreditCard: Boolean, id: Long?): Flow<List<FinanceWallet>> {
        return flow {
            val cursor = helper.writableDatabase.rawQuery(
                "SELECT ${FinanceWalletsColumns.FULL_ID}, ${FinanceWalletsColumns.FULL_TITLE}, ${FinanceWalletsColumns.FULL_IS_CREDIT_CARD}, ${FinanceWalletsColumns.FULL_CREATED_DATE}, ${FinanceWalletsColumns.FULL_COLOR}, ${FinanceWalletsColumns.COINS_SUM_IN}, ${FinanceWalletsColumns.COINS_SUM_OUT}\n" +
                        "FROM ${FinanceWalletsColumns.TABLENAME}\n" +
                        "LEFT JOIN (SELECT ${FinanceOperationsColumns.FULL_OWNER_ID}, SUM(${FinanceOperationsColumns.FULL_COINS}) as ${FinanceWalletsColumns.COINS_SUM_IN} from ${FinanceOperationsColumns.TABLENAME} WHERE ${FinanceOperationsColumns.FULL_IS_INCOME} = 1 GROUP BY ${FinanceOperationsColumns.FULL_OWNER_ID}) as a ON a.${FinanceOperationsColumns.OWNER_ID} = ${FinanceWalletsColumns.FULL_ID}\n" +
                        "LEFT JOIN (SELECT ${FinanceOperationsColumns.FULL_OWNER_ID}, SUM(${FinanceOperationsColumns.FULL_COINS}) as ${FinanceWalletsColumns.COINS_SUM_OUT} from ${FinanceOperationsColumns.TABLENAME} WHERE ${FinanceOperationsColumns.FULL_IS_INCOME} = 0 GROUP BY ${FinanceOperationsColumns.FULL_OWNER_ID}) as b ON b.${FinanceOperationsColumns.OWNER_ID} = ${FinanceWalletsColumns.FULL_ID}\n" +
                        (if (id == null) "WHERE ${FinanceWalletsColumns.FULL_IS_CREDIT_CARD} = $isCreditCard\n" else "WHERE ${FinanceWalletsColumns.FULL_ID} = $id\n") +
                        "ORDER BY ${FinanceWalletsColumns.FULL_CREATED_DATE} DESC;",
                emptyArray()
            )
            val data: MutableList<FinanceWallet> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    val item = FinanceWallet().setDBId(it.getLong(BaseColumns._ID))
                        .setTitle(it.getString(FinanceWalletsColumns.TITLE))
                        .setCreditCard(it.getBoolean(FinanceWalletsColumns.IS_CREDIT_CARD))
                        .setColor(it.getInt(FinanceWalletsColumns.COLOR))
                        .setCreateDate(it.getLong(FinanceWalletsColumns.CREATED_DATE))
                        .setCoins(
                            it.getDouble(FinanceWalletsColumns.COINS_SUM_IN) - it.getDouble(
                                FinanceWalletsColumns.COINS_SUM_OUT
                            )
                        )
                    data.add(item)
                }
            }
            emit(data)
        }
    }

    override fun updateWallet(wallet: FinanceWallet): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                if (wallet.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(FinanceWalletsColumns.TITLE, wallet.title)
                    cv.put(FinanceWalletsColumns.CREATED_DATE, wallet.createDate)
                    cv.put(FinanceWalletsColumns.IS_CREDIT_CARD, wallet.isCreditCard)
                    cv.put(FinanceWalletsColumns.COLOR, wallet.color)
                    val ins = db.insert(FinanceWalletsColumns.TABLENAME, null, cv)
                    wallet.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(wallet.db_id.toString())

                    val cv = ContentValues()
                    cv.put(FinanceWalletsColumns.TITLE, wallet.title)
                    cv.put(FinanceWalletsColumns.CREATED_DATE, wallet.createDate)
                    cv.put(FinanceWalletsColumns.IS_CREDIT_CARD, wallet.isCreditCard)
                    cv.put(FinanceWalletsColumns.COLOR, wallet.color)
                    db.update(FinanceWalletsColumns.TABLENAME, cv, where, args)
                }
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun deleteWallet(id: Long): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                val where = BaseColumns._ID + " = ?"
                val args = arrayOf(id.toString())

                db.delete(FinanceWalletsColumns.TABLENAME, where, args)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun getOperations(ownerId: Long): Flow<ArrayList<FinanceOperation>> {
        return flow {
            val db = helper.readableDatabase
            val where = FinanceOperationsColumns.OWNER_ID + " = ?"
            val args = arrayOf(ownerId.toString())

            val cursor = db.query(
                FinanceOperationsColumns.TABLENAME,
                OPERATION_PROJECTION,
                where,
                args,
                null,
                null,
                "${FinanceOperationsColumns.CREATED_DATE} DESC"
            )
            val ret = ArrayList<FinanceOperation>()
            cursor.use {
                while (it.moveToNext()) {
                    ret.add(
                        FinanceOperation().setDBId(it.getLong(BaseColumns._ID))
                            .setOwnerId(it.getLong(FinanceOperationsColumns.OWNER_ID))
                            .setTitle(it.getString(FinanceOperationsColumns.TITLE))
                            .setDescription(it.getString(FinanceOperationsColumns.DESCRIPTION))
                            .setCoins(it.getDouble(FinanceOperationsColumns.COINS))
                            .setIsIncome(it.getBoolean(FinanceOperationsColumns.IS_INCOME))
                            .setCreateDate(it.getLong(FinanceOperationsColumns.CREATED_DATE))
                            .setColor(it.getInt(FinanceOperationsColumns.COLOR))
                    )
                }
            }
            emit(ret)
        }
    }

    override fun updateOperation(operation: FinanceOperation): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                if (operation.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(FinanceOperationsColumns.OWNER_ID, operation.db_owner_id)
                    cv.put(FinanceOperationsColumns.TITLE, operation.title)
                    cv.put(FinanceOperationsColumns.DESCRIPTION, operation.description)
                    cv.put(FinanceOperationsColumns.CREATED_DATE, operation.createDate)
                    cv.put(FinanceOperationsColumns.COINS, operation.coins)
                    cv.put(FinanceOperationsColumns.IS_INCOME, operation.isIncome)
                    cv.put(FinanceOperationsColumns.COLOR, operation.color)
                    val ins = db.insert(FinanceOperationsColumns.TABLENAME, null, cv)
                    operation.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(operation.db_id.toString())

                    val cv = ContentValues()
                    cv.put(FinanceOperationsColumns.OWNER_ID, operation.db_owner_id)
                    cv.put(FinanceOperationsColumns.TITLE, operation.title)
                    cv.put(FinanceOperationsColumns.DESCRIPTION, operation.description)
                    cv.put(FinanceOperationsColumns.CREATED_DATE, operation.createDate)
                    cv.put(FinanceOperationsColumns.COINS, operation.coins)
                    cv.put(FinanceOperationsColumns.IS_INCOME, operation.isIncome)
                    cv.put(FinanceOperationsColumns.COLOR, operation.color)
                    db.update(FinanceOperationsColumns.TABLENAME, cv, where, args)
                }
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun deleteOperation(id: Long): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                val where = BaseColumns._ID + " = ?"
                val args = arrayOf(id.toString())

                db.delete(FinanceOperationsColumns.TABLENAME, where, args)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun fetchBalance(): Flow<FinanceBalance> {
        return flow {
            val cursor = helper.writableDatabase.rawQuery(
                "SELECT ${FinanceWalletsColumns.FULL_IS_CREDIT_CARD}, ${FinanceWalletsColumns.COINS_SUM_IN}, ${FinanceWalletsColumns.COINS_SUM_OUT}\n" +
                        "FROM ${FinanceWalletsColumns.TABLENAME}\n" +
                        "LEFT JOIN (SELECT ${FinanceOperationsColumns.FULL_OWNER_ID}, SUM(${FinanceOperationsColumns.FULL_COINS}) as ${FinanceWalletsColumns.COINS_SUM_IN} from ${FinanceOperationsColumns.TABLENAME} WHERE ${FinanceOperationsColumns.FULL_IS_INCOME} = 1 GROUP BY ${FinanceOperationsColumns.FULL_OWNER_ID}) as a ON a.${FinanceOperationsColumns.OWNER_ID} = ${FinanceWalletsColumns.FULL_ID}\n" +
                        "LEFT JOIN (SELECT ${FinanceOperationsColumns.FULL_OWNER_ID}, SUM(${FinanceOperationsColumns.FULL_COINS}) as ${FinanceWalletsColumns.COINS_SUM_OUT} from ${FinanceOperationsColumns.TABLENAME} WHERE ${FinanceOperationsColumns.FULL_IS_INCOME} = 0 GROUP BY ${FinanceOperationsColumns.FULL_OWNER_ID}) as b ON b.${FinanceOperationsColumns.OWNER_ID} = ${FinanceWalletsColumns.FULL_ID}\n" +
                        "ORDER BY ${FinanceWalletsColumns.FULL_CREATED_DATE} DESC;",
                emptyArray()
            )
            val data = FinanceBalance()
            cursor.use {
                while (it.moveToNext()) {
                    data.setFullBalance(
                        data.fullBalance + it.getDouble(FinanceWalletsColumns.COINS_SUM_IN) - it.getDouble(
                            FinanceWalletsColumns.COINS_SUM_OUT
                        )
                    )
                    if (it.getBoolean(FinanceWalletsColumns.IS_CREDIT_CARD)) {
                        data.setCreditBalance(
                            data.creditBalance + it.getDouble(FinanceWalletsColumns.COINS_SUM_IN) - it.getDouble(
                                FinanceWalletsColumns.COINS_SUM_OUT
                            )
                        )
                    } else {
                        data.setWalletBalance(
                            data.walletBalance + it.getDouble(FinanceWalletsColumns.COINS_SUM_IN) - it.getDouble(
                                FinanceWalletsColumns.COINS_SUM_OUT
                            )
                        )
                    }
                }
            }
            emit(data)
        }
    }

    private val OPERATION_PROJECTION = arrayOf(
        BaseColumns._ID,
        FinanceOperationsColumns.OWNER_ID,
        FinanceOperationsColumns.TITLE,
        FinanceOperationsColumns.DESCRIPTION,
        FinanceOperationsColumns.COINS,
        FinanceOperationsColumns.IS_INCOME,
        FinanceOperationsColumns.CREATED_DATE,
        FinanceOperationsColumns.COLOR
    )
}