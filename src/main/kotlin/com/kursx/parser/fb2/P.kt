package com.kursx.parser.fb2

import com.kursx.parser.fb2.fonts.Emphasis
import com.kursx.parser.fb2.fonts.StrikeThrough
import com.kursx.parser.fb2.fonts.Strong
import kotlinx.serialization.Serializable
import org.w3c.dom.Node

@Suppress("unused")
@Serializable
open class P : Element {
    var images: ArrayList<Image>? = null
        protected set
    protected var emphasis: ArrayList<Emphasis>? = null
    protected var strong: ArrayList<Strong>? = null
    protected var strikeThrough: ArrayList<StrikeThrough>? = null

    //    TODO
    //    Для нижних индексов <sub>, а для верхних индексов <sup>
    //    Программный код - <code>
    //    <subtitle>* * *</subtitle>
    //  <cite>
    //  <p>Время - деньги.<p>
    //  <text-author>Бенджамин Франклин</text-author>
    //  </cite>
    //  <p>Об этом вы можете прочитать <a l:href="#n1">здесь</a>.</p>
    //  <p>text<a l:href="#n_2" type="note">[2]</a>
    constructor() : super()
    constructor(image: Image) : super() {
        images = ArrayList()
        images!!.add(image)
    }

    constructor(p: Node) : super(p) {
        val nodeList = p.childNodes
        for (index in 0 until nodeList.length) {
            val node = nodeList.item(index)
            when (nodeList.item(index).nodeName) {
                "image" -> {
                    if (images == null) images = ArrayList()
                    images!!.add(Image(node))
                }

                "strikethrough" -> {
                    if (strikeThrough == null) strikeThrough = ArrayList()
                    strikeThrough!!.add(StrikeThrough(node.textContent, p.textContent))
                }

                "strong" -> {
                    if (strong == null) strong = ArrayList()
                    strong!!.add(Strong(node.textContent, p.textContent))
                }

                "emphasis", "subtitle" -> {
                    if (emphasis == null) emphasis = ArrayList()
                    emphasis!!.add(Emphasis(node.textContent, p.textContent))
                }
            }
        }
    }

    constructor(p: String?) : super(p)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as P

        if (images != other.images) return false
        if (emphasis != other.emphasis) return false
        if (strong != other.strong) return false
        if (strikeThrough != other.strikeThrough) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (images?.hashCode() ?: 0)
        result = 31 * result + (emphasis?.hashCode() ?: 0)
        result = 31 * result + (strong?.hashCode() ?: 0)
        result = 31 * result + (strikeThrough?.hashCode() ?: 0)
        return result
    }

}
