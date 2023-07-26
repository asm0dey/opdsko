package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Document

@Serializable
class SrcTitleInfo : TitleInfo {
    constructor(doc: Document?) : super(doc!!)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}
