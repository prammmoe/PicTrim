package com.prammmoe.pictrim.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.picTrimPreferences by preferencesDataStore(name = "pictrim_preferences")
private val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")

@Singleton
class OnboardingPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    val isCompleted: Flow<Boolean> = context.picTrimPreferences.data.map { it[OnboardingCompleted] ?: false }

    suspend fun completeOnboarding() {
        context.picTrimPreferences.edit { it[OnboardingCompleted] = true }
    }
}
