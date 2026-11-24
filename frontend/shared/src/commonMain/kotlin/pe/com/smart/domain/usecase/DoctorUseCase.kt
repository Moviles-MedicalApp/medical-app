package pe.com.smart.domain.usecase

import pe.com.smart.domain.repository.DoctorRepository

class DoctorUseCase(private val repository: DoctorRepository) {
    suspend fun getDoctors() = repository.getDoctors()
    suspend fun getDoctorById(id: Int) = repository.getDoctorById(id)
    suspend fun createDoctor(name: String, lastName: String, email: String, phone: String, specialityId: Int) = repository.createDoctor(name, lastName, email, phone, specialityId)
    suspend fun updateDoctor(id: Int, name: String, lastName: String, email: String, phone: String, specialityId: Int) = repository.updateDoctor(id, name, lastName, email, phone, specialityId)
    suspend fun deleteDoctor(id: Int) = repository.deleteDoctor(id)
}
