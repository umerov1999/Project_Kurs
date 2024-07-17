package dev.umerov.project.db.impl

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import dev.umerov.project.db.ProjectDBHelper
import dev.umerov.project.db.column.ShoppingListColumns
import dev.umerov.project.db.column.ShoppingProductColumns
import dev.umerov.project.db.interfaces.IShoppingListDBHelperStorage
import dev.umerov.project.getBoolean
import dev.umerov.project.getInt
import dev.umerov.project.getLong
import dev.umerov.project.getString
import dev.umerov.project.isNull
import dev.umerov.project.model.main.labs.Product
import dev.umerov.project.model.main.labs.ShoppingList
import dev.umerov.project.util.coroutines.CoroutinesUtils.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShoppingListDBHelperStorage internal constructor(context: Context) :
    IShoppingListDBHelperStorage {
    private val app: Context = context.applicationContext
    private val helper: ProjectDBHelper by lazy {
        ProjectDBHelper(app)
    }

    override fun getShoppingListHelper(): Flow<List<String>> {
        return flow {
            val cursor = helper.writableDatabase.rawQuery(
                "SELECT ${ShoppingListColumns.FULL_TITLE} FROM ${ShoppingListColumns.TABLENAME} UNION ALL SELECT ${ShoppingProductColumns.FULL_NAME} FROM ${ShoppingProductColumns.TABLENAME};",
                emptyArray()
            )
            val data: MutableList<String> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    data.add(it.getString(0))
                }
            }
            emit(data)
        }
    }

    override fun getShoppingList(id: Long?): Flow<List<ShoppingList>> {
        return flow {
            val cursor = helper.writableDatabase.rawQuery(
                "SELECT ${ShoppingListColumns.FULL_ID}, ${ShoppingListColumns.FULL_TITLE}, ${ShoppingListColumns.FULL_DESCRIPTION}, ${ShoppingListColumns.FULL_CREATION_DATE}, ${ShoppingListColumns.FULL_COLOR}, ${ShoppingListColumns.DB_PURCHASE}, ${ShoppingListColumns.DB_PLANNED_PURCHASE}\n" +
                        "FROM ${ShoppingListColumns.TABLENAME}\n" +
                        "LEFT JOIN (SELECT ${ShoppingProductColumns.FULL_OWNER_ID}, SUM(${ShoppingProductColumns.FULL_IS_BOUGHT} * ${ShoppingProductColumns.FULL_COUNT}) as ${ShoppingListColumns.DB_PURCHASE} from ${ShoppingProductColumns.TABLENAME} GROUP BY ${ShoppingProductColumns.FULL_OWNER_ID}) as a ON a.${ShoppingProductColumns.OWNER_ID} = ${ShoppingListColumns.FULL_ID}\n" +
                        "LEFT JOIN (SELECT ${ShoppingProductColumns.FULL_OWNER_ID}, SUM(${ShoppingProductColumns.FULL_COUNT}) as ${ShoppingListColumns.DB_PLANNED_PURCHASE} from ${ShoppingProductColumns.TABLENAME} GROUP BY ${ShoppingProductColumns.FULL_OWNER_ID}) as b on b.${ShoppingProductColumns.OWNER_ID} = ${ShoppingListColumns.FULL_ID}\n" +
                        (if (id == null) "" else "WHERE ${ShoppingListColumns.FULL_ID} = $id\n") +
                        "ORDER BY ${ShoppingListColumns.FULL_CREATION_DATE} DESC;",
                emptyArray()
            )
            val data: MutableList<ShoppingList> = ArrayList(cursor.count)
            cursor.use {
                while (it.moveToNext()) {
                    val item = ShoppingList().setDBId(it.getLong(BaseColumns._ID))
                        .setTitle(it.getString(ShoppingListColumns.TITLE))
                        .setDescription(it.getString(ShoppingListColumns.DESCRIPTION))
                        .setColor(it.getInt(ShoppingListColumns.COLOR))
                        .setCreationDate(it.getLong(ShoppingListColumns.CREATION_DATE))
                    if (!it.isNull(ShoppingListColumns.DB_PLANNED_PURCHASE)) {
                        item.setDBPlannedPurchase(it.getInt(ShoppingListColumns.DB_PLANNED_PURCHASE))
                    }
                    if (!it.isNull(ShoppingListColumns.DB_PURCHASE)) {
                        item.setDBPurchase(it.getInt(ShoppingListColumns.DB_PURCHASE))
                    }
                    data.add(item)
                }
            }
            emit(data)
        }
    }

    override fun getProducts(ownerId: Long): Flow<ArrayList<Product>> {
        return flow {
            val db = helper.readableDatabase
            val where = ShoppingProductColumns.OWNER_ID + " = ?"
            val args = arrayOf(ownerId.toString())

            val cursor = db.query(
                ShoppingProductColumns.TABLENAME,
                PRODUCT_PROJECTION,
                where,
                args,
                null,
                null,
                ShoppingProductColumns.IS_BOUGHT
            )
            val ret = ArrayList<Product>()
            cursor.use {
                while (it.moveToNext()) {
                    ret.add(
                        Product().setDBId(it.getLong(BaseColumns._ID))
                            .setDBOwnerId(it.getLong(ShoppingProductColumns.OWNER_ID))
                            .setName(it.getString(ShoppingProductColumns.NAME))
                            .setCount(it.getInt(ShoppingProductColumns.COUNT))
                            .setIsBought(it.getBoolean(ShoppingProductColumns.IS_BOUGHT))
                            .setUnit(it.getInt(ShoppingProductColumns.UNIT))
                            .setColor(it.getInt(ShoppingProductColumns.COLOR))
                    )
                }
            }
            emit(ret)
        }
    }

    override fun updateShoppingList(shoppingList: ShoppingList): Flow<Boolean> {
        return flow {
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                if (shoppingList.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(ShoppingListColumns.TITLE, shoppingList.title)
                    cv.put(ShoppingListColumns.DESCRIPTION, shoppingList.description)
                    cv.put(ShoppingListColumns.CREATION_DATE, shoppingList.creationDate)
                    cv.put(ShoppingListColumns.COLOR, shoppingList.color)
                    val ins = db.insert(ShoppingListColumns.TABLENAME, null, cv)
                    shoppingList.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(shoppingList.db_id.toString())

                    val cv = ContentValues()
                    cv.put(ShoppingListColumns.TITLE, shoppingList.title)
                    cv.put(ShoppingListColumns.DESCRIPTION, shoppingList.description)
                    cv.put(ShoppingListColumns.CREATION_DATE, shoppingList.creationDate)
                    cv.put(ShoppingListColumns.COLOR, shoppingList.color)
                    db.update(ShoppingListColumns.TABLENAME, cv, where, args)
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

    override fun updateProduct(product: Product): Flow<Boolean> {
        return flow {
            if (product.db_owner_id < 0) {
                throw UnsupportedOperationException()
            }
            val db = helper.writableDatabase
            db.beginTransaction()
            if (!isActive()) {
                db.endTransaction()
                emit(false)
                return@flow
            }
            try {
                if (product.db_id < 0) {
                    val cv = ContentValues()
                    cv.put(ShoppingProductColumns.OWNER_ID, product.db_owner_id)
                    cv.put(ShoppingProductColumns.NAME, product.name)
                    cv.put(ShoppingProductColumns.COUNT, product.count)
                    cv.put(ShoppingProductColumns.IS_BOUGHT, product.isBought)
                    cv.put(ShoppingProductColumns.UNIT, product.unit)
                    cv.put(ShoppingProductColumns.COLOR, product.color)
                    val ins = db.insert(ShoppingProductColumns.TABLENAME, null, cv)
                    product.setDBId(ins)
                } else {
                    val where = BaseColumns._ID + " = ?"
                    val args = arrayOf(product.db_id.toString())

                    val cv = ContentValues()
                    cv.put(ShoppingProductColumns.OWNER_ID, product.db_owner_id)
                    cv.put(ShoppingProductColumns.NAME, product.name)
                    cv.put(ShoppingProductColumns.COUNT, product.count)
                    cv.put(ShoppingProductColumns.IS_BOUGHT, product.isBought)
                    cv.put(ShoppingProductColumns.UNIT, product.unit)
                    cv.put(ShoppingProductColumns.COLOR, product.color)
                    db.update(ShoppingProductColumns.TABLENAME, cv, where, args)
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

    override fun deleteProduct(id: Long): Flow<Boolean> {
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

                db.delete(ShoppingProductColumns.TABLENAME, where, args)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    override fun deleteShoppingList(id: Long): Flow<Boolean> {
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
                val whereProduct = ShoppingProductColumns.OWNER_ID + " = ?"
                val args = arrayOf(id.toString())

                db.delete(ShoppingProductColumns.TABLENAME, whereProduct, args)
                db.delete(ShoppingListColumns.TABLENAME, where, args)
                if (isActive()) {
                    db.setTransactionSuccessful()
                }
            } finally {
                db.endTransaction()
            }
            emit(true)
        }
    }

    private val PRODUCT_PROJECTION = arrayOf(
        BaseColumns._ID,
        ShoppingProductColumns.OWNER_ID,
        ShoppingProductColumns.NAME,
        ShoppingProductColumns.COUNT,
        ShoppingProductColumns.IS_BOUGHT,
        ShoppingProductColumns.UNIT,
        ShoppingProductColumns.COLOR
    )
}


//CREATE TABLE IF NOT EXISTS [shopping_list_column] ( [id] INTEGER, [title] TEXT, [description] TEXT, CONSTRAINT [] UNIQUE ([id]) ON CONFLICT REPLACE);
//CREATE TABLE IF NOT EXISTS [shopping_product_column] ( [id] INTEGER, [owner_id] INTEGER, [name] TEXT, [is_bought] BOOLEAN, [count_item] INTEGER, CONSTRAINT [] UNIQUE ([id],[owner_id]) ON CONFLICT REPLACE);

//INSERT INTO shopping_list_column (id,title,description) VALUES(1, 'Test1', 'Test2');
//INSERT INTO shopping_list_column (id,title,description) VALUES(2, 'Test2', 'Test3');
//INSERT INTO shopping_product_column (id,owner_id,name,is_bought,count_item) VALUES(1,1,'TestProd1',1,2);
//INSERT INTO shopping_product_column (id,owner_id,name,is_bought,count_item) VALUES(2,1,'TestProd2',1,4);
//INSERT INTO shopping_product_column (id,owner_id,name,is_bought,count_item) VALUES(3,1,'TestProd3',0,6);
//INSERT INTO shopping_product_column (id,owner_id,name,is_bought,count_item) VALUES(4,2,'TestProd3',0,6);

//SELECT id, title, description, db_Purchase, db_plannedPurchase
//FROM shopping_list_column
//LEFT JOIN (select owner_id, SUM(is_bought * count_item) as db_plannedPurchase from shopping_product_column GROUP BY owner_id) as a ON a.owner_id = shopping_list_column.id
//LEFT JOIN (select owner_id, SUM(count_item) as db_Purchase from shopping_product_column GROUP BY owner_id) as b on b.owner_id = shopping_list_column.id