package io.github.asm0dey.model

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
    val written: String?
) {
    val fileName: String = "${if (bookSequence?.no != null) "${bookSequence.no}. $title" else title}.fb2"
    val fullFileName: String = run {
        val no = if (bookSequence?.no != null) bookSequence.no.toString() + ". " else ""
        val author = authors.first().fullName + ". "
        "$no$author$title.fb2"
    }

}