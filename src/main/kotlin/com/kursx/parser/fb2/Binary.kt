package com.kursx.parser.fb2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import org.w3c.dom.Node

//http://www.fictionbook.org/index.php/Элемент_binary
@Suppress("unused")
@Serializable
class Binary : IdElement {
    var contentType: String? = null
    lateinit var binary: String

    constructor()
    internal constructor(node: Node) : super(node) {
        binary = node.textContent
        val map = node.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if ("content-type" == attr.nodeName) {
                contentType = attr.nodeValue
            }
        }
    }

    override fun toString(): String {
        return "Binary{" +
                "contentType='" + contentType + '\'' +
                ", binary='" + binary + '\'' +
                ", id='" + id + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Binary

        if (contentType != other.contentType) return false
        if (binary != other.binary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentType?.hashCode() ?: 0
        result = 31 * result + binary.hashCode()
        return result
    }

}
