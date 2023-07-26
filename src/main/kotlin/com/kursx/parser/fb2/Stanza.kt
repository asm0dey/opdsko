package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Stanza {
    internal constructor(node: Node) {
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val paragraph = nodeList.item(i)
            when (paragraph.nodeName) {
                "title" -> {
                    title.add(Title(paragraph))
                }

                "subtitle" -> {
                    stanza.add(Subtitle(paragraph))
                }

                "v" -> {
                    stanza.add(V(paragraph))
                }
            }
        }
    }

    var title: ArrayList<Title> = arrayListOf()
    var stanza: ArrayList<Element> = arrayListOf()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stanza

        if (title != other.title) return false
        if (stanza != other.stanza) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + stanza.hashCode()
        return result
    }

}
