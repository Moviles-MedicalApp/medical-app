package pe.com.smart.data.remote

import pe.com.smart.data.remote.auth.AuthApiService
import pe.com.smart.data.remote.doctor.DoctorApiService
import pe.com.smart.data.remote.patient.PatientApiService
import pe.com.smart.data.remote.speciality.SpecialityApiService
import pe.com.smart.data.repository.PatientRepositoryImpl
import pe.com.smart.data.repository.SpecialityRepositoryImpl
import pe.com.smart.domain.repository.DoctorRepository
import pe.com.smart.domain.repository.DoctorRepositoryImpl
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
}
