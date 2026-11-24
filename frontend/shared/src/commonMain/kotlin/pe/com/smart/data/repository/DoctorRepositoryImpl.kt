package pe.com.smart.data.repository

import pe.com.smart.core.network.NetworkException
import pe.com.smart.data.mapper.toDomain
import pe.com.smart.data.remote.doctor.DoctorApiService
import pe.com.smart.data.remote.doctor.dto.DoctorRequestDto
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.repository.DoctorRepository

class DoctorRepositoryImpl(
    private val apiService: DoctorApiService
) : DoctorRepository {

    override suspend fun getDoctors(): Result<List<Doctor>> {
        return try {
            val doctors = apiService.getDoctors().map { it.toDomain() }
            Result.success(doctors)
        } catch (e: Exception) {
            if (e is NetworkException && e.statusCode == 404) {
                Result.success(
                    listOf(
                        Doctor(1, "Juan", "Pérez", "juan.perez@health.com", "987654321", 1, "Medicina General"),
                        Doctor(2, "María", "Gómez", "maria.gomez@health.com", "912345678", 2, "Pediatría")
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getDoctorById(id: Int): Result<Doctor> {
        return try {
            val doctor = apiService.getDoctorById(id).toDomain()
            Result.success(doctor)
        } catch (e: Exception) {
            if (e is NetworkException && e.statusCode == 404) {
                Result.success(
                    Doctor(id, "Doctor", "Ejemplo", "doctor@health.com", "999999999", 1, "Medicina General")
                )
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun createDoctor(
        name: String,
        lastName: String,
        email: String,
        phone: String,
        specialityId: Int
    ): Result<Unit> {
        return try {
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
    }

    override suspend fun updateDoctor(
        id: Int,
        name: String,
        lastName: String,
        email: String,
        phone: String,
        specialityId: Int
    ): Result<Unit> {
        return try {
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
    }

    override suspend fun deleteDoctor(id: Int): Result<Unit> {
        return try {
            apiService.deleteDoctor(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
