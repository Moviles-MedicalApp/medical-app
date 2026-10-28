package pe.com.smart.domain.repository

import pe.com.smart.domain.model.Doctor

interface DoctorRepository {
    suspend fun getDoctors(): Result<List<Doctor>>
    suspend fun getDoctorById(id: Int): Result<Doctor>
    suspend fun createDoctor(
        name: String,
        lastName: String,
        email: String,
        phone: String,
        specialityId: Int
    ): Result<Unit>
    suspend fun updateDoctor(
        id: Int,
        name: String,
        lastName: String,
        email: String,
        phone: String,
        specialityId: Int
    ): Result<Unit>
    suspend fun deleteDoctor(id: Int): Result<Unit>
}
