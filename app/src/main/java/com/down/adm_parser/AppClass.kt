package com.down.adm_parser

import android.app.Application
import com.down.adm_parser.interview.InterviewModel
import com.down.adm_parser.interview.data.GetDataImpl
import com.down.adm_parser.interview.domain.GetData
import com.down.adm_parser.interview.persistence.GetLocalStudents
import com.down.adm_parser.interview.persistence.GetLocalStudentsImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(
                modules = module {
                    singleOf(::GetDataImpl) { bind<GetData>() }
                    singleOf(::GetLocalStudentsImpl) { bind<GetLocalStudents>() }
                    viewModelOf(::InterviewModel)
                }
            )
        }
    }
}