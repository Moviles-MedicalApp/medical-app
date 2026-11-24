package pe.com.smart.data.remote.mapper

import pe.com.smart.data.remote.dto.speciality.SpecialityDto
import pe.com.smart.domain.model.Speciality

fun SpecialityDto.toDomain(): Speciality {
    return Speciality(
        id = id ?: 0,
        name = name.orEmpty(),
        description = description.orEmpty(),
        deleted = deleted ?: false
    )
}
