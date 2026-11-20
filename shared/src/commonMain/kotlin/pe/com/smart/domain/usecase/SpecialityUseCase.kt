package pe.com.smart.domain.usecase

import pe.com.smart.domain.repository.SpecialityRepository

class SpecialityUseCase(private val repository: SpecialityRepository) {
    suspend fun getSpecialities() = repository.getSpecialities()
    suspend fun getSpecialityById(id: Int) = repository.getSpecialityById(id)
    suspend fun createSpeciality(name: String, description: String, deleted: Boolean = false) = repository.createSpeciality(name, description, deleted)
    suspend fun updateSpeciality(id: Int, name: String, description: String, deleted: Boolean) = repository.updateSpeciality(id, name, description, deleted)
    suspend fun deleteSpeciality(id: Int) = repository.deleteSpeciality(id)
}
