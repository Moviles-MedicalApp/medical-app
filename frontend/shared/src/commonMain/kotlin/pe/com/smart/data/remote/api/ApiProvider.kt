package pe.com.smart.data.remote.api

import pe.com.smart.data.remote.api.auth.AuthApiService
import pe.com.smart.data.remote.api.appointment.AppointmentApiService
import pe.com.smart.data.remote.api.doctor.DoctorApiService
import pe.com.smart.data.remote.api.patient.PatientApiService
import pe.com.smart.data.remote.api.speciality.SpecialityApiService
import pe.com.smart.data.remote.api.schedule.MedicalScheduleApiService
import pe.com.smart.data.repository.DoctorRepositoryImpl
import pe.com.smart.data.repository.AppointmentRepositoryImpl
import pe.com.smart.data.repository.MedicalScheduleRepositoryImpl
import pe.com.smart.data.repository.PatientRepositoryImpl
import pe.com.smart.data.repository.SpecialityRepositoryImpl
import pe.com.smart.domain.repository.DoctorRepository
import pe.com.smart.domain.repository.AppointmentRepository
import pe.com.smart.domain.repository.MedicalScheduleRepository
import pe.com.smart.domain.repository.PatientRepository
import pe.com.smart.domain.repository.SpecialityRepository

object ApiProvider {

    private val httpClient =
        HttpClientFactory.create()

    val authApiService =
        AuthApiService(httpClient)

    private val patientApiService =
        PatientApiService(httpClient)

    val patientRepository: PatientRepository =
        PatientRepositoryImpl(
            patientApiService
        )

    private val specialityApiService =
        SpecialityApiService(httpClient)

    val specialityRepository: SpecialityRepository =
        SpecialityRepositoryImpl(
            specialityApiService
        )

    private val doctorApiService =
        DoctorApiService(httpClient)

    val doctorRepository: DoctorRepository =
        DoctorRepositoryImpl(
            doctorApiService
        )

    private val appointmentApiService = AppointmentApiService(httpClient)
    val appointmentRepository: AppointmentRepository = AppointmentRepositoryImpl(appointmentApiService)

    private val medicalScheduleApiService =
        MedicalScheduleApiService(httpClient)

    val medicalScheduleRepository: MedicalScheduleRepository =
        MedicalScheduleRepositoryImpl(medicalScheduleApiService)
}
