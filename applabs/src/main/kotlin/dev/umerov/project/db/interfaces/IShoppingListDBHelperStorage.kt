package dev.umerov.project.db.interfaces

import dev.umerov.project.model.main.labs.Product
import dev.umerov.project.model.main.labs.ShoppingList
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IShoppingListDBHelperStorage {
    fun getShoppingListHelper(): Single<List<String>>
    fun getShoppingList(id: Long? = null): Single<List<ShoppingList>>
    fun getProducts(ownerId: Long): Single<ArrayList<Product>>
    fun updateShoppingList(shoppingList: ShoppingList): Completable
    fun updateProduct(product: Product): Completable
    fun deleteProduct(id: Long): Completable
    fun deleteShoppingList(id: Long): Completable
}
