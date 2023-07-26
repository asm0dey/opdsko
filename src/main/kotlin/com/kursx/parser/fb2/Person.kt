package com.kursx.parser.fb2

import kotlinx.serialization.Serializable
import org.w3c.dom.Node

//http://www.fictionbook.org/index.php/Элемент_author
@Suppress("unused")
@Serializable
open class Person {
    @JvmField
    var id: String? = null
    var firstName: String? = null
    var middleName: String? = null
    var lastName: String? = null
    var nickname: String? = null
    protected var homePages: ArrayList<String> = arrayListOf()
    protected var emails: ArrayList<String> = arrayListOf()

    constructor()
    internal constructor(node: Node) {
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val author = nodeList.item(i)
            when (author.nodeName) {
                "id" -> id = author.textContent
                "home-page" -> {
                    homePages.add(author.textContent)
                }

                "email" -> {
                    if (emails == null) emails = ArrayList()
                    emails!!.add(author.textContent)
                }

                "nickname" -> nickname = author.textContent
                "first-name" -> firstName = author.textContent
                "middle-name" -> middleName = author.textContent
                "last-name" -> lastName = author.textContent
            }
        }
    }


    val fullName: String
        get() = ((if (firstName == null) "" else "$firstName ")
                + (if (middleName == null) "" else "$middleName ")
                + if (lastName == null) "" else lastName)


    override fun toString(): String {
        return "Person{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", homePages=" + homePages +
                ", emails=" + emails +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (middleName != other.middleName) return false
        if (lastName != other.lastName) return false
        if (nickname != other.nickname) return false
        if (homePages != other.homePages) return false
        if (emails != other.emails) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (middleName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + homePages.hashCode()
        result = 31 * result + emails.hashCode()
        return result
    }

}
