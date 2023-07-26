package com.kursx.parser.fb2.fonts

import kotlinx.serialization.Serializable

@Serializable
class StrikeThrough : Font {
    constructor(strong: String, p: String) : super(strong, p)
}