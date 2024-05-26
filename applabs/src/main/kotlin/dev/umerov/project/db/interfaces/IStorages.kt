package dev.umerov.project.db.interfaces

interface IStorages {
    fun projectStore(): IProjectDBHelperStorage
    fun shoppingStore(): IShoppingListDBHelperStorage
}