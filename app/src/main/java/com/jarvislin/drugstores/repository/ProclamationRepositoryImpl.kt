package com.jarvislin.drugstores.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.domain.repository.ProclamationRepository
import com.jarvislin.drugstores.BuildConfig
import com.jarvislin.drugstores.data.LocalData
import com.jarvislin.drugstores.data.model.RemoteConfig
import com.jarvislin.drugstores.extension.toObject
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

class ProclamationRepositoryImpl(
    private val localData: LocalData,
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
) : ProclamationRepository {

    override fun fetchProclamation(): Maybe<List<Proclamation>> {
        FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 120 else 21600) // 6 hrs
            .build()
            .let { remoteConfig.setConfigSettingsAsync(it) }

        return Maybe.create<List<Proclamation>> { emitter ->
            remoteConfig.fetchAndActivate()
                .addOnSuccessListener {
                    try {
                        remoteConfig.getString("proclamations")
                            .toObject(RemoteConfig::class.java)
                            .proclamations
                            .map { it.toProclamation() }
                            .filter { it.isValid() }
                            .sortedByDescending { it.createdAt }
                            .let { emitter.onSuccess(it) }
                    } catch (ex: Exception) {
                        emitter.onError(ex)
                    }
                }
                .addOnFailureListener { emitter.onError(it) }
                .addOnCanceledListener { emitter.onComplete() }
        }.subscribeOn(Schedulers.io())
    }

    override fun hasNewProclamation(proclamations: List<Proclamation>): Boolean {
        return localData.proclamations != proclamations
    }

    override fun saveProclamations(json: String) {
        localData.proclamationsText = json
    }
}