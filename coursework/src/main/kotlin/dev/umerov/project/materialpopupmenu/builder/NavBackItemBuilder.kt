package dev.umerov.project.materialpopupmenu.builder

import androidx.annotation.StringRes
import dev.umerov.project.materialpopupmenu.MaterialPopupMenu
import dev.umerov.project.materialpopupmenu.MaterialPopupMenuBuilder

class NavBackItemBuilder private constructor(override val data: MaterialPopupMenuBuilder.NavBackItem.Data) :
    NormalItemBuilder<NavBackItemBuilder>() {
    constructor() : this(MaterialPopupMenuBuilder.NavBackItem.Data())
    constructor(label: CharSequence) : this(MaterialPopupMenuBuilder.NavBackItem.Data(label))
    constructor(@StringRes labelRes: Int) : this(MaterialPopupMenuBuilder.NavBackItem.Data(labelRes))

    @Deprecated("This is a no-op", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith(""))
    override fun setDismissOnSelect(dismiss: Boolean): NavBackItemBuilder {
        return super.setDismissOnSelect(dismiss)
    }

    override fun self() = this
    override fun build(): MaterialPopupMenu.PopupMenuNavBackItem =
        MaterialPopupMenu.PopupMenuNavBackItem(data, resolveOnShowCallback())
}