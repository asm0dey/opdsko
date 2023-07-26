package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Epigraph : IdElement {
    var elements = ArrayList<Element>()
    var textAuthor: ArrayList<TextAuthor>? = ArrayList()

    constructor()
    internal constructor(root: Node) {
        val map = root.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if (attr.nodeName == "id") {
                id = attr.nodeValue
            }
        }
        val body = root.childNodes
        for (item in 0 until body.length) {
            val node = body.item(item)
            when (node.nodeName) {
                "text-author" -> {
                    if (textAuthor == null) textAuthor = ArrayList()
                    textAuthor!!.add(TextAuthor(node))
                }

                "poem" -> elements.add(Poem(node))
                "cite" -> elements.add(Cite(node))
                "p" -> elements.add(P(node))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Epigraph

        if (elements != other.elements) return false
        if (textAuthor != other.textAuthor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = elements.hashCode()
        result = 31 * result + (textAuthor?.hashCode() ?: 0)
        return result
    }
}
