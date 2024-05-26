package dev.umerov.project.model.db

import androidx.annotation.IntDef

@IntDef(RegisterType.NULL, RegisterType.BALANCE)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterType {
    companion object {
        const val NULL = 0
        const val BALANCE = 1
    }
}
