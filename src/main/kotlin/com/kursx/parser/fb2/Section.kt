package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
class Section : IdElement {
    var image: Image? = null
    var annotation: Annotation? = null
    protected var epigraphs: ArrayList<Epigraph> = arrayListOf()
    protected var sections: ArrayList<Section> = arrayListOf()
    protected var elements: ArrayList<Element> = arrayListOf()
    protected var title: Title? = null

    constructor()
    internal constructor(root: Node) : super(root) {
        val body = root.childNodes
        for (item in 0 until body.length) {
            val node = body.item(item)
            when (node.nodeName) {
                "title" -> title = Title(node)
                "elements" -> annotation = Annotation(node)
                "image" -> {
                    elements.add(P(Image(node)))
                }

                "epigraph" -> {
                    epigraphs.add(Epigraph(node))
                }

                "section" -> {
                    sections.add(Section(node))
                }

                "poem" -> {
                    elements.add(Poem(node))
                }

                "subtitle" -> {
                    elements.add(Subtitle(node))
                }

                "p" -> {
                    elements.add(P(node))
                }

                "empty-line" -> {
                    elements.add(EmptyLine())
                }

                "cite" -> {
                    elements.add(Cite(node))
                }
            }
        }
    }


    fun getTitleString(innerDivider: String, outerDivider: String): String {
        if (title == null) return ""
        val builder = StringBuilder()
        val list = ArrayList<Element>(
            title!!.paragraphs
        )
        builder.append(Element.getText(list, innerDivider)).append(outerDivider)
        return builder.substring(0, builder.length - outerDivider.length)
    }


    override fun toString(): String {
        var data = getTitleString(". ", "\n")
        if (elements.isNotEmpty()) {
            data += " p: " + elements.size
        }
        if (sections.isNotEmpty()) {
            data += " section: " + sections.size
        }
        return data.trim { it <= ' ' }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Section

        if (image != other.image) return false
        if (annotation != other.annotation) return false
        if (epigraphs != other.epigraphs) return false
        if (sections != other.sections) return false
        if (elements != other.elements) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (annotation?.hashCode() ?: 0)
        result = 31 * result + epigraphs.hashCode()
        result = 31 * result + sections.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        return result
    }

}
