package com.unionbankph.corporate.di

import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.common.BaseTest
import dagger.BindsInstance
import dagger.Component

@PerApplication
@Component(
    modules = [
        NetworkModuleTest::class
    ]
)
interface AppComponentTest {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun baseTest(baseTest: BaseTest): Builder

        fun networkModuleTest(networkModuleTest: NetworkModuleTest): Builder

        fun build(): AppComponentTest
    }

    fun inject(test: BaseTest)
}
