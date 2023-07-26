package com.kursx.parser.fb2.fonts

import kotlinx.serialization.Serializable

@Serializable
class Emphasis : Font {
    constructor(emphasis: String, p: String) : super(emphasis, p)
}
