package pe.com.smart.domain.usecase

import pe.com.smart.domain.repository.PatientRepository

class PatientUseCase(private val repository: PatientRepository) {
    suspend fun getPatients() = repository.getPatients()
    suspend fun getPatientById(id: Int) = repository.getPatientById(id)
    suspend fun createPatient(name: String, lastName: String, dni: String, email: String, phone: String) = repository.createPatient(name, lastName, dni, email, phone)
    suspend fun updatePatient(id: Int, name: String, lastName: String, dni: String, email: String, phone: String) = repository.updatePatient(id, name, lastName, dni, email, phone)
    suspend fun deletePatient(id: Int) = repository.deletePatient(id)
}
