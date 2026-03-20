package org.delcom.module

import io.ktor.server.application.*
import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.ReviewService
import org.delcom.services.UserService
import org.delcom.services.WisataService
import org.koin.dsl.module

fun appModule(application: Application) = module {
    val baseUrl = application.environment.config
        .property("ktor.app.baseUrl")
        .getString()
        .trimEnd('/')

    val jwtSecret = application.environment.config
        .property("ktor.jwt.secret")
        .getString()

    // User Repository
    single<IUserRepository> {
        UserRepository(baseUrl)
    }

    // Refresh Token Repository
    single<IRefreshTokenRepository> {
        RefreshTokenRepository()
    }

    // Wisata Repository
    single<IWisataRepository> {
        WisataRepository(baseUrl)
    }

    // Review Repository
    single<IReviewRepository> {
        ReviewRepository()
    }

    // Auth Service
    single {
        AuthService(jwtSecret, get(), get())
    }

    // User Service
    single {
        UserService(get(), get())
    }

    // Wisata Service
    single {
        WisataService(get(), get(), get())
    }

    // Review Service
    single {
        ReviewService(get(), get(), get())
    }
}