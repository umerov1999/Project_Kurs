package dev.umerov.project.view.snake

interface Delegate {
    fun moveUp()
    fun moveDown()
    fun moveLeft()
    fun moveRight()

    fun onSurfaceCreated(pixelFormat: Int, width: Int, height: Int)

    val snake: SnakeModel
}
