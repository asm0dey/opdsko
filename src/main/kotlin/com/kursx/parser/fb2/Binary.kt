package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.w3c.dom.Node

//http://www.fictionbook.org/index.php/Элемент_binary
@Suppress("unused")
@Serializable
class Binary : IdElement {
    @ProtoNumber(2)
    var contentType: String? = null
    @ProtoNumber(3)
    lateinit var binary: ByteArray

    constructor()
    internal constructor(node: Node) : super(node) {
        binary = org.apache.commons.codec.binary.Base64().decode(node.textContent)
        val map = node.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if ("content-type" == attr.nodeName) {
                contentType = attr.nodeValue
            }
        }
    }

    override fun toString(): String {
        return "Binary(contentType=$contentType, binary=${binary.contentToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Binary

        if (contentType != other.contentType) return false
        if (!binary.contentEquals(other.binary)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + binary.contentHashCode()
        return result
    }


}
