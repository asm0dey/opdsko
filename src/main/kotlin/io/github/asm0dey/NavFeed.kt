package io.github.asm0dey

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.text.SimpleDateFormat

val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
const val NAVIGATION_TYPE = "application/atom+xml;profile=opds-catalog;kind=navigation"
const val ACQUISITION_TYPE = "application/atom+xml;profile=opds-catalog;kind=acquisition"

fun selfLink(path: String, feedType: String) = NavFeed.NavLink("self", path, feedType)
fun startLink() = NavFeed.NavLink("start", "/opds", NAVIGATION_TYPE)


@Serializable
@XmlSerialName("feed", "http://www.w3.org/2005/Atom", "")
data class NavFeed(
    @XmlElement(true) @XmlSerialName("id", "http://www.w3.org/2005/Atom", "") val id: String,
    @XmlElement(true) @XmlSerialName("title", "http://www.w3.org/2005/Atom", "") val title: String,
    @XmlElement(true) @XmlSerialName("updated", "http://www.w3.org/2005/Atom", "") val updated: String,
    val author: List<XAuthor>,
    val links: List<NavLink>,
    val entries: List<Entry>,
) {
    @Serializable
    @XmlSerialName("author", "http://www.w3.org/2005/Atom", "")
    data class XAuthor(
        @XmlElement(true) @XmlSerialName("name", "http://www.w3.org/2005/Atom", "") val name: String,
        @XmlElement(true) @XmlSerialName("uri", "http://www.w3.org/2005/Atom", "") val uri: String,
    )

    @Serializable
    @XmlSerialName("link", "http://www.w3.org/2005/Atom", "")
    data class NavLink(
        @XmlElement(false) @XmlSerialName("type", "", "") val type: String,
        @XmlElement(false) @XmlSerialName("rel", "", "") val rel: String,
        @XmlElement(false) @XmlSerialName("href", "", "") val href: String,
    )

    @Serializable
    sealed interface Entry

    @Serializable
    @XmlSerialName("entry", "http://www.w3.org/2005/Atom", "")
    data class NavEntry(
        @XmlElement(true) @XmlSerialName("title", "http://www.w3.org/2005/Atom", "atom") val name: String,
        val link: NavLink,
        @XmlElement(true) @XmlSerialName("id", "http://www.w3.org/2005/Atom", "atom") val id: String,
        @XmlElement(true) @XmlSerialName("summary", "http://www.w3.org/2005/Atom", "atom") val description: String? = null,
        @XmlElement(true) @XmlSerialName("updated", "http://www.w3.org/2005/Atom", "atom") val updated: String,
    ) : Entry

    @Serializable
    @XmlSerialName("entry", "http://www.w3.org/2005/Atom", "atom")
    data class BookEntry(
        @XmlElement(true) @XmlSerialName("title", "http://www.w3.org/2005/Atom", "atom") val title: String,
        @XmlElement(true) @XmlSerialName("id", "http://www.w3.org/2005/Atom", "atom") val id: String,
        val author: List<XAuthor>,
        @XmlElement(true) @XmlSerialName("published", "http://www.w3.org/2005/Atom", "atom") val published: String,
        @XmlElement(true) @XmlSerialName("language", "http://purl.org/dc/terms/", "dcterms") val lang: String?,
        @XmlElement(true) @XmlSerialName("date", "http://purl.org/dc/terms/", "dcterms") val date: String?,
        val genres: List<XCategory>,
        val links: List<NavLink>,
        @XmlElement(true) @XmlSerialName("summary", "http://www.w3.org/2005/Atom", "atom") val summary: String?,
        @XmlElement(true) @XmlSerialName("content", "http://www.w3.org/2005/Atom", "atom") @XmlChildrenName("p","http://www.w3.org/1999/xhtml","x") val content: List<String> = listOf(),
    ) : Entry {
        @Serializable
        @XmlSerialName("category", "http://www.w3.org/2005/Atom", "atom")
        data class XCategory(
            @XmlElement(false) @XmlSerialName("term", "", "") val term: String,
            @XmlElement(false) @XmlSerialName("term", "", "") val label: String = term,
        )
    }
}
