package com.unionbankph.corporate.common.domain.interactor

import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

/**
 * Abstract class for a UseCase that returns an instance of a [Single].
 */
abstract class SingleUseCase<T, in Params> constructor(
    private val threadExecutor: ThreadExecutor,
    private val postExecutionThread: PostExecutionThread
) {

    /**
     * Builds a [Single] which will be used when the current [SingleUseCase] is executed.
     */
    protected abstract fun buildUseCaseObservable(params: Params? = null): Single<T>

    /**
     * Executes the current use case.
     */
    open fun execute(
        singleObserver: DisposableSingleObserver<T>,
        doOnSubscribeEvent: (() -> Unit)? = null,
        doFinallyEvent: (() -> Unit)? = null,
        params: Params? = null
    ): DisposableSingleObserver<T> {
        val single = buildUseCaseObservable(params)
            .subscribeOn(Schedulers.from(threadExecutor))
            .observeOn(postExecutionThread.scheduler)
            .doOnSubscribe { doOnSubscribeEvent?.invoke() }
            .doFinally { doFinallyEvent?.invoke() } as Single<T>
        return single.subscribeWith(singleObserver)
    }
}
