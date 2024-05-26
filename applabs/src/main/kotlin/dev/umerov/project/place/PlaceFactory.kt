package dev.umerov.project.place

import dev.umerov.project.fragment.filemanager.FileManagerFragment
import dev.umerov.project.fragment.main.shoppingproducts.ShoppingProductsFragment
import dev.umerov.project.model.main.labs.ShoppingList

object PlaceFactory {
    fun getMainPlace(): Place {
        return Place(Place.MAIN)
    }

    fun getSettingsThemePlace(): Place {
        return Place(Place.SETTINGS_THEME)
    }

    fun getPreferencesPlace(): Place {
        return Place(Place.PREFERENCES)
    }

    val securitySettingsPlace: Place
        get() = Place(Place.SECURITY)

    fun getFileManagerPlace(
        path: String,
        base: Boolean,
        isSelect: Boolean,
    ): Place {
        return Place(Place.FILE_MANAGER)
            .setArguments(FileManagerFragment.buildArgs(path, base, isSelect))
    }

    fun getStaffPlace(): Place {
        return Place(Place.STAFF)
    }

    fun getLAB_1Place(): Place {
        return Place(Place.LAB_1)
    }

    fun getLAB_2Place(): Place {
        return Place(Place.LAB_2)
    }

    fun getLAB_3Place(): Place {
        return Place(Place.LAB_3)
    }

    fun getLAB_4Place(): Place {
        return Place(Place.LAB_4)
    }

    fun getLAB_4_1Place(): Place {
        return Place(Place.LAB_4_1)
    }

    fun getLAB_5Place(): Place {
        return Place(Place.LAB_5)
    }

    fun getLAB_6Place(): Place {
        return Place(Place.LAB_6)
    }

    fun getLAB_7Place(): Place {
        return Place(Place.LAB_7)
    }

    fun getLAB_8Place(): Place {
        return Place(Place.LAB_8)
    }

    fun getLAB_9Place(): Place {
        return Place(Place.LAB_9)
    }

    fun getLAB_10Place(): Place {
        return Place(Place.LAB_10)
    }

    fun getLAB_11Place(): Place {
        return Place(Place.LAB_11)
    }

    fun getLAB_12Place(): Place {
        return Place(Place.LAB_12)
    }

    fun getLAB_13Place(): Place {
        return Place(Place.LAB_13)
    }

    fun getLAB_14Place(): Place {
        return Place(Place.LAB_14)
    }

    fun getLAB_15Place(): Place {
        return Place(Place.LAB_15)
    }

    fun getLAB_16Place(): Place {
        return Place(Place.LAB_16)
    }

    fun getLAB_17Place(): Place {
        return Place(Place.LAB_17)
    }

    fun getLAB_18Place(): Place {
        return Place(Place.LAB_18)
    }

    fun getLAB_19Place(): Place {
        return Place(Place.LAB_19)
    }

    fun getSnakePlace(): Place {
        return Place(Place.SNAKE)
    }

    fun getShoppingListPlace(): Place {
        return Place(Place.SHOPPING_LIST)
    }

    fun getShoppingProductPlace(
        shoppingList: ShoppingList
    ): Place {
        return Place(Place.SHOPPING_PRODUCTS)
            .setArguments(ShoppingProductsFragment.buildArgs(shoppingList))
    }
}
