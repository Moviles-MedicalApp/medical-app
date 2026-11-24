package pe.com.smart.presentation.patients.form

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PatientFormViewModelTest {

    @Test
    fun testDniValidation_empty() {
        val viewModel = PatientFormViewModel(patientId = null)
        
        assertEquals("", viewModel.uiState.value.dni)
        
        viewModel.savePatient(onSuccess = {})
        
        assertNotNull(viewModel.uiState.value.dniError)
        assertEquals("Ingresa el DNI", viewModel.uiState.value.dniError)
    }

    @Test
    fun testDniValidation_shortLength() {
        val viewModel = PatientFormViewModel(patientId = null)
        
        viewModel.onDniChange("1234567") // 7 digits
        assertEquals("1234567", viewModel.uiState.value.dni)
        
        viewModel.savePatient(onSuccess = {})
        
        assertNotNull(viewModel.uiState.value.dniError)
        assertEquals("El DNI debe tener 8 dígitos", viewModel.uiState.value.dniError)
    }

    @Test
    fun testDniValidation_valid() {
        val viewModel = PatientFormViewModel(patientId = null)
        
        viewModel.onDniChange("12345678") // 8 digits
        assertEquals("12345678", viewModel.uiState.value.dni)
        
        viewModel.savePatient(onSuccess = {})
        
        assertNull(viewModel.uiState.value.dniError)
    }
}
