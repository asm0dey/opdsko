package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.protobuf.ProtoNumber
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.jsoup.parser.Parser
import org.tinylog.Logger
import org.tinylog.TaggedLogger
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

@Serializable
class FictionBook {
    @ProtoNumber(1)
    private var xmlns: Array<Xmlns?> = Array(0){null}
    @ProtoNumber(2)
    var description: Description? = null
        private set
    @Transient
    private var bodies: MutableList<Body> = ArrayList()
    @ProtoNumber(3)
    var binaries: MutableMap<String, Binary> = hashMapOf()
    @ProtoNumber(4)
    private var encoding = "utf-8"

    constructor(zipFile: ZipFile, fileHeader: FileHeader) {
        fun isp() = zipFile.getInputStream(fileHeader)
        val zipFilePath = zipFile.file.absolutePath + "#" + fileHeader.fileName
        val doc: Document
        try {
            encoding = zipFile.getInputStream(fileHeader).use { inp ->
                XMLInputFactory.newInstance().createXMLStreamReader(inp).characterEncodingScheme
            } ?: encoding
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            var foundIllegalCharacters = false
            BufferedReader(InputStreamReader(isp())).use { br ->
                try {
                    val line = StringBuilder(br.readLine().trim { it <= ' ' })
                    if (!line.toString().startsWith("<")) {
                        foundIllegalCharacters = true
                    }
                } catch (e: Exception) {
                    FB2_LOGGER.error(e)
                }
            }
            doc = if (foundIllegalCharacters) {
                FB2_LOGGER.debug { "Found illegal characters in $zipFilePath" }
                val text = StringBuilder()
                text.fillFromReader(BufferedReader(InputStreamReader(isp())))
                try {
                    FB2_LOGGER.debug { "  Parsing $zipFilePath without explicitly specified encoding" }
                    db.parse(InputSource(StringReader(text.toString())))
                } catch (sax: SAXException) {
                    FB2_LOGGER.warn(sax) { "    Parsing $zipFilePath filed, falling back to Jsoup" }
                    BufferedInputStream(isp()).use { br ->
                        W3CDom.convert(Jsoup.parse(br, encoding, "https://yandex.ru", Parser.xmlParser()))
                    }
                }
            } else {
                try {
                    FB2_LOGGER.debug { "Parsing $zipFilePath with encoding $encoding" }
                    db.fromEncodedInputStream(isp())
                } catch (sax: SAXException) {
                    FB2_LOGGER.warn(sax) { "  Parsing $zipFilePath filed, falling back to Jsoup" }
                    BufferedInputStream(isp()).use { br ->
                        W3CDom.convert(Jsoup.parse(br, encoding, "https://yandex.ru", Parser.xmlParser()))
                    }
                }
            }
        } catch (e: IOException) {
            FB2_LOGGER.error(e) { "Error while processing $zipFilePath" }
            throw e
        } catch (e: ParserConfigurationException) {
            FB2_LOGGER.error(e) { "Error while processing $zipFilePath" }
            throw e
        } catch (e: XMLStreamException) {
            FB2_LOGGER.error(e) { "Error while processing $zipFilePath" }
            throw e
        }
        afterInit(doc, zipFilePath)
    }

    constructor(file: File) {
        val doc: Document
        try {
            encoding = FileReader(file).use { reader ->
                XMLInputFactory.newInstance().createXMLStreamReader(reader).characterEncodingScheme
            } ?: encoding
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            var foundIllegalCharacters = false
            BufferedReader(FileReader(file)).use { br ->
                try {
                    val line = StringBuilder(br.readLine().trim { it <= ' ' })
                    if (!line.toString().startsWith("<")) {
                        foundIllegalCharacters = true
                    }
                } catch (e: Exception) {
                    FB2_LOGGER.error(e)
                }
            }
            doc = if (foundIllegalCharacters) {
                FB2_LOGGER.debug { "Found illegal characters in ${file.absolutePath}" }
                val text = StringBuilder()
                text.fillFromReader(BufferedReader(FileReader(file)))
                try {
                    FB2_LOGGER.debug { "Parsing ${file.absolutePath} without explicitly specified encoding" }
                    db.parse(InputSource(StringReader(text.toString())))
                } catch (sax: SAXException) {
                    FB2_LOGGER.warn(sax) { "Parsing ${file.absolutePath} filed, falling back to Jsoup" }
                    W3CDom.convert(Jsoup.parse(file, encoding, "https://yandex.ru", Parser.xmlParser()))
                }
            } else {
                try {
                    FB2_LOGGER.debug { "Parsing ${file.absolutePath} with encoding $encoding" }
                    db.fromEncodedInputStream(FileInputStream(file))
                } catch (sax: SAXException) {
                    FB2_LOGGER.warn(sax) { "Parsing ${file.absolutePath} filed, falling back to Jsoup" }
                    W3CDom.convert(Jsoup.parse(file, encoding, "https://yandex.ru", Parser.xmlParser()))
                }
            }
        } catch (e: Exception) {
            when (e) {
                is IOException, is ParserConfigurationException, is XMLStreamException ->
                    FB2_LOGGER.error(e) { "Error while processing ${file.absolutePath}" }
            }
            throw e
        }
        afterInit(doc, file.absolutePath)
    }

    private fun StringBuilder.fillFromReader(reader: BufferedReader) = reader.use { br ->
        var line = br.readLine()
        if (line != null && line.contains("<")) {
            line = line.substring(line.indexOf("<"))
        }
        while (line != null) {
            append(line)
            line = br.readLine()
        }
    }

    private fun DocumentBuilder.fromEncodedInputStream(inputStream: InputStream): Document {
        return InputStreamReader(BufferedInputStream(inputStream), encoding).use { br ->
            parse(InputSource(br))
        }
    }

    private fun afterInit(doc: Document, path: String) {
        initXmlns(doc)
        description = Description(doc)
        val bodyNodes = doc.getElementsByTagName("body")
        for (item in 0 until bodyNodes.length) {
            bodies.add(Body(bodyNodes.item(item)))
        }
        val binary = doc.getElementsByTagName("binary")
        for (item in 0 until binary.length) {
            val binary1 = Binary(binary.item(item))
            try {
                binaries[binary1.id!!.replace("#", "")] = binary1
            } catch (e: Exception) {
                FB2_LOGGER.error(e) { "Invalid binary $binary1 in file $path" }
            }
        }
    }

    private fun setXmlns(nodeList: ArrayList<Node?>) {
        xmlns = arrayOfNulls(nodeList.size)
        for (index in nodeList.indices) {
            val node = nodeList[index]!!
            xmlns[index] = Xmlns(node)
        }
    }

    private fun initXmlns(doc: Document) {
        val fictionBook = doc.getElementsByTagName("FictionBook")
        val xmlns = ArrayList<Node?>()
        for (item in 0 until fictionBook.length) {
            val map = fictionBook.item(item).attributes
            for (index in 0 until map.length) {
                val node = map.item(index)
                xmlns.add(node)
            }
        }
        setXmlns(xmlns)
    }

    val authors: ArrayList<Person>
        get() = description!!.documentInfo!!.authors
    val body: Body
        get() = getBody(null)
    val notes: Body
        get() = getBody("notes")
    val comments: Body
        get() = getBody("comments")

    private fun getBody(name: String?): Body {
        for (body in bodies) {
            if (name == body.name) {
                return body
            }
        }
        return bodies[0]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FictionBook

        if (!xmlns.contentEquals(other.xmlns)) return false
        if (description != other.description) return false
        if (binaries != other.binaries) return false
        if (encoding != other.encoding) return false

        return true
    }

    override fun hashCode(): Int {
        var result = xmlns.contentHashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + binaries.hashCode()
        result = 31 * result + encoding.hashCode()
        return result
    }

    val title: String
        get() = description!!.titleInfo!!.bookTitle!!
    val lang: String?
        get() = description!!.titleInfo?.lang
    val annotation: Annotation?
        get() = description!!.titleInfo?.annotation

    companion object {
        val FB2_LOGGER: TaggedLogger = Logger.tag("FB2")
    }

}
