package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Sequence {
    var name: String? = null
        protected set
    var number: String? = null
        protected set

    constructor()
    internal constructor(node: Node) {
        val map = node.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if (attr.nodeName == "name") {
                name = attr.nodeValue
            } else if (attr.nodeName == "number") {
                number = attr.nodeValue
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sequence

        if (name != other.name) return false
        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (number?.hashCode() ?: 0)
        return result
    }

}
