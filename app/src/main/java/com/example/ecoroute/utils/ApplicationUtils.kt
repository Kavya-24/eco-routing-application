package com.example.ecoroute.utils


import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
//import com.google.firebase.FirebaseApp

@Keep
class ApplicationUtils : Application() {

    companion object {

        lateinit var instance: ApplicationUtils


        fun getApplication(): ApplicationUtils {
            return instance
        }

        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()


        instance = this

        //Firebase init also
//        FirebaseApp.initializeApp(getContext())


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)


    }
}