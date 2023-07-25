package io.github.asm0dey.inpx

import net.lingala.zip4j.ZipFile

object InpxParser {

    fun scan(inpxPath: String): MutableMap<Int, FictionBook> {
        val header = "AUTHOR;GENRE;TITLE;SERIES;SERNO;FILE;SIZE;LIBID;DEL;EXT;DATE;LANG;KEYWORDS".split(';')
            .mapIndexed { index, s -> s to index }.toMap()
        val zipFile = ZipFile(inpxPath)
        return zipFile
            .fileHeaders
            .asSequence()
            .filter { it.fileName.endsWith(".inp") }
            .flatMap {
                zipFile.getInputStream(it).buffered().use { input ->
                    input.reader().useLines { seq -> seq.toList() }
                }
            }
            .map { it.split(0x04.toChar()) }
            .filterNot { it[header["TITLE"]!!].isBlank() }
            .filterNot { breakColonDelimied(it[header["AUTHOR"]!!]).all(String::isBlank) }
            .filterNot { breakColonDelimied(it[header["AUTHOR"]!!]).flatMap(::breakCommaDelimied).all(String::isBlank) }
            .filter { it[header["DEL"]!!] == "0" }
            .distinctBy { it[header["FILE"]!!].toInt() }
            .groupBy { it[header["FILE"]!!].toInt() }
            .mapValues { it.value.single() }
            .mapValues {
//                Logger.tag("INPX").info { "Reading info for book ${it.key}" }
                val authorString = it.value[header["AUTHOR"]!!]
                val authors = breakColonDelimied(authorString).map { Author(breakCommaDelimied(it)) }
                val series = it.value[header["SERIES"]!!]
                val bookSequence = if (series.isNotBlank()) {
                    BookSequence(series, it.value[header["SERNO"]!!].toIntOrNull())
                } else null
                val genreString = it.value[header["GENRE"]!!]
                val genres = breakColonDelimied(genreString)
                val title = it.value[header["TITLE"]!!]
                val date = it.value[header["DATE"]!!]
                FictionBook(authors, genres, title, bookSequence, date, it.value[header["LANG"]!!])
            }
            .toMutableMap()

    }

    private fun breakColonDelimied(str: String) = str.split(':').filter(String::isNotBlank).map(String::trim)
    private fun breakCommaDelimied(str: String) = str.split(',').filter(String::isNotBlank).map(String::trim)
}

