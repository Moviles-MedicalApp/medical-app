package pe.com.smart.domain.repository

import pe.com.smart.domain.model.Patient

interface PatientRepository {

    suspend fun getPatients():
            Result<List<Patient>>

    suspend fun getPatientById(
        id: Int
    ): Result<Patient>

    suspend fun createPatient(
        name: String,
        lastName: String,
        dni: String,
        email: String,
        phone: String
    ): Result<Unit>

    suspend fun updatePatient(
        id: Int,
        name: String,
        lastName: String,
        dni: String,
        email: String,
        phone: String
    ): Result<Unit>

    suspend fun deletePatient(
        id: Int
    ): Result<Unit>
}