/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.`public`.tables.interfaces


import java.io.Serializable
import java.time.OffsetDateTime


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
interface IBook : Serializable {
    val id: Long?
    val path: String
    val name: String
    val date: String?
    val added: OffsetDateTime?
    val sequence: String?
    val sequenceNumber: Long?
    val lang: String?
    val zipFile: String?
    val seqid: Int?
}
