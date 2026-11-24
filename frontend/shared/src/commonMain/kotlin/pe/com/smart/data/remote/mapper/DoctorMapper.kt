package pe.com.smart.data.remote.mapper

import pe.com.smart.data.remote.dto.doctor.DoctorDto
import pe.com.smart.domain.model.Doctor

fun DoctorDto.toDomain(): Doctor {
    return Doctor(
        id = id ?: 0,
        name = name.orEmpty(),
        lastName = lastName.orEmpty(),
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        specialityId = specialityId ?: 0,
        specialityName = specialityName.orEmpty(),
        deleted = deleted ?: false
    )
}
