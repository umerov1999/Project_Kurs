package dev.umerov.project.db.impl

import android.content.Context
import android.content.ContextWrapper
import dev.umerov.project.db.interfaces.IProjectDBHelperStorage
import dev.umerov.project.db.interfaces.IShoppingListDBHelperStorage
import dev.umerov.project.db.interfaces.IStorages

class AppStorages(base: Context) : ContextWrapper(base), IStorages {
    private val projectStore = ProjectDBHelperStorage(this)
    private val shoppingStore = ShoppingListDBHelperStorage(this)
    override fun projectStore(): IProjectDBHelperStorage {
        return projectStore
    }

    override fun shoppingStore(): IShoppingListDBHelperStorage {
        return shoppingStore
    }
}