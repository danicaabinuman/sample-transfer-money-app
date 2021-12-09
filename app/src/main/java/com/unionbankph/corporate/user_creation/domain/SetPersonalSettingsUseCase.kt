package com.unionbankph.corporate.user_creation.domain

import com.unionbankph.corporate.app.common.extension.flatMapCompletableIf
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.interactor.SingleUseCase
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.notification.data.form.NotificationForm
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.payment_link.data.gateway.PaymentLinkGateway
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.user_creation.data.param.PersonalizeSettings
import io.reactivex.Single
import javax.inject.Inject

class SetPersonalSettingsUseCase
@Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val notificationGateway: NotificationGateway,
    private val settingsGateway: SettingsGateway,
    private val responseProvider: ResponseProvider
) : SingleUseCase<Boolean, PersonalizeSettings>(threadExecutor, postExecutionThread){

    override fun buildUseCaseObservable(params: PersonalizeSettings?): Single<Boolean> {

        return notificationGateway.getNotifications()
            .flatMap {
                notificationGateway.updateNotificationSettings(
                    NotificationForm(
                        it.notifications?.sortedBy { it.notificationId }?.toMutableList() ?: mutableListOf(),
                        params?.notification)
                )
            }.flatMapCompletable { settingsGateway.considerAsRecentUser(params?.promptTypeEnum!!) }
            .toSingle{
                true
            }.flatMapCompletableIf(
                {params?.totp == true},
                { settingsGateway.totpSubscribe(ManageDeviceForm(""))
                    /*.startWith { settingsGateway.considerAsRecentUser(params?.promptTypeEnum!!) }*/}
            ).toSingle {
                true
            }
    }
}