package dev.umerov.project.settings

import android.content.Context
import android.graphics.Color
import dev.umerov.project.R


object CurrentTheme {
    fun getColorPrimary(context: Context?): Int {
        return context?.let {
            getColorFromAttrs(
                androidx.appcompat.R.attr.colorPrimary,
                it,
                "#000000"
            )
        } ?: Color.TRANSPARENT
    }

    fun getColorControlNormal(context: Context?): Int {
        return context?.let {
            getColorFromAttrs(
                androidx.appcompat.R.attr.colorControlNormal,
                it, "#000000"
            )
        } ?: Color.TRANSPARENT
    }

    fun getColorToast(context: Context): Int {
        return getColorFromAttrs(R.attr.toast_background, context, "#ffffff")
    }

    fun getColorWhiteContrastFix(context: Context): Int {
        return getColorFromAttrs(R.attr.white_color_contrast_fix, context, "#ffffff")
    }

    fun getColorBlackContrastFix(context: Context): Int {
        return getColorFromAttrs(R.attr.black_color_contrast_fix, context, "#000000")
    }

    fun getColorOnPrimary(context: Context): Int {
        return getColorFromAttrs(
            com.google.android.material.R.attr.colorOnPrimary,
            context,
            "#000000"
        )
    }

    fun getColorSurface(context: Context): Int {
        return getColorFromAttrs(
            com.google.android.material.R.attr.colorSurface,
            context,
            "#000000"
        )
    }

    fun getColorOnSurface(context: Context): Int {
        return getColorFromAttrs(
            com.google.android.material.R.attr.colorOnSurface,
            context,
            "#000000"
        )
    }

    fun getColorBackground(context: Context): Int {
        return getColorFromAttrs(android.R.attr.colorBackground, context, "#000000")
    }

    fun getColorOnBackground(context: Context): Int {
        return getColorFromAttrs(
            com.google.android.material.R.attr.colorOnBackground,
            context,
            "#000000"
        )
    }

    fun getStatusBarColor(context: Context): Int {
        return getColorFromAttrs(android.R.attr.statusBarColor, context, "#000000")
    }

    fun getNavigationBarColor(context: Context): Int {
        return getColorFromAttrs(android.R.attr.navigationBarColor, context, "#000000")
    }

    fun getColorSecondary(context: Context?): Int {
        return context?.let {
            getColorFromAttrs(
                com.google.android.material.R.attr.colorSecondary,
                it,
                "#000000"
            )
        } ?: Color.TRANSPARENT
    }

    fun getStatusBarNonColored(context: Context): Int {
        return getColorFromAttrs(R.attr.statusBarNonColoredColor, context, "#000000")
    }

    fun getColorFromAttrs(resId: Int, context: Context, defaultColor: String): Int {
        val attribute = intArrayOf(resId)
        val array = context.theme.obtainStyledAttributes(attribute)
        val color = array.getColor(0, Color.parseColor(defaultColor))
        array.recycle()
        return color
    }

    fun getSecondaryTextColorCode(context: Context): Int {
        return getColorFromAttrs(android.R.attr.textColorSecondary, context, "#000000")
    }
}
