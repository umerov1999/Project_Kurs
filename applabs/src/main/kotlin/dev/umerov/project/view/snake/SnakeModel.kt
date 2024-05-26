package dev.umerov.project.view.snake

import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable
import dev.umerov.project.getBoolean
import dev.umerov.project.putBoolean
import dev.umerov.project.readTypedObjectCompat
import dev.umerov.project.settings.Settings
import dev.umerov.project.writeTypedObjectCompat
import kotlin.random.Random

class SnakeModel : Parcelable {
    private var startingPosition: ArrayList<Point> = ArrayList()
    var segments: ArrayList<Point> = ArrayList()
    val cellSide: Int = 65
    val apple = Point()

    @Direction
    var direction: Int = Direction.RIGHT
    var isGameOver = false
    var score: Int = 0
    var highScore: Int = 0

    constructor() {
        reset()
    }

    constructor(parcel: Parcel) {
        startingPosition = ArrayList(parcel.createTypedArrayList(Point.CREATOR).orEmpty())
        segments = ArrayList(parcel.createTypedArrayList(Point.CREATOR).orEmpty())
        parcel.readTypedObjectCompat(Point.CREATOR)?.let {
            apple.x = it.x
            apple.y = it.y
        }
        direction = parcel.readInt()
        isGameOver = parcel.getBoolean()
        score = parcel.readInt()
        highScore = parcel.readInt()
    }

    fun copy(src: SnakeModel) {
        startingPosition = src.startingPosition
        segments = src.segments
        direction = src.direction
        isGameOver = src.isGameOver
        score = src.score
        highScore = src.highScore
    }

    fun reset() {
        score = 0
        highScore = Settings.get().main().snakeScore
        direction = arrayOf(Direction.UP, Direction.DOWN, Direction.RIGHT).random()
        val x = cellSide * (3..5).random()
        val y = cellSide * (2..5).random()

        startingPosition.clear()
        startingPosition.add(Point(x, y))
        startingPosition.add(Point(x - cellSide, y))
        startingPosition.add(Point(x - (2 * cellSide), y))

        segments.clear()

        createSnake()
        apple.x = Random.nextInt(5) * cellSide
        apple.y = Random.nextInt(5) * cellSide
        isGameOver = false
    }

    private fun createSnake() {
        for (position in startingPosition) {
            addSegment(position)
        }
    }

    private fun addSegment(position: Point) {
        segments.add(position)
    }

    fun extend() {
        addSegment(Point(segments[segments.size - 1]))
    }

    fun move() {
        for (i in segments.size - 1 downTo 1) {
            segments[i] = Point(segments[i - 1])
        }
        val head = segments[0]
        when (direction) {
            Direction.UP -> {
                head.y -= cellSide
            }

            Direction.DOWN -> {
                head.y += cellSide
            }

            Direction.LEFT -> {
                head.x -= cellSide
            }

            Direction.RIGHT -> {
                head.x += cellSide
            }
        }
    }

    fun moveUp() {
        direction = Direction.UP
    }

    fun moveDown() {
        direction = Direction.DOWN
    }

    fun moveLeft() {
        direction = Direction.LEFT
    }

    fun moveRight() {
        direction = Direction.RIGHT
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(startingPosition)
        dest.writeTypedList(segments)
        dest.writeTypedObjectCompat(apple, flags)
        dest.writeInt(direction)
        dest.putBoolean(isGameOver)
        dest.writeInt(score)
        dest.writeInt(highScore)
    }

    companion object CREATOR : Parcelable.Creator<SnakeModel> {
        override fun createFromParcel(parcel: Parcel): SnakeModel {
            return SnakeModel(parcel)
        }

        override fun newArray(size: Int): Array<SnakeModel?> {
            return arrayOfNulls(size)
        }
    }
}
