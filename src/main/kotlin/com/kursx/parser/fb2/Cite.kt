package com.kursx.parser.fb2

import org.w3c.dom.Node

//http://www.fictionbook.org/index.php/Элемент_cite
@Suppress("unused")
class Cite : Element {
    var id: String? = null
    var lang: String? = null
    protected var elements: ArrayList<Element> = arrayListOf()
    protected var textAuthor: ArrayList<TextAuthor> = arrayListOf()

    constructor()
    internal constructor(node: Node) {
        val attrs = node.attributes
        for (index in 0 until attrs.length) {
            val attr = attrs.item(index)
            if (attr.nodeName == "id") {
                id = attr.nodeValue
            }
            if (attr.nodeName == "xml:lang") {
                lang = attr.nodeValue
            }
        }
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val paragraph = nodeList.item(i)
            when (paragraph.nodeName) {
                "text-author" -> {
                    if (textAuthor == null) textAuthor = ArrayList()
                    textAuthor!!.add(TextAuthor(paragraph))
                }

                "poem" -> {
                    if (elements == null) elements = ArrayList()
                    elements!!.add(Poem(paragraph))
                }

                "subtitle" -> {
                    if (elements == null) elements = ArrayList()
                    elements!!.add(Subtitle(paragraph))
                }

                "p" -> {
                    if (elements == null) elements = ArrayList()
                    elements!!.add(P(paragraph))
                }

                "empty-line" -> {
                    elements.add(EmptyLine())
                }
            }
        }
    }


    override var text: String?
        get() {
            val list = ArrayList(elements)
            list.addAll(textAuthor)
            return getText(list, "\n")
        }
        set(text) {
            super.text = text
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cite

        if (id != other.id) return false
        if (lang != other.lang) return false
        if (elements != other.elements) return false
        if (textAuthor != other.textAuthor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (lang?.hashCode() ?: 0)
        result = 31 * result + elements.hashCode()
        result = 31 * result + textAuthor.hashCode()
        return result
    }

}
