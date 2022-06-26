package io.github.asm0dey.model

import java.time.ZonedDateTime

data class Author(val names: List<String>) {
    val lastName = names.first()
    val fullName = "$lastName, ${(names - lastName).joinToString(" ")}"
}

data class BookSequence(val name: String, val no: Int?)
data class FictionBook(
    val authors: List<Author>,
    val genre: List<String>,
    val title: String,
    val bookSequence: BookSequence?,
    val written: String?,
) {
    val fileName: String = "${if (bookSequence?.no != null) "${bookSequence.no}. $title" else title}.fb2"
    val fullFileName: String = run {
        val no = if (bookSequence?.no != null) bookSequence.no.toString() + ". " else ""
        val author = authors.first().fullName + ". "
        "$no$author$title.fb2"
    }

}

data class Entry(
    val title: String,
    val links: List<Link>,
    val id: String,
    val summary: String? = null,
    val updated: ZonedDateTime,
) {
    constructor(
        title: String,
        id: String,
        summary: String? = null,
        updated: ZonedDateTime,
        vararg links: Link,
    ) : this(title, links.toList(), id, summary, updated)

    data class Link(
        val rel: String,
        val href: String,
        val type: String,
        val count: Long? = null,
        val title: String? = null,
        val facetGroup: String? = null,
        val activeFacet: Boolean? = null,

        )
}