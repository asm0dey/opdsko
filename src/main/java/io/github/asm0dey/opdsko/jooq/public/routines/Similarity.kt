/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.routines


import org.jooq.Field
import org.jooq.Parameter
import org.jooq.impl.AbstractRoutine
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Similarity : AbstractRoutine<Float>("similarity", io.github.asm0dey.opdsko.jooq.`public`.Public.PUBLIC, SQLDataType.REAL) {
    companion object {

        /**
         * The parameter <code>public.similarity.RETURN_VALUE</code>.
         */
        val RETURN_VALUE: Parameter<Float?> = Internal.createParameter("RETURN_VALUE", SQLDataType.REAL, false, false)

        /**
         * The parameter <code>public.similarity._1</code>.
         */
        val _1: Parameter<String?> = Internal.createParameter("_1", SQLDataType.CLOB, false, true)

        /**
         * The parameter <code>public.similarity._2</code>.
         */
        val _2: Parameter<String?> = Internal.createParameter("_2", SQLDataType.CLOB, false, true)
    }

    init {
        returnParameter = Similarity.RETURN_VALUE
        addInParameter(Similarity._1)
        addInParameter(Similarity._2)
    }

    /**
     * Set the <code>_1</code> parameter IN value to the routine
     */
    fun set__1(value: String?): Unit = setValue(Similarity._1, value)

    /**
     * Set the <code>_1</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__1(field: Field<String?>): Similarity {
        setField(Similarity._1, field)
        return this
    }

    /**
     * Set the <code>_2</code> parameter IN value to the routine
     */
    fun set__2(value: String?): Unit = setValue(Similarity._2, value)

    /**
     * Set the <code>_2</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__2(field: Field<String?>): Similarity {
        setField(Similarity._2, field)
        return this
    }
}