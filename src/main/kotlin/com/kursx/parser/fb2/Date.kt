package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Date {
    var value: String? = null
        protected set
    var date: String? = null
        protected set

    constructor()
    internal constructor(node: Node) {
        val map = node.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if (attr.nodeName == "value") {
                value = attr.nodeValue
            }
        }
        date = node.textContent
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Date

        if (value != other.value) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }

}