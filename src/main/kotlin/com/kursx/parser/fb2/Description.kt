package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Document

@Suppress("unused")
@Serializable
class Description {
    var titleInfo: TitleInfo? = null
        protected set
    var srcTitleInfo: SrcTitleInfo? = null
        protected set
    var documentInfo: DocumentInfo? = null
        protected set
    var publishInfo: PublishInfo? = null
        protected set
    var customInfo = ArrayList<CustomInfo>()
        protected set

    constructor()
    internal constructor(doc: Document) {
        titleInfo = TitleInfo(doc)
        srcTitleInfo = SrcTitleInfo(doc)
        documentInfo = DocumentInfo(doc)
        publishInfo = PublishInfo(doc)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Description

        if (titleInfo != other.titleInfo) return false
        if (srcTitleInfo != other.srcTitleInfo) return false
        if (documentInfo != other.documentInfo) return false
        if (publishInfo != other.publishInfo) return false
        if (customInfo != other.customInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = titleInfo?.hashCode() ?: 0
        result = 31 * result + (srcTitleInfo?.hashCode() ?: 0)
        result = 31 * result + (documentInfo?.hashCode() ?: 0)
        result = 31 * result + (publishInfo?.hashCode() ?: 0)
        result = 31 * result + customInfo.hashCode()
        return result
    }

}
