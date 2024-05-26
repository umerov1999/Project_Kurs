package dev.umerov.project.view.snake

import androidx.annotation.IntDef

@IntDef(Direction.UP, Direction.DOWN, Direction.RIGHT, Direction.LEFT)
@Retention(AnnotationRetention.SOURCE)
annotation class Direction {
    companion object {
        const val UP = 0
        const val DOWN = 1
        const val RIGHT = 2
        const val LEFT = 3
    }
}