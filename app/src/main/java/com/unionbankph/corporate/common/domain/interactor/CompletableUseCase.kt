package com.unionbankph.corporate.common.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers

/**
 * Abstract class for a UseCase that returns an instance of a [Completable].
 */
abstract class CompletableUseCase<in Params> protected constructor(
    private val threadExecutor: ThreadExecutor,
    private val postExecutionThread: PostExecutionThread
) {

    private val subscription = Disposables.empty()

    /**
     * Builds a [Completable] which will be used when the current [CompletableUseCase] is executed.
     */
    protected abstract fun buildUseCaseObservable(params: Params?): Completable

    /**
     * Executes the current use case.
     */
    fun execute(
        params: Params? = null,
        doOnSubscribeEvent: (() -> Unit)? = null,
        doFinallyEvent: (() -> Unit)? = null
    ): Completable {
        return this.buildUseCaseObservable(params)
            .subscribeOn(Schedulers.from(threadExecutor))
            .observeOn(postExecutionThread.scheduler)
            .doOnSubscribe { doOnSubscribeEvent?.let { doOnSubscribeEvent() } }
            .doFinally { doFinallyEvent?.let { doFinallyEvent() } }
    }

    /**
     * Unsubscribes from current [Disposable].
     */
    fun unsubscribe() {
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }
}
