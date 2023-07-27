package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.w3c.dom.Document

@Suppress("unused")
@Serializable
class PublishInfo {
    @ProtoNumber(1)
    var bookName: String? = null
        protected set
    @ProtoNumber(2)
    var city: String? = null
        protected set
    @ProtoNumber(3)
    var year: String? = null
        protected set
    @ProtoNumber(4)
    var publisher: String? = null
        protected set
    @ProtoNumber(5)
    var isbn: String? = null
        protected set
    @ProtoNumber(6)
    var sequence: Sequence? = null
        protected set

    constructor()
    internal constructor(document: Document) {
        val description = document.getElementsByTagName("publish-info")
        for (item in 0 until description.length) {
            val map = description.item(item).childNodes
            for (index in 0 until map.length) {
                val node = map.item(index)
                when (node.nodeName) {
                    "book-name" -> bookName = node.textContent
                    "city" -> city = node.textContent
                    "year" -> year = node.textContent
                    "isbn" -> isbn = node.textContent
                    "publisher" -> publisher = node.textContent
                    "sequence" -> sequence = Sequence(node)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublishInfo

        if (bookName != other.bookName) return false
        if (city != other.city) return false
        if (year != other.year) return false
        if (publisher != other.publisher) return false
        if (isbn != other.isbn) return false
        if (sequence != other.sequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bookName?.hashCode() ?: 0
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (publisher?.hashCode() ?: 0)
        result = 31 * result + (isbn?.hashCode() ?: 0)
        result = 31 * result + (sequence?.hashCode() ?: 0)
        return result
    }
}
