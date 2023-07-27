package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.w3c.dom.Document

@Suppress("unused")
@Serializable
open class TitleInfo {
    @ProtoNumber(1)
    var genres = ArrayList<String>()

    //  TODO http://www.fictionbook.org/index.php/Жанры_FictionBook_2.1
    @ProtoNumber(2)
    var keywords = ArrayList<String>()
        protected set
    @ProtoNumber(3)
    var bookTitle: String? = null
        protected set
    @ProtoNumber(4)
    var date: String? = null
        protected set
    @ProtoNumber(5)
    var lang: String? = null
    @ProtoNumber(6)
    var srcLang: String? = null
        protected set
    @ProtoNumber(7)
    var authors = ArrayList<Person>()
        protected set
    @ProtoNumber(8)
    var translators = ArrayList<Person>()
        protected set
    @ProtoNumber(9)
    var annotation: Annotation? = null
        protected set
    @ProtoNumber(10)
    var coverPage = ArrayList<Image>()
        protected set
    @ProtoNumber(11)
    var sequence: Sequence? = null
        protected set

    constructor()
    internal constructor(document: Document) {
        val description = document.getElementsByTagName("title-info")
        for (item in 0 until description.length) {
            val map = description.item(item).childNodes
            for (index in 0 until map.length) {
                val node = map.item(index)
                when (node.nodeName) {
                    "sequence" -> sequence = Sequence(node)
                    "coverpage" -> {
                        val images = node.childNodes
                        var image = 0
                        while (image < images.length) {
                            if (images.item(image).nodeName == "image") {
                                coverPage.add(Image(images.item(image)))
                            }
                            image++
                        }
                    }

                    "annotation" -> annotation = Annotation(node)
                    "date" -> date = node.textContent
                    "author" -> authors.add(Person(node))
                    "translator" -> translators.add(Person(node))
                    "keywords" -> keywords.add(node.textContent)
                    "genre" -> genres.add(node.textContent)
                    "book-title" -> bookTitle = node.textContent
                    "lang" -> lang = node.textContent
                    "src-lang" -> srcLang = node.textContent
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TitleInfo

        if (genres != other.genres) return false
        if (keywords != other.keywords) return false
        if (bookTitle != other.bookTitle) return false
        if (date != other.date) return false
        if (lang != other.lang) return false
        if (srcLang != other.srcLang) return false
        if (authors != other.authors) return false
        if (translators != other.translators) return false
        if (annotation != other.annotation) return false
        if (coverPage != other.coverPage) return false
        if (sequence != other.sequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = genres.hashCode()
        result = 31 * result + keywords.hashCode()
        result = 31 * result + (bookTitle?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (lang?.hashCode() ?: 0)
        result = 31 * result + (srcLang?.hashCode() ?: 0)
        result = 31 * result + authors.hashCode()
        result = 31 * result + translators.hashCode()
        result = 31 * result + (annotation?.hashCode() ?: 0)
        result = 31 * result + coverPage.hashCode()
        result = 31 * result + (sequence?.hashCode() ?: 0)
        return result
    }

}
