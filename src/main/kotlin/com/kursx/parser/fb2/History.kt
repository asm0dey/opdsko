package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Serializable
class History  //    TODO http://www.fictionbook.org/index.php/Элемент_history
    : Annotation {
    internal constructor(node: Node?) : super(node!!)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}
