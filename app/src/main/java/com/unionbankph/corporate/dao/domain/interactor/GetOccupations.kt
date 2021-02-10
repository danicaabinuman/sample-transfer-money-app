package com.unionbankph.corporate.dao.domain.interactor

import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class GetOccupations
@Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val responseProvider: ResponseProvider,
    private val daoGateway: DaoGateway
) : SingleUseCase<PagedDto<Selector>, Pageable?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Pageable?): Single<PagedDto<Selector>> {
        return daoGateway.getAccessToken()
            .zipWith(daoGateway.getReferenceNumber())
            .flatMap {
                params?.let { pageable ->
                    daoGateway.getOccupations(
                        it.first.nullable(),
                        it.second.nullable(),
                        pageable
                    )
                }
            }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .map { pagedDto ->
                val mapContent = pagedDto.results.map {
                    val selector = Selector()
                    selector.id = it.finacleCode
                    selector.value = it.name
                    selector
                }.toMutableList()
                mapPagedDto(mapContent, pagedDto)
            }
    }

    private fun mapPagedDto(
        mapContent: MutableList<Selector>,
        pagedDto: PagedDto<*>
    ): PagedDto<Selector> {
        return PagedDto<Selector>().apply {
            results = mapContent
            hasNextPage = pagedDto.hasNextPage
            totalElements = pagedDto.totalElements
            currentPage = pagedDto.currentPage
            pageSize = pagedDto.pageSize
            totalPages = pagedDto.totalPages
        }
    }
}
