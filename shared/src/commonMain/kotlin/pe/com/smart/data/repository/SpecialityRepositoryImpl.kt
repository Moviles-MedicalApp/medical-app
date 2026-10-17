package pe.com.smart.data.repository

import pe.com.smart.core.network.NetworkException
import pe.com.smart.data.mapper.toDomain
import pe.com.smart.data.remote.speciality.SpecialityApiService
import pe.com.smart.data.remote.speciality.dto.SpecialityRequestDto
import pe.com.smart.domain.model.Speciality
import pe.com.smart.domain.repository.SpecialityRepository

class SpecialityRepositoryImpl(
    private val apiService: SpecialityApiService
) : SpecialityRepository {

    override suspend fun getSpecialities(): Result<List<Speciality>> {
        return try {
            val specialities = apiService
                .getSpecialities()
                .map { it.toDomain() }

            Result.success(specialities)
        } catch (e: Exception) {
            /*
             * Si el backend aún no ha publicado el endpoint (404),
             * devolvemos una lista vacía o lista inicial de desarrollo
             * para evitar que la UI quede bloqueada con error.
             */
            if (e is NetworkException && e.statusCode == 404) {
                Result.success(
                    listOf(
                        Speciality(1, "Medicina General", "Atención médica primaria y preventiva", true),
                        Speciality(2, "Pediatría", "Atención médica integral para niños y adolescentes", true),
                        Speciality(3, "Cardiología", "Diagnóstico y tratamiento de enfermedades del corazón", true),
                        Speciality(4, "Dermatología", "Especialidad encargada de la piel y sus anexos", true)
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getSpecialityById(
        id: Int
    ): Result<Speciality> {
        return try {
            val speciality = apiService
                .getSpecialityById(id)
                .toDomain()

            Result.success(speciality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createSpeciality(
        name: String,
        description: String,
        deleted: Boolean
    ): Result<Unit> {
        return try {
            apiService.createSpeciality(
                SpecialityRequestDto(
                    name = name,
                    description = description,
                    deleted = deleted
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSpeciality(
        id: Int,
        name: String,
        description: String,
        deleted: Boolean
    ): Result<Unit> {
        return try {
            apiService.updateSpeciality(
                id = id,
                request = SpecialityRequestDto(
                    name = name,
                    description = description,
                    deleted = deleted
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSpeciality(
        id: Int
    ): Result<Unit> {
        return try {
            apiService.deleteSpeciality(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
