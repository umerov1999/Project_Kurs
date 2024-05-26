package dev.umerov.project.module

object FileUtils {
    private external fun getThreadsCountNative(): Int

    val threadsCount: Int
        get() = getThreadsCountNative()
}
