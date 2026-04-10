package dev.mudrock.tiviyomitvlauncher.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.mudrock.tiviyomitvlauncher.data.DatabaseContainer
import dev.mudrock.tiviyomitvlauncher.data.executeAsListFlow
import dev.mudrock.tiviyomitvlauncher.data.resolver.InputResolver
import dev.mudrock.tiviyomitvlauncher.data.sqldelight.Input
import timber.log.Timber

class InputRepository(
    private val context: Context,
    private val inputResolver: InputResolver,
    private val database: DatabaseContainer
) {
    private suspend fun commitInputs(inputs: Collection<Input>) = withContext(Dispatchers.IO) {
        Timber.d("Committing ${inputs.size} inputs")
        // Remove inputs found in database but not in committed list
        val existingIds = database.inputs.getAll().executeAsList().map { it.id }
        val newIds = inputs.map { it.id }.toSet()
        val idsToRemove = existingIds.subtract(newIds)

        idsToRemove.forEach { id ->
            database.inputs.removeById(id)
        }

        // Upsert inputs
        inputs.forEach { input ->
            commitInput(input)
        }
    }

    private fun commitInput(input: Input) {
        database.inputs.upsert(
            id = input.id,
            inputId = input.id,
            displayName = input.displayName,
            packageName = input.packageName,
            type = input.type,
            switchIntentUri = input.switchIntentUri
        )
    }

    suspend fun refreshAllInputs() {
        Timber.d("Refreshing all inputs")
        val inputs = inputResolver.getInputs(context)
        commitInputs(inputs)
        Timber.d("Inputs refreshed: ${inputs.size} inputs")
    }

    fun getInputs() = database.inputs.getAll().executeAsListFlow()
}
