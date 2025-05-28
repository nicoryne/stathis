package citu.edu.stathis.mobile.features.vitals.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import kotlinx.coroutines.flow.Flow

interface VitalsRepository {
    suspend fun saveVitals(vitalSigns: VitalSigns): ClientResponse<Unit>
    fun getVitalsHistory(userId: String): Flow<ClientResponse<List<VitalSigns>>>
    suspend fun deleteVitalRecord(recordId: String): ClientResponse<Unit>
    // `getLatestVitalSigns` will now primarily come from Health Connect via a use case.
}