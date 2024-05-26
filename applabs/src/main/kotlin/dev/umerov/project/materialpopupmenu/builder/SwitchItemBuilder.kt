package dev.umerov.project.materialpopupmenu.builder

import androidx.annotation.StringRes
import dev.umerov.project.materialpopupmenu.MaterialPopupMenu
import dev.umerov.project.materialpopupmenu.MaterialPopupMenuBuilder

class SwitchItemBuilder private constructor(override val data: MaterialPopupMenuBuilder.SwitchItem.Data) :
    ToggleItemBuilder<SwitchItemBuilder>() {
    constructor(label: CharSequence) : this(MaterialPopupMenuBuilder.SwitchItem.Data(label))
    constructor(@StringRes labelRes: Int) : this(MaterialPopupMenuBuilder.SwitchItem.Data(labelRes))

    override fun self() = this
    override fun build(): MaterialPopupMenu.PopupMenuSwitchItem =
        MaterialPopupMenu.PopupMenuSwitchItem(data, resolveOnShowCallback())
}