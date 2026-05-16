package com.mascotasperdidas.app.app.di

import com.mascotasperdidas.app.domain.port.`in`.CreateReport
import com.mascotasperdidas.app.domain.port.`in`.DeleteAccount
import com.mascotasperdidas.app.domain.port.`in`.DeleteReport
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import com.mascotasperdidas.app.domain.port.`in`.RequestPhoneOtp
import com.mascotasperdidas.app.domain.port.`in`.SearchReports
import com.mascotasperdidas.app.domain.port.`in`.SignInWithGoogle
import com.mascotasperdidas.app.domain.port.`in`.SignOut
import com.mascotasperdidas.app.domain.port.`in`.UpdateNotificationPrefs
import com.mascotasperdidas.app.domain.port.`in`.UpdateUserProfile
import com.mascotasperdidas.app.domain.port.`in`.VerifyPhoneOtp
import com.mascotasperdidas.app.domain.usecase.CreateReportImpl
import com.mascotasperdidas.app.domain.usecase.DeleteAccountImpl
import com.mascotasperdidas.app.domain.usecase.DeleteReportImpl
import com.mascotasperdidas.app.domain.usecase.ObserveCurrentUserImpl
import com.mascotasperdidas.app.domain.usecase.ObserveReportsImpl
import com.mascotasperdidas.app.domain.usecase.RequestPhoneOtpImpl
import com.mascotasperdidas.app.domain.usecase.SearchReportsImpl
import com.mascotasperdidas.app.domain.usecase.SignInWithGoogleImpl
import com.mascotasperdidas.app.domain.usecase.SignOutImpl
import com.mascotasperdidas.app.domain.usecase.UpdateNotificationPrefsImpl
import com.mascotasperdidas.app.domain.usecase.UpdateUserProfileImpl
import com.mascotasperdidas.app.domain.usecase.VerifyPhoneOtpImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindSignInWithGoogle(impl: SignInWithGoogleImpl): SignInWithGoogle

    @Binds
    abstract fun bindRequestPhoneOtp(impl: RequestPhoneOtpImpl): RequestPhoneOtp

    @Binds
    abstract fun bindVerifyPhoneOtp(impl: VerifyPhoneOtpImpl): VerifyPhoneOtp

    @Binds
    abstract fun bindObserveCurrentUser(impl: ObserveCurrentUserImpl): ObserveCurrentUser

    @Binds
    abstract fun bindUpdateUserProfile(impl: UpdateUserProfileImpl): UpdateUserProfile

    @Binds
    abstract fun bindUpdateNotificationPrefs(impl: UpdateNotificationPrefsImpl): UpdateNotificationPrefs

    @Binds
    abstract fun bindSignOut(impl: SignOutImpl): SignOut

    @Binds
    abstract fun bindDeleteAccount(impl: DeleteAccountImpl): DeleteAccount

    @Binds
    abstract fun bindObserveReports(impl: ObserveReportsImpl): ObserveReports

    @Binds
    abstract fun bindCreateReport(impl: CreateReportImpl): CreateReport

    @Binds
    abstract fun bindSearchReports(impl: SearchReportsImpl): SearchReports

    @Binds
    abstract fun bindDeleteReport(impl: DeleteReportImpl): DeleteReport
}
