package com.mevastyle.app
import android.app.Application
import com.google.firebase.FirebaseApp
import com.mevastyle.app.data.GarmentManager

class MevaStyleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        GarmentManager.init(this)
    }
}
