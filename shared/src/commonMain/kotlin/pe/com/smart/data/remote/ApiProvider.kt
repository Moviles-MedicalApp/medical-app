package pe.com.smart.data.remote

import pe.com.smart.data.remote.auth.AuthApiService
import pe.com.smart.data.remote.patient.PatientApiService
import pe.com.smart.data.repository.PatientRepositoryImpl
import pe.com.smart.domain.repository.PatientRepository

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
}