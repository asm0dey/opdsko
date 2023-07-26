package com.kursx.parser.fb2.fonts

import kotlinx.serialization.Serializable

@Serializable
open class Font(
    val startIndex: Int,
    val finishIndex: Int
) {
    constructor(emphasis: String, p: String) : this(
        p.indexOf(emphasis),
        p.indexOf(emphasis) + emphasis.length
    )


}
