package io.github.asm0dey.inpx

data class Author(val names: List<String>)

data class BookSequence(val name: String, val no: Int?)
data class FictionBook(
    val authors: List<Author>,
    val genres: List<String>,
    val title: String,
    val bookSequence: BookSequence?,
    val date: String?,
    val lang: String?
)