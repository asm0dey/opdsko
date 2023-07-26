package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Poem : Element {
    var title: Title? = null
        protected set
    var epigraphs: ArrayList<Epigraph>? = null
        protected set
    protected var stanza = ArrayList<Stanza>()
    var textAuthor: String? = null
        protected set
    var date: String? = null
        protected set

    constructor()
    internal constructor(node: Node) {
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val paragraph = nodeList.item(i)
            when (paragraph.nodeName) {
                "text-author" -> textAuthor = paragraph.textContent
                "title" -> title = Title(paragraph)
                "epigraph" -> {
                    if (epigraphs == null) epigraphs = ArrayList()
                    epigraphs!!.add(Epigraph(paragraph))
                }

                "date" -> date = paragraph.textContent
                "stanza" -> stanza.add(Stanza(paragraph))
            }
        }
    }

    override var text: String?
        get() {
            val list = ArrayList<Element>()
            if (title != null) list.addAll(title!!.paragraphs)
            for (stanza1 in stanza) {
                if (stanza1.title != null) {
                    for (title1 in stanza1.title!!) {
                        if (title1 != null) list.addAll(title1.paragraphs)
                    }
                }
                list.addAll(stanza1.stanza!!)
            }
            return getText(list, "\n")
        }
        set(text) {
            super.text = text
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Poem

        if (title != other.title) return false
        if (epigraphs != other.epigraphs) return false
        if (stanza != other.stanza) return false
        if (textAuthor != other.textAuthor) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (epigraphs?.hashCode() ?: 0)
        result = 31 * result + stanza.hashCode()
        result = 31 * result + (textAuthor?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }

}
