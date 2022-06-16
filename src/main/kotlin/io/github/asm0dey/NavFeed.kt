package io.github.asm0dey

import io.ktor.http.*
import io.ktor.http.content.*
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.XmlVersion
import org.redundent.kotlin.xml.xml
import java.text.SimpleDateFormat
import java.util.*

private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
const val NAVIGATION_TYPE = "application/atom+xml;profile=opds-catalog;kind=navigation"
const val ACQUISITION_TYPE = "application/atom+xml;profile=opds-catalog;kind=acquisition"

sealed interface Entry
data class NavEntry(
    val name: String,
    val link: NavLink,
    val id: String,
    val description: String? = null,
) : Entry

data class NavLink(
    val rel: String,
    val href: String,
    val type: String,
)

data class BookEntry(
    val title: String,
    val id: String,
    val updated: Date,
    val author: Iterable<EntryAuthor>,
    val genres: List<String>?,
    val imageLink: NavLink?,
    val downloadLink: NavLink,
    val description: String?,
    val lang: String?,
    val issued: String?,
) : Entry

data class EntryAuthor(val name: String, var link: String)

data class NavFeed(
    private val path: String,
    val lastUpdate: Date,
    val entries: Iterable<Entry> = emptyList(),
    val title: String,
    val id: String,
    val feedType: String,
) :
    OutgoingContent.ByteArrayContent() {
    override val contentType: ContentType = ContentType(
        "application",
        "atom+xml",
        listOf(
            HeaderValueParam("profile", "opds-catalog"),
            HeaderValueParam("kind", "navigation"),
        )
    )

    override fun bytes(): ByteArray {
        val content = xml(version = XmlVersion.V10, root = "feed", encoding = "UTF-8") {
            namespaces()
            element("id", id)
            element("title", title)
            element("updated", df.format(lastUpdate))
            author("Pasha Finkelshteyn", "https://github.com/asm0dey/opdsKo")
            navLink("self", path, feedType)
            navLink("start", "/opds", NAVIGATION_TYPE)
            entries {
                for (entry in entries) {
                    when (entry) {
                        is NavEntry -> renderNavEntry(entry)
                        is BookEntry -> renderBookEntry(entry)
                    }
                }
            }
        }
        return content.toString(PrintOptions(singleLineTextElements = true)).toByteArray()
    }

    private fun EntriesContext.renderBookEntry(entry: BookEntry) {
        entry {
            title(entry.title)
            id(entry.id)
            for (entryAuthor in entry.author) {
                author {
                    name(entryAuthor.name)
                    uri(entryAuthor.link)
                }
            }
            published(df.format(entry.updated))
            entry.lang?.let {
                dcLanguage(it)
            }
            entry.issued?.let {
                dcDate(it)
            }
            entry.description?.let {
                description(it)
            }
            entry.genres?.let {
                for (genre in it) {
                    category(genre)
                }
            }
            entry.imageLink?.let { link(it.rel, it.href, it.type) }
            link(entry.downloadLink.rel, entry.downloadLink.href, entry.downloadLink.type)
        }
    }

    private fun EntriesContext.renderNavEntry(entry: NavEntry) {
        entry {
            title(entry.name)
            id(entry.id)
            entry.description?.let {
                description(it)
            }
            link(entry.link.rel, entry.link.href, entry.link.type)
            updated(df.format(lastUpdate))
        }
    }
}

class EntryContext(private val node: Node) {
    fun id(id: String) {
        node.element("id", id)
    }

    fun title(title: String) {
        node.element("title", title)
    }

    fun author(block: AuthorContext.() -> Unit) {
        node.element("author") {
            AuthorContext(this).block()
        }
    }

    fun description(description: String) {
        node.element("summary", description)
    }

    fun updated(date: String) {
        node.element("updated", date)
    }

    fun published(date: String) {
        node.element("published", date)
    }

    fun dcDate(date: String) {
        node.element("dcterms:date", date)
    }

    fun dcLanguage(lang: String) {
        node.element("dcterms:language", lang)
    }

    fun content(html: Node.() -> Unit) {
        node.element("content") {
            attribute("type", "xhtml")
            element("div") {
                xmlns = "http://www.w3.org/1999/xhtml"
                html()
            }
        }
    }

    fun related(href: String, title: String, type: String) {
        node.element("link") {
            attribute("href", href)
            attribute("type", type)
            attribute("title", title)
        }
    }

    fun category(text: String) {
        node.element("category") {
            attribute("term", text)
            attribute("label", text)
        }
    }

    fun link(rel: String, href: String, type: String) {
        node.element("link") {
            attribute("rel", rel)
            attribute("href", href)
            attribute("type", type)
        }
    }

}

class AuthorContext(private val node: Node) {
    fun name(name: String) {
        node.element("name", name)
    }

    fun uri(path: String) {
        node.element("uri", path)
    }
}

class EntriesContext(private val node: Node) {
    fun entry(function: EntryContext.() -> Unit) {
        node.element("entry") {
            EntryContext(this).function()
        }
    }
}

fun Node.entries(function: EntriesContext.() -> Unit) {
    EntriesContext(this).function()
}

private fun Node.author(name: String, uri: String) {
    element("author") {
        element("name", name)
        element("uri", uri)
    }
}

private fun Node.namespaces() {
    namespace("dcterms", "http://purl.org/dc/terms/")
    namespace("thr", "http://purl.org/syndication/thread/1.0")
    namespace("opds", "http://opds-spec.org/2010/catalog")
    namespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/")
    namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
    xmlns = "http://www.w3.org/2005/Atom"
}


private fun Node.navLink(rel: String, href: String, type: String) {
    element("link") {
        attribute("type", type)
        attribute("rel", rel)
        attribute("href", href)
    }
}