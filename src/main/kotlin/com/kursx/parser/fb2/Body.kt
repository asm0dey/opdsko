package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

//http://www.fictionbook.org/index.php/Элемент_body
@Suppress("unused")
@Serializable
class Body {
    var lang: String? = null
    var name: String? = null
    var title: Title? = null
    var image: Image? = null
    var sections = ArrayList<Section>()
    var epigraphs: ArrayList<Epigraph>? = null

    constructor()
    internal constructor(body: Node) {
        val attrs = body.attributes
        for (index in 0 until attrs.length) {
            val attr = attrs.item(index)
            if (attr.nodeName == "name") {
                name = attr.nodeValue
            }
            if (attr.nodeName == "xml:lang") {
                lang = attr.nodeValue
            }
        }
        val map = body.childNodes
        for (index in 0 until map.length) {
            val node = map.item(index)
            when (node.nodeName) {
                "section" -> sections.add(Section(node))
                "title" -> title = Title(node)
                "name" -> name = node.textContent
                "image" -> image = Image(node)
                "epigraph" -> {
                    if (epigraphs == null) epigraphs = ArrayList()
                    epigraphs!!.add(Epigraph(node))
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Body

        if (lang != other.lang) return false
        if (name != other.name) return false
        if (title != other.title) return false
        if (image != other.image) return false
        if (sections != other.sections) return false
        if (epigraphs != other.epigraphs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lang?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + sections.hashCode()
        result = 31 * result + (epigraphs?.hashCode() ?: 0)
        return result
    }

}