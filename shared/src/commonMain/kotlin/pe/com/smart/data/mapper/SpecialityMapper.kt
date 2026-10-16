package pe.com.smart.data.mapper

import pe.com.smart.data.remote.speciality.dto.SpecialityDto
import pe.com.smart.domain.model.Speciality

fun SpecialityDto.toDomain(): Speciality {
    return Speciality(
        id = id ?: 0,
        name = name.orEmpty(),
        description = description.orEmpty(),
        active = active ?: true
    )
}
