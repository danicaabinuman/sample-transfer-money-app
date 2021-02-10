package com.unionbankph.corporate.approval.data.model


class Node(val data: Any?, val id: Int) {

    private var position: Vector? = null

    private var size: Size? = null

    val x: Float
        get() = position?.x!!

    val y: Float
        get() = position?.y!!

    val width: Int
        get() = size?.width!!

    val height: Int
        get() = size?.height!!

    fun setPos(pos: Vector) {
        this.position = pos
    }

    fun setSize(width: Int, height: Int) {
        size = Size(width, height)
    }

    override fun toString(): String {
        return "Node{" +
                "pos=" + position +
                ", data=" + data +
                ", size=" + size +
                '}'.toString()
    }

}
