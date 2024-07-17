package dev.umerov.project.db.interfaces

import dev.umerov.project.model.main.labs.Product
import dev.umerov.project.model.main.labs.ShoppingList
import kotlinx.coroutines.flow.Flow

interface IShoppingListDBHelperStorage {
    fun getShoppingListHelper(): Flow<List<String>>
    fun getShoppingList(id: Long? = null): Flow<List<ShoppingList>>
    fun getProducts(ownerId: Long): Flow<ArrayList<Product>>
    fun updateShoppingList(shoppingList: ShoppingList): Flow<Boolean>
    fun updateProduct(product: Product): Flow<Boolean>
    fun deleteProduct(id: Long): Flow<Boolean>
    fun deleteShoppingList(id: Long): Flow<Boolean>
}
