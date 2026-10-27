package pe.com.smart.data.mapper

import pe.com.smart.data.remote.doctor.dt.DoctorDto
import pe.com.smart.domain.model.Doctor

fun DoctorDto.toDomain(): Doctor =
    Doctor(
        id = id,
        name = name,
        lastName = lastName,
        fullName = fullName,
        email = email,
        phone = phone,
        specialityId = specialityId,
        specialityName = specialityName
    )