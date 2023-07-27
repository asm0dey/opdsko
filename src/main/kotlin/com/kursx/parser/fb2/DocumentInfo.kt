package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.w3c.dom.Document

@Suppress("unused")
@Serializable
class DocumentInfo {
    @ProtoNumber(1)
    var authors = ArrayList<Person>()
        protected set
    @ProtoNumber(2)
    protected var publishers: ArrayList<Person>? = null
    @ProtoNumber(3)
    var programUsed: String? = null
        protected set
    @ProtoNumber(4)
    var srcUrl: String? = null
        protected set
    @ProtoNumber(5)
    var srcOcr: String? = null
        protected set
    @ProtoNumber(6)
    var email: String? = null
        protected set
    @ProtoNumber(7)
    var id: String? = null
        protected set
    @ProtoNumber(8)
    var version: String? = null
        protected set
    @ProtoNumber(9)
    var history: History? = null
        protected set
    @ProtoNumber(10)
    var date: Date? = null
        protected set

    constructor()
    internal constructor(document: Document) {
        val description = document.getElementsByTagName("document-info")
        for (item in 0 until description.length) {
            val map = description.item(item).childNodes
            for (index in 0 until map.length) {
                val node = map.item(index)
                when (node.nodeName) {
                    "author" -> authors.add(Person(node))
                    "publisher" -> {
                        if (publishers == null) publishers = ArrayList()
                        publishers!!.add(Person(node))
                    }

                    "program-used" -> programUsed = node.textContent
                    "email" -> email = node.textContent
                    "src-url" -> srcUrl = node.textContent
                    "src-ocr" -> srcOcr = node.textContent
                    "id" -> id = node.textContent
                    "version" -> version = node.textContent
                    "history" -> history = History(node)
                    "date" -> date = Date(node)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentInfo

        if (authors != other.authors) return false
        if (publishers != other.publishers) return false
        if (programUsed != other.programUsed) return false
        if (srcUrl != other.srcUrl) return false
        if (srcOcr != other.srcOcr) return false
        if (email != other.email) return false
        if (id != other.id) return false
        if (version != other.version) return false
        if (history != other.history) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authors.hashCode()
        result = 31 * result + (publishers?.hashCode() ?: 0)
        result = 31 * result + (programUsed?.hashCode() ?: 0)
        result = 31 * result + (srcUrl?.hashCode() ?: 0)
        result = 31 * result + (srcOcr?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (history?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        return result
    }
}