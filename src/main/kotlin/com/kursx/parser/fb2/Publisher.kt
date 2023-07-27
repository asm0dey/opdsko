package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Publisher : Person {
    @ProtoNumber(1)
    var lang: String? = null

    constructor()
    constructor(node: Node) : super(node) {
        val map = node.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if (attr.nodeName == "lang") {
                id = attr.nodeValue
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Publisher

        return lang == other.lang
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (lang?.hashCode() ?: 0)
        return result
    }

}
