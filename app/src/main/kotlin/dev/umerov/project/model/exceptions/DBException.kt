package dev.umerov.project.model.exceptions

class DBException(@DBExceptionType val id: Int) : Throwable(id.toString())