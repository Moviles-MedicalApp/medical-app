package pe.com.smart.domain.repository

import pe.com.smart.domain.model.Speciality

interface SpecialityRepository {

    suspend fun getSpecialities(): Result<List<Speciality>>

    suspend fun getSpecialityById(
        id: Int
    ): Result<Speciality>

    suspend fun createSpeciality(
        name: String,
        description: String,
        deleted: Boolean = false
    ): Result<Unit>

    suspend fun updateSpeciality(
        id: Int,
        name: String,
        description: String,
        deleted: Boolean
    ): Result<Unit>

    suspend fun deleteSpeciality(
        id: Int
    ): Result<Unit>
}
