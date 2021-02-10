package com.unionbankph.corporate.common.domain.provider

import io.reactivex.Scheduler

interface SchedulerProvider {
    fun ui(): Scheduler
    fun io(): Scheduler
    fun newThread(): Scheduler
    fun computation(): Scheduler
}
