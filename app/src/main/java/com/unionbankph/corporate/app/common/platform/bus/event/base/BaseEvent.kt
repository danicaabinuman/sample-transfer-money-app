package com.unionbankph.corporate.app.common.platform.bus.event.base

open class BaseEvent<T> {

    val eventType: String
    val payload: T?

    constructor(eventType: String, payload: T) {
        this.eventType = eventType
        this.payload = payload
    }

    constructor(eventType: String) {
        this.eventType = eventType
        payload = null
    }
}
