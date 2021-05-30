package com.jarvislin.drugstores.page.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.page.map.MapsActivity
import com.jarvislin.drugstores.page.menu.MenuActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (isGooglePlayServicesAvailable()) {
            checkInAppUpdate()
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                val dialog =
                    googleApiAvailability.getErrorDialog(this, status, REQUEST_PLAY_SERVICE)
                dialog.setOnDismissListener { checkInAppUpdate() }
                dialog.show()
            }
            return false
        }
        return true
    }

    private fun checkInAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_UPDATE
                )
            } else {
                login()
            }
        }

        appUpdateInfoTask.addOnFailureListener { login() }
    }

    private fun login() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { direct() }
    }

    private fun direct() {
        MenuActivity.start(this)
        finish()
    }

    companion object {
        private const val REQUEST_UPDATE = 55
        private const val REQUEST_PLAY_SERVICE = 66
    }
}