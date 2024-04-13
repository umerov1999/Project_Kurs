package dev.umerov.project.materialpopupmenu.builder

import androidx.annotation.StringRes
import dev.umerov.project.materialpopupmenu.MaterialPopupMenu
import dev.umerov.project.materialpopupmenu.MaterialPopupMenuBuilder

class RadioButtonItemBuilder private constructor(override val data: MaterialPopupMenuBuilder.RadioButtonItem.Data) :
    ToggleItemBuilder<RadioButtonItemBuilder>() {
    constructor(label: CharSequence) : this(MaterialPopupMenuBuilder.RadioButtonItem.Data(label))
    constructor(@StringRes labelRes: Int) : this(
        MaterialPopupMenuBuilder.RadioButtonItem.Data(
            labelRes
        )
    )

    override fun self() = this
    override fun build(): MaterialPopupMenu.PopupMenuRadioButtonItem =
        MaterialPopupMenu.PopupMenuRadioButtonItem(data, resolveOnShowCallback())
}