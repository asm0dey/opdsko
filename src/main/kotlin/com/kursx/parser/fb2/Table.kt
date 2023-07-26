package com.kursx.parser.fb2

import kotlinx.serialization.Serializable

@Serializable
class Table : Element() { //    TODO http://www.fictionbook.org/index.php/Элемент_table
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

}
