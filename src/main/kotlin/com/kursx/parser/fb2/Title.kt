package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Title {
    var paragraphs = ArrayList<P>()
        protected set

    constructor()
    internal constructor(root: Node) {
        val body = root.childNodes
        for (item in 0 until body.length) {
            val node = body.item(item)
            if ("p" == node.nodeName) {
                paragraphs.add(P(node))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Title

        return paragraphs == other.paragraphs
    }

    override fun hashCode(): Int {
        return paragraphs.hashCode()
    }

}
