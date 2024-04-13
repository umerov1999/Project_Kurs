package dev.umerov.project.place

object PlaceFactory {
    fun getBalancePlace(): Place {
        return Place(Place.BALANCE)
    }

    fun getPastePlace(): Place {
        return Place(Place.PASTE)
    }

    fun getTakePlace(): Place {
        return Place(Place.TAKE)
    }

    fun getSettingsThemePlace(): Place {
        return Place(Place.SETTINGS_THEME)
    }

    fun getPreferencesPlace(): Place {
        return Place(Place.PREFERENCES)
    }

    val securitySettingsPlace: Place
        get() = Place(Place.SECURITY)
}
