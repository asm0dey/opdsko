package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.w3c.dom.Node

@Serializable
open class Element {
    companion object {
        fun getText(list: ArrayList<Element>, divider: String): String {
            val text = StringBuilder()
            for (p in list) {
                text.append(p.text).append(divider)
            }
            return if (text.length <= divider.length) "" else text.substring(0, text.length - divider.length)
                .trim { it <= ' ' }
        }
    }

    @ProtoNumber(1)
    open var text: String? = null
        protected set


    constructor() {
        text = null
    }

    constructor(p: Node) {
        text = p.textContent
    }

    constructor(p: String?) {
        text = p
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Element

        return text == other.text
    }

    override fun hashCode(): Int {
        return text?.hashCode() ?: 0
    }


}
