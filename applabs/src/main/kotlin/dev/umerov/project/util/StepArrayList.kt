package dev.umerov.project.util

import de.maxr1998.modernpreferences.PreferenceScreen
import dev.umerov.project.Includes

class StepArrayList<T>(list: List<T>, private val key: String? = null) : ArrayList<T>(list) {
    private var currentItem = 0
    fun getNext(): T? {
        if (size <= 0) {
            return null
        }
        currentItem++
        if (currentItem >= size - 1) {
            currentItem = 0
        }
        key?.let {
            PreferenceScreen.getPreferences(Includes.provideApplicationContext()).edit()
                .putInt(key, currentItem).apply()
        }
        return get(currentItem)
    }

    init {
        key?.let {
            currentItem =
                PreferenceScreen.getPreferences(Includes.provideApplicationContext()).getInt(key, 0)
        }
    }

    companion object {
        const val FINANCE_OPERATION = "finance_operation"
        const val FINANCE_WALLET = "finance_wallet"
        const val LAB14_AUDIO_ALBUM = "lab14_audio_album"
        const val PRODUCT = "product"
        const val SHOPPING_LIST = "shopping_list"
    }
}