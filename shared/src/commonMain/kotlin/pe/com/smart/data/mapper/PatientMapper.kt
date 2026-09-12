package pe.com.smart.data.mapper

import pe.com.smart.data.remote.patient.dto.PatientDto
import pe.com.smart.domain.model.Patient

fun PatientDto.toDomain(): Patient {

    return Patient(
        id = id,
        name = name,
        lastName = lastName,
        fullName = fullName,
        dni = dni,
        email = email,
        phone = phone
    )
}