package com.unionbankph.corporate.app.common.platform.bus.event.base

import com.unionbankph.corporate.app.common.platform.bus.base.BaseBus

open class BaseEventBus<T> : BaseBus<BaseEvent<T>>()
