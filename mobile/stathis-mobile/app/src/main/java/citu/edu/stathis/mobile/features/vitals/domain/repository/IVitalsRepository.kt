package citu.edu.stathis.mobile.features.vitals.domain.repository

import citu.edu.stathis.mobile.features.vitals.domain.model.VitalSigns
import kotlinx.coroutines.flow.Flow

interface IVitalsRepository {
    suspend fun saveVitalSigns(vitalSigns: VitalSigns): Result<Unit>
    suspend fun getVitalSignsHistory(userId: String): Flow<List<VitalSigns>>
    suspend fun getLatestVitalSigns(userId: String): Flow<VitalSigns?>
    suspend fun deleteVitalSigns(id: String): Result<Unit>
}