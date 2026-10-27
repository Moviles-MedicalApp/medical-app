package pe.com.smart.domain.repository

import pe.com.smart.data.mapper.toDomain
import pe.com.smart.data.remote.doctor.DoctorApiService
import pe.com.smart.data.remote.doctor.dt.DoctorRequestDto
import pe.com.smart.domain.model.Doctor

class DoctorRepositoryImpl (
    private val apiService: DoctorApiService
) : DoctorRepository {

    override suspend fun getDoctors(): Result<List<Doctor>> =
        try {
            val doctors = apiService
                .getDoctors()
                .map { it.toDomain() }

            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getDoctorById(
        id: Int
    ): Result<Doctor> =
        try {
            val doctor = apiService
                .getDoctorById(id)
                .toDomain()

            Result.success(doctor)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun createDoctor(
        name: String,
        lastName: String,
        email: String,
        phone: String,
        specialityId: Int
    ): Result<Unit> =
        try {
            apiService.createDoctor(
                DoctorRequestDto(
                    name = name,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    specialityId = specialityId
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun updateDoctor(
        id: Int,
        name: String,
        lastName: String,
        email: String,
        phone: String,
        specialityId: Int
    ): Result<Unit> =
        try {
            apiService.updateDoctor(
                id = id,
                request = DoctorRequestDto(
                    name = name,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    specialityId = specialityId
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteDoctor(
        id: Int
    ): Result<Unit> =
        try {
            apiService.deleteDoctor(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}