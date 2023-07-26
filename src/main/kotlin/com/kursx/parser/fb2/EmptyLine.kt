package com.kursx.parser.fb2

class EmptyLine : Element() {
    init {
        text = ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

}
