package com.moneybol.app.providers

import com.moneybol.app.core.model.ProviderCategory
import com.moneybol.app.core.model.ProviderInfo
import com.moneybol.app.core.model.ProviderStatus
import com.moneybol.app.providers.banks.GenericBankParser
import com.moneybol.app.providers.esewa.EsewaParser
import com.moneybol.app.providers.fonepay.FonepayParser
import com.moneybol.app.providers.khalti.KhaltiParser
import com.moneybol.app.providers.nepalpay.NepalPayParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/**
 * Registry of all known payment providers in Nepal.
 *
 * IMPORTANT: ProviderStatus reflects verification state.
 * All providers ship as FORMAT_UNVERIFIED in V1 until tested
 * with real sanitized notification samples.
 */
object ProviderRegistry {

    val providers = listOf(
        // ── Wallets ──
        ProviderInfo(
            id = "esewa",
            displayName = "eSewa",
            packageNames = listOf("com.f1soft.esewa"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.WALLET,
        ),
        ProviderInfo(
            id = "khalti",
            displayName = "Khalti",
            packageNames = listOf("com.khalti"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.WALLET,
        ),

        // ── Payment Networks ──
        ProviderInfo(
            id = "fonepay",
            displayName = "Fonepay",
            packageNames = listOf("com.fonepay.merchant"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.PAYMENT_NETWORK,
        ),
        ProviderInfo(
            id = "nepalpay",
            displayName = "NepalPay",
            packageNames = listOf("com.nchl.creditor.nchl_nps_creditor_app"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.PAYMENT_NETWORK,
        ),

        // ── Banks (extensible registry) ──
        ProviderInfo(
            id = "nabil",
            displayName = "Nabil Bank",
            packageNames = listOf("com.nabilbank.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "nic_asia",
            displayName = "NIC ASIA",
            packageNames = listOf("com.nmbmobilebanking.nicasia"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "global_ime",
            displayName = "Global IME Bank",
            packageNames = listOf("com.gibl.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "himalayan",
            displayName = "Himalayan Bank",
            packageNames = listOf("com.hbl.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "siddhartha",
            displayName = "Siddhartha Bank",
            packageNames = listOf("com.siddharthabank.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "nepal_bank",
            displayName = "Nepal Bank",
            packageNames = listOf("com.nepalbank.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "nmb",
            displayName = "NMB Bank",
            packageNames = listOf("com.nmb.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "prabhu",
            displayName = "Prabhu Bank",
            packageNames = listOf("com.prabhubank.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
        ProviderInfo(
            id = "machhapuchchhre",
            displayName = "Machhapuchchhre Bank",
            packageNames = listOf("com.mbl.mobilebanking"),
            status = ProviderStatus.FORMAT_UNVERIFIED,
            category = ProviderCategory.BANK,
        ),
    )
}

/**
 * Hilt module providing parser instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @IntoSet
    @Singleton
    fun provideEsewaParser(): PaymentNotificationParser = EsewaParser()

    @Provides
    @IntoSet
    @Singleton
    fun provideKhaltiParser(): PaymentNotificationParser = KhaltiParser()

    @Provides
    @IntoSet
    @Singleton
    fun provideFonepayParser(): PaymentNotificationParser = FonepayParser()

    @Provides
    @IntoSet
    @Singleton
    fun provideNepalPayParser(): PaymentNotificationParser = NepalPayParser()

    @Provides
    @IntoSet
    @Singleton
    fun provideGenericBankParser(): PaymentNotificationParser = GenericBankParser()
}
