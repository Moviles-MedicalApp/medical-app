package pe.com.smart.di

import org.koin.dsl.module
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.data.repository.AuthRepositoryImpl
import pe.com.smart.domain.repository.AppointmentRepository
import pe.com.smart.domain.repository.AuthRepository
import pe.com.smart.domain.repository.DoctorRepository
import pe.com.smart.domain.repository.MedicalScheduleRepository
import pe.com.smart.domain.repository.PatientRepository
import pe.com.smart.domain.repository.SpecialityRepository
import pe.com.smart.domain.usecase.AppointmentUseCase
import pe.com.smart.domain.usecase.AuthUseCase
import pe.com.smart.domain.usecase.DoctorUseCase
import pe.com.smart.domain.usecase.MedicalScheduleUseCase
import pe.com.smart.domain.usecase.PatientUseCase
import pe.com.smart.domain.usecase.SpecialityUseCase
import pe.com.smart.presentation.login.LoginViewModel
import pe.com.smart.presentation.login.RegistrationViewModel
import pe.com.smart.presentation.appointments.AppointmentsViewModel
import pe.com.smart.presentation.doctors.DoctorsViewModel
import pe.com.smart.presentation.home.HomeViewModel
import pe.com.smart.presentation.patients.PatientsViewModel
import pe.com.smart.presentation.schedules.MedicalSchedulesViewModel
import pe.com.smart.presentation.specialities.SpecialitiesViewModel
import pe.com.smart.presentation.users.UsersViewModel

val appModule = module {
    single<AppointmentRepository> { ApiProvider.appointmentRepository }
    single<DoctorRepository> { ApiProvider.doctorRepository }
    single<MedicalScheduleRepository> { ApiProvider.medicalScheduleRepository }
    single<PatientRepository> { ApiProvider.patientRepository }
    single<SpecialityRepository> { ApiProvider.specialityRepository }
    single<AuthRepository> { AuthRepositoryImpl(ApiProvider.authApiService) }

    factory { AppointmentUseCase(get()) }
    factory { AuthUseCase(get()) }
    factory { DoctorUseCase(get()) }
    factory { MedicalScheduleUseCase(get()) }
    factory { PatientUseCase(get()) }
    factory { SpecialityUseCase(get()) }

    factory { LoginViewModel(get()) }
    factory { RegistrationViewModel(get()) }
    factory { UsersViewModel(get(), get()) }
    factory { AppointmentsViewModel(get(), get(), get(), get()) }
    factory { DoctorsViewModel(get()) }
    factory { HomeViewModel(get(), get(), get(), get()) }
    factory { PatientsViewModel(get()) }
    factory { MedicalSchedulesViewModel(get(), get()) }
    factory { SpecialitiesViewModel(get()) }
}
