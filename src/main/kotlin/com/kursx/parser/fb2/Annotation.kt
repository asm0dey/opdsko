package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

//http://www.fictionbook.org/index.php/Элемент_annotation
@Suppress("unused")
@Serializable
open class Annotation : IdElement {
    var text = ""
        private set
    var lang: String? = null
    var elements: ArrayList<Element>? = null

    constructor()
    internal constructor(node: Node) : super(node) {
        try {
            val writer = StringWriter()
            TransformerFactory.newInstance().newTransformer().transform(DOMSource(node), StreamResult(writer))
            val xml = writer.toString()
            text = Jsoup.parseBodyFragment(xml).body().text()
        } catch (ignored: TransformerException) {
        }
        val map = node.attributes
        for (index in 0 until map.length) {
            val attr = map.item(index)
            if (attr.nodeName == "xml:lang") {
                lang = attr.nodeValue
            }
        }
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val paragraph = nodeList.item(i)
            if (elements == null) elements = ArrayList()
            when (paragraph.nodeName) {
                "p" -> elements!!.add(P(paragraph))
                "poem" -> elements!!.add(Poem(paragraph))
                "cite" -> elements!!.add(Cite(paragraph))
                "subtitle" -> elements!!.add(Subtitle(paragraph))
                "empty-line" -> elements!!.add(EmptyLine())
                "table" -> elements!!.add(Table())
            }
        }
    }

    val annotations: ArrayList<Element>
        get() = if (elements == null) ArrayList() else elements!!

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Annotation

        if (text != other.text) return false
        if (lang != other.lang) return false
        if (elements != other.elements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (lang?.hashCode() ?: 0)
        result = 31 * result + (elements?.hashCode() ?: 0)
        return result
    }

}
