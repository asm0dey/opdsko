package com.kursx.parser.fb2

/**
 * Created by kurs on 30.7.17.
 */
@Suppress("unused")
class Gson{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
