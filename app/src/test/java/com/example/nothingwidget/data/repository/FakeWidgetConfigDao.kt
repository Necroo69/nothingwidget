package com.example.nothingwidget.data.repository

import com.example.nothingwidget.data.local.WidgetConfigDao
import com.example.nothingwidget.data.local.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [WidgetConfigDao] for pure JVM unit tests — no Room, no device.
 * Backed by a [MutableStateFlow] so flow-based queries emit on every write.
 */
class FakeWidgetConfigDao : WidgetConfigDao {
    private val store = MutableStateFlow<List<WidgetConfigEntity>>(emptyList())

    override fun getAllWidgetConfigs(): Flow<List<WidgetConfigEntity>> = store

    override suspend fun getWidgetConfigById(id: String): WidgetConfigEntity? =
        store.value.find { it.id == id }

    override fun getPinnedWidgetConfigs(): Flow<List<WidgetConfigEntity>> =
        store.map { list -> list.filter { it.isPinned } }

    override suspend fun insertOrUpdateWidgetConfig(config: WidgetConfigEntity) {
        store.value = store.value.filterNot { it.id == config.id } + config
    }

    override suspend fun deleteWidgetConfig(id: String) {
        store.value = store.value.filterNot { it.id == id }
    }
}
