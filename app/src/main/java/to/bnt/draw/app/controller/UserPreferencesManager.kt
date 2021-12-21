package to.bnt.draw.app.controller

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import to.bnt.draw.app.data.SettingsStore
import java.io.IOException

class UserPreferencesManager(private val context: Context) {
    suspend fun saveToDataStore(settingsStore: SettingsStore) {
        context.dataStore.edit { preferences ->
            if (settingsStore.token == null) {
                preferences.remove(TOKEN)
            } else {
                preferences[TOKEN] = settingsStore.token
            }
        }
    }

    suspend fun cleanUserPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN)
        }
    }

    fun getPreferencesFromDataStore() = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences -> SettingsStore(token = preferences[TOKEN]) }

    private companion object {
        private const val USER_PREFERENCES = "user_preferences"
        private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES)
        private val TOKEN = stringPreferencesKey("token")
    }
}
