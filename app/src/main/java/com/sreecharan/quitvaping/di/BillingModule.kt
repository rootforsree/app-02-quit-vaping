package com.sreecharan.quitvaping.di

import com.sreecharan.quitvaping.data.billing.BillingRepository
import com.sreecharan.quitvaping.data.billing.BillingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository
}
