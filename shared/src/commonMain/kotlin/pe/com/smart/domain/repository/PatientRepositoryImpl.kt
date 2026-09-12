package pe.com.smart.data.repository

import pe.com.smart.data.mapper.toDomain
import pe.com.smart.data.remote.patient.PatientApiService
import pe.com.smart.data.remote.patient.dto.PatientRequestDto
import pe.com.smart.domain.model.Patient
import pe.com.smart.domain.repository.PatientRepository

class PatientRepositoryImpl(
    private val apiService: PatientApiService
) : PatientRepository {

    override suspend fun getPatients():
            Result<List<Patient>> {

        return try {

            val patients =
                apiService
                    .getPatients()
                    .map {
                        it.toDomain()
                    }

            Result.success(patients)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun getPatientById(
        id: Int
    ): Result<Patient> {

        return try {

            val patient =
                apiService
                    .getPatientById(id)
                    .toDomain()

            Result.success(patient)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun createPatient(
        name: String,
        lastName: String,
        dni: String,
        email: String,
        phone: String
    ): Result<Unit> {

        return try {

            apiService.createPatient(
                PatientRequestDto(
                    name = name,
                    lastName = lastName,
                    dni = dni,
                    email = email,
                    phone = phone
                )
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun updatePatient(
        id: Int,
        name: String,
        lastName: String,
        dni: String,
        email: String,
        phone: String
    ): Result<Unit> {

        return try {

            apiService.updatePatient(
                id = id,
                request = PatientRequestDto(
                    name = name,
                    lastName = lastName,
                    dni = dni,
                    email = email,
                    phone = phone
                )
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun deletePatient(
        id: Int
    ): Result<Unit> {

        return try {

            apiService.deletePatient(id)

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}