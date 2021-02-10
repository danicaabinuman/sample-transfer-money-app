package com.unionbankph.corporate.approval.data.model

class Edge(
    var source: Node? = null,
    var destination: Node? = null,
    var topBasement: Node? = null,
    var bottomBasement: Node? = null,
    val pathColor: Int? = null
)
