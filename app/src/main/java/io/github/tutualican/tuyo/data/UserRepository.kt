package io.github.tutualican.tuyo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.tutualican.tuyo.domain.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tuyo_user_v1",
)

/**
 * A single durable JSON document is enough for this dataset and keeps the
 * migration to Room mechanical later. DataStore serializes writes, so two quick
 * reviews cannot clobber each other.
 */
class UserRepository(private val context: Context) {

    val state: Flow<UserData> = context.userDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> decode(preferences[StateKey]) }

    suspend fun read(): UserData = state.first()

    suspend fun update(transform: (UserData) -> UserData) {
        context.userDataStore.edit { preferences ->
            val current = decode(preferences[StateKey])
            preferences[StateKey] = json.encodeToString(UserData.serializer(), transform(current))
        }
    }

    suspend fun clear() {
        context.userDataStore.edit { preferences -> preferences.remove(StateKey) }
    }

    companion object {
        private val StateKey = stringPreferencesKey("state_json")

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = false
        }

        fun decode(raw: String?): UserData {
            if (raw.isNullOrBlank()) return UserData()
            return runCatching { json.decodeFromString(UserData.serializer(), raw) }
                .getOrElse { UserData() }
        }
    }
}
