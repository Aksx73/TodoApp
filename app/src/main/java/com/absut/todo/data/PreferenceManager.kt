package com.absut.todo.data

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder { BY_NAME, BY_DATE }

/**
* To get sort preference and hide complete pref together
* */
data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    val preferencesFlow: Flow<FilterPreferences> = dataStore.data
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(preferences[SORT_ORDER] ?: SortOrder.BY_DATE.name)
            val hideCompleted = preferences[HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emptyPreferences()
            } else {
                throw exception
            }
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIDE_COMPLETED] = hideCompleted
        }
    }

    companion object {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")
    }
}