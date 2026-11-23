
package pe.com.smart.presentation.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.com.smart.data.remote.auth.dto.AdminUserDto
import pe.com.smart.presentation.components.form.AppSearchField

@Composable
fun UsersScreen(
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = koinViewModel()
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val selectionVersion =
        UserProvisioningSelection.version

    LaunchedEffect(selectionVersion, viewModel) {
        viewModel.applyProvisioningSelection()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (state.showCreate) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.background
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .padding(bottom = if (state.showCreate) 16.dp else 84.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.showCreate) {
                TextButton(
                    onClick = { viewModel.setCreateVisible(false) },
                    // Compose rejects negative padding at runtime. Keeping the
                    // standard button bounds prevents the create screen from
                    // crashing as soon as it is rendered.
                    modifier = Modifier
                ) { Text("‹  Volver a usuarios") }

                Text(
                    text = "Nueva cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea accesos de forma segura y vincúlalos a un perfil cuando sea necesario.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CreateUserContent(state = state, viewModel = viewModel)
            } else {
                Text(
                    text = "Gestión de usuarios",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Administra las cuentas, los permisos y el acceso a MedicalApp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                UsersListContent(state = state, viewModel = viewModel)
            }
        }

        if (!state.showCreate) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setCreateVisible(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Nueva cuenta") }
            )
        }
    }

    state.edit?.let { edit ->
        UserEditDialog(
            state = state,
            edit = edit,
            viewModel = viewModel
        )
    }
}

@Composable
private fun UsersListContent(
    state: UsersUiState,
    viewModel: UsersViewModel
) {
    var showFilters by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("${state.userPage.totalElements} cuentas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (showFilters) "Ocultar" else if (
                        state.roleFilter != null || state.statusFilter != null
                    ) "Filtros" else "Filtrar"
                )
            }
        }

        AppSearchField(
            query = state.search,
            onQueryChange = viewModel::onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Buscar por usuario o correo",
            contentDescription = "Buscar usuario"
        )

        if (state.roleFilter != null || state.statusFilter != null) {
            Text(
                text = listOfNotNull(
                    state.roleFilter?.let(::userRoleLabel),
                    state.statusFilter?.let(::userStatusLabel)
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        }
    }

    if (showFilters) {
        UserFiltersPanel(
            state = state,
            onRoleSelected = viewModel::onRoleFilter,
            onStatusSelected = viewModel::onStatusFilter,
            onApply = {
                viewModel.searchUsers()
            }
        )
    }

    if (state.isLoadingUsers) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )

            Text(
                text = "Cargando cuentas...",
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        }
    }

    state.usersError?.let {
        ErrorBanner(it)
    }

    if (
        !state.isLoadingUsers ||
        state.userPage.content.isNotEmpty()
    ) {
        if (state.userPage.content.isEmpty()) {
            Text(
                text = "No se encontraron cuentas con los filtros aplicados.",
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        } else {
            state.userPage.content.forEach { user ->
                UserListCard(
                    user = user,
                    onManage = {
                        viewModel.openUser(user)
                    }
                )
            }
        }
    }

    if (state.userPage.totalPages > 1) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.changePage(state.page - 1)
                },
                enabled = !state.isLoadingUsers &&
                        state.page > 0
            ) {
                Text("Anterior")
            }

            Text(
                text = "${state.page + 1} / ${state.userPage.totalPages}",
                style = MaterialTheme.typography.labelMedium
            )

            OutlinedButton(
                onClick = {
                    viewModel.changePage(state.page + 1)
                },
                enabled = !state.isLoadingUsers &&
                        state.page + 1 < state.userPage.totalPages
            ) {
                Text("Siguiente")
            }
        }
    }
}

@Composable
private fun UserFiltersPanel(
    state: UsersUiState,
    onRoleSelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onApply: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7F9FA),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtrar cuentas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rol", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserFilterChip(
                        selected = state.roleFilter == null,
                        onClick = { onRoleSelected(null) },
                        label = { Text("Todos") }
                    )
                    state.roles.forEach { role ->
                        UserFilterChip(
                            selected = state.roleFilter == role,
                            onClick = { onRoleSelected(role) },
                            label = { Text(userRoleLabel(role)) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estado", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserFilterChip(
                        selected = state.statusFilter == null,
                        onClick = { onStatusSelected(null) },
                        label = { Text("Todos") }
                    )
                    accountStatuses.forEach { status ->
                        UserFilterChip(
                            selected = state.statusFilter == status,
                            onClick = { onStatusSelected(status) },
                            label = { Text(userStatusLabel(status)) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    onRoleSelected(null)
                    onStatusSelected(null)
                    onApply()
                }) { Text("Limpiar") }
                Button(onClick = onApply) { Text("Aplicar filtros") }
            }
        }
    }
}

@Composable
private fun UserListCard(
    user: AdminUserDto,
    onManage: () -> Unit
) {
    SectionCard(user.username) {
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Estado: ${userStatusLabel(user.status)}",
            color = if (user.status == "ACTIVE") {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Text(
            text = "Roles: ${
                user.roles.joinToString {
                    userRoleLabel(it)
                }
            }",
            style = MaterialTheme.typography.bodySmall
        )

        user.profiles.forEach { profile ->
            Text(
                text = "${userRoleLabel(profile.type)}: ${profile.fullName}" +
                        if (profile.deleted) {
                            " (eliminado)"
                        } else {
                            ""
                        },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onManage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Gestionar cuenta")
        }
    }
}

@Composable
private fun CreateUserContent(
    state: UsersUiState,
    viewModel: UsersViewModel
) {
    val form = state.create
    val linking = form.profileId != null

    val selectedAvailable = state.availableProfiles.any {
        it.profileId == form.profileId
    }

    val canSubmit = !form.isSaving && (
            !linking ||
                    (
                            state.profilesLoaded &&
                                    state.profileAvailability ==
                                    ProfileAvailabilityState.Success &&
                                    selectedAvailable
                            )
            )

    SectionCard("Tipo de cuenta") {
        if (linking && form.profileName != null) {
            Surface(
                color = MaterialTheme
                    .colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Vincular cuenta a",
                        style = MaterialTheme
                            .typography.labelMedium
                    )

                    Text(
                        text = form.profileName,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = userRoleLabel(form.role),
                        style = MaterialTheme
                            .typography.bodySmall
                    )
                }
            }
        }

        if (!linking) {
            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                state.roles.forEach { role ->
                    UserFilterChip(
                        selected = form.role == role,
                        onClick = {
                            viewModel.changeCreateRole(role)
                        },
                        label = { Text(userRoleLabel(role)) }
                    )
                }
            }
        }

        if (form.role != "ADMIN") {
            Text(
                text = if (form.role == "PATIENT") {
                    "Selecciona un perfil de paciente existente para crear su cuenta de acceso."
                } else {
                    "Vincula un perfil activo existente o crea uno nuevo cuando corresponda."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )

            when (state.profileAvailability) {
                ProfileAvailabilityState.Loading -> {
                    Text("Verificando perfiles disponibles...")
                }

                ProfileAvailabilityState.Empty -> {
                    Text("No hay perfiles activos sin cuenta.")
                }

                ProfileAvailabilityState.Unauthorized -> {
                    ErrorBanner(
                        "Tu sesión expiró. Inicia sesión nuevamente."
                    )
                }

                ProfileAvailabilityState.Forbidden -> {
                    ErrorBanner(
                        "Se requieren permisos de administrador para consultar perfiles."
                    )
                }

                ProfileAvailabilityState.Failure,
                ProfileAvailabilityState.BackendUnavailable -> {
                    ErrorBanner(
                        "No se pudo verificar la disponibilidad de perfiles."
                    )

                    OutlinedButton(
                        onClick =
                            viewModel::refreshAvailableProfiles
                    ) {
                        Text("Reintentar verificación")
                    }
                }

                ProfileAvailabilityState.Success -> {
                    if (linking && !selectedAvailable) {
                        ErrorBanner(
                            "El perfil ya no está disponible para vincular."
                        )
                    } else if (!linking) {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            state.availableProfiles.forEach { profile ->
                                UserFilterChip(
                                    selected =
                                        profile.profileId ==
                                                form.profileId,
                                    onClick = {
                                        viewModel.selectProfile(
                                            profile.profileId
                                        )
                                    },
                                    label = {
                                        Text("${profile.fullName} · ${profile.identifier}")
                                    }
                                )
                            }
                        }
                    }
                }

                ProfileAvailabilityState.Idle -> Unit
            }

            if (linking && form.profileName == null) {
                OutlinedButton(
                    onClick = {
                        viewModel.selectProfile(null)
                    },
                    enabled = !form.isSaving
                ) {
                    Text("No vincular perfil")
                }
            }
        }
    }

    SectionCard("Credenciales de acceso") {
        AdminField(
            label = "Usuario",
            value = form.username,
            enabled = !form.isSaving,
            onValueChange = { value ->
                viewModel.updateCreate {
                    it.copy(username = value)
                }
            }
        )

        AdminField(
            label = "Correo electrónico",
            value = form.email,
            enabled = !form.isSaving &&
                    !(linking && form.role == "DOCTOR"),
            keyboardType = KeyboardType.Email,
            onValueChange = { value ->
                viewModel.updateCreate {
                    it.copy(email = value)
                }
            }
        )

        if (linking && form.role == "DOCTOR") {
            Text(
                text = "Se utilizará el correo registrado en el perfil médico.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "La cuenta se crea activa con esta contraseña inicial. Entrégala al usuario por un canal seguro; MedicalApp no la almacenará después de crear la cuenta.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AdminField(
            label = "Contraseña temporal",
            value = form.password,
            enabled = !form.isSaving,
            password = true,
            onValueChange = { value ->
                viewModel.updateCreate {
                    it.copy(password = value)
                }
            }
        )

        AdminField(
            label = "Confirmar contraseña",
            value = form.confirmPassword,
            enabled = !form.isSaving,
            password = true,
            onValueChange = { value ->
                viewModel.updateCreate {
                    it.copy(confirmPassword = value)
                }
            }
        )
    }

    if (
        !linking &&
        form.role !in setOf("ADMIN", "PATIENT")
    ) {
        SectionCard("Datos del perfil") {
            AdminField(
                label = "Nombres",
                value = form.name,
                enabled = !form.isSaving,
                onValueChange = { value ->
                    viewModel.updateCreate {
                        it.copy(name = value)
                    }
                }
            )

            AdminField(
                label = "Apellidos",
                value = form.lastName,
                enabled = !form.isSaving,
                onValueChange = { value ->
                    viewModel.updateCreate {
                        it.copy(lastName = value)
                    }
                }
            )

            if (form.role == "DOCTOR") {
                Text(
                    text = "Especialidad médica",
                    style = MaterialTheme
                        .typography.labelLarge
                )

                SpecialityPicker(
                    selectedId = form.specialityId,
                    state = state,
                    enabled = !form.isSaving,
                    onSelected = { id ->
                        viewModel.updateCreate {
                            it.copy(specialityId = id)
                        }
                    }
                )
            }

            if (form.role == "EMPLOYEE") {
                AdminField(
                    label = "DNI (8 dígitos)",
                    value = form.dni,
                    enabled = !form.isSaving,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { value ->
                        viewModel.updateCreate {
                            it.copy(
                                dni = value.filter {
                                        char -> char.isDigit()
                                }.take(8)
                            )
                        }
                    }
                )

                AdminField(
                    label = "Cargo",
                    value = form.jobTitle,
                    enabled = !form.isSaving,
                    onValueChange = { value ->
                        viewModel.updateCreate {
                            it.copy(jobTitle = value)
                        }
                    }
                )
            }

            AdminField(
                label = "Teléfono",
                value = form.phone,
                enabled = !form.isSaving,
                keyboardType = KeyboardType.Phone,
                onValueChange = { value ->
                    viewModel.updateCreate {
                        it.copy(phone = value.take(16))
                    }
                }
            )
        }
    }

    state.catalogError?.let {
        ErrorBanner(it)
    }

    form.error?.let {
        ErrorBanner(it)
    }

    Button(
        onClick = viewModel::createUser,
        modifier = Modifier.fillMaxWidth(),
        enabled = canSubmit,
        shape = RoundedCornerShape(14.dp)
    ) {
        if (form.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            if (form.isSaving) {
                "Creando cuenta..."
            } else {
                "Crear usuario"
            }
        )
    }
}

@Composable
private fun SpecialityPicker(
    selectedId: Int?,
    state: UsersUiState,
    enabled: Boolean,
    onSelected: (Int) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box {
        OutlinedButton(
            onClick = {
                expanded = true
            },
            enabled = enabled &&
                    state.specialities.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                state.specialities.firstOrNull {
                    it.id == selectedId
                }?.name ?: "Selecciona una especialidad"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            state.specialities.forEach { speciality ->
                DropdownMenuItem(
                    text = {
                        Text(speciality.name)
                    },
                    onClick = {
                        expanded = false
                        onSelected(speciality.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun UserEditDialog(
    state: UsersUiState,
    edit: UserEditFormState,
    viewModel: UsersViewModel
) {
    val action = edit.pendingAction

    Dialog(
        onDismissRequest = viewModel::closeUser,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(24.dp),
            // Keep the editing area neutral. The previous blue-tinted surface made
            // the form compete visually with the account status and primary action.
            color = Color(0xFFFFFEFC),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Gestionar cuenta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = edit.user.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = viewModel::closeUser,
                        enabled = !edit.isWorking
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = Color(0xFFF3F7F8),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Estado: ${userStatusLabel(edit.user.status)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            edit.user.profiles.forEach { profile ->
                                Text(
                                    text = "${userRoleLabel(profile.type)}: ${profile.fullName}" +
                                        if (profile.deleted) " (eliminado)" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Text(
                        text = "Datos de acceso",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                AdminField(
                    label = "Usuario",
                    value = edit.username,
                    enabled = !edit.isWorking,
                    onValueChange = { value ->
                        viewModel.updateEdit {
                            it.copy(username = value)
                        }
                    }
                )

                AdminField(
                    label = "Correo electrónico",
                    value = edit.email,
                    enabled = !edit.isWorking,
                    keyboardType = KeyboardType.Email,
                    onValueChange = { value ->
                        viewModel.updateEdit {
                            it.copy(email = value)
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text("Correo verificado")

                    Switch(
                        checked = edit.emailVerified,
                        enabled = !edit.isWorking,
                        onCheckedChange = { value ->
                            viewModel.updateEdit {
                                it.copy(
                                    emailVerified = value
                                )
                            }
                        }
                    )
                }

                Text(
                    text = "Roles",
                    style = MaterialTheme
                        .typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                RoleSelector(
                    roles = state.roles,
                    selectedRoles = edit.roles,
                    enabled = !edit.isWorking,
                    canChangeRole = { role ->
                        val ownAdmin = state.currentUserId == edit.user.id && role == "ADMIN"
                        val hasProfile = role == "ADMIN" || edit.user.profiles.any {
                            it.type == role && !it.deleted
                        }
                        !ownAdmin && hasProfile
                    },
                    onRoleToggled = viewModel::toggleEditRole
                )

                if (state.currentUserId == edit.user.id) {
                    Text(
                        text = "No puedes bloquear, desactivar ni retirar tu rol de administrador.",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = MaterialTheme
                            .colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Text(
                    text = "Seguridad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                AdminField(
                    label = "Contraseña temporal",
                    value = edit.resetPassword,
                    enabled = !edit.isWorking,
                    password = true,
                    onValueChange = { value ->
                        viewModel.updateEdit {
                            it.copy(resetPassword = value)
                        }
                    }
                )

                AdminField(
                    label = "Confirmar contraseña",
                    value = edit.resetConfirmation,
                    enabled = !edit.isWorking,
                    password = true,
                    onValueChange = { value ->
                        viewModel.updateEdit {
                            it.copy(resetConfirmation = value)
                        }
                    }
                )

                OutlinedButton(
                    onClick = {
                        viewModel.requestAction(
                            UserAction.ResetPassword
                        )
                    },
                    enabled = !edit.isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Restablecer contraseña")
                }

                if (state.currentUserId != edit.user.id) {
                    Row(
                        modifier = Modifier.horizontalScroll(
                            rememberScrollState()
                        ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        if (
                            edit.user.status != "ACTIVE" &&
                            edit.user.status != "DISABLED"
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.requestAction(
                                        UserAction.ChangeStatus(
                                            "ACTIVE"
                                        )
                                    )
                                },
                                enabled = !edit.isWorking
                            ) {
                                Text("Activar")
                            }
                        }

                        if (edit.user.status == "ACTIVE") {
                            OutlinedButton(
                                onClick = {
                                    viewModel.requestAction(
                                        UserAction.ChangeStatus(
                                            "BLOCKED"
                                        )
                                    )
                                },
                                enabled = !edit.isWorking
                            ) {
                                Text("Bloquear")
                            }
                        }

                        if (edit.user.status != "DISABLED") {
                            OutlinedButton(
                                onClick = {
                                    viewModel.requestAction(
                                        UserAction.Disable
                                    )
                                },
                                enabled = !edit.isWorking
                            ) {
                                Text("Desactivar")
                            }
                        }
                    }
                }

                edit.error?.let {
                    ErrorBanner(it)
                }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = viewModel::closeUser,
                        enabled = !edit.isWorking,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar")
                    }
                    Button(
                        onClick = { viewModel.requestAction(UserAction.Save) },
                        enabled = !edit.isWorking,
                        modifier = Modifier.weight(1.6f)
                    ) {
                        Text(if (edit.isWorking) "Guardando..." else "Guardar cambios")
                    }
                }
            }
        }
    }

    if (action != null) {
        val label = when (action) {
            UserAction.Save ->
                "Guardar cambios"

            UserAction.ResetPassword ->
                "Restablecer contraseña"

            UserAction.Disable ->
                "Desactivar cuenta"

            is UserAction.ChangeStatus ->
                if (action.status == "BLOCKED") {
                    "Bloquear cuenta"
                } else {
                    "Activar cuenta"
                }
        }

        AlertDialog(
            onDismissRequest = {
                if (!edit.isWorking) {
                    viewModel.cancelAction()
                }
            },
            title = {
                Text(label)
            },
            text = {
                Text(
                    "¿Confirmas esta operación para " +
                            "${edit.user.username}? " +
                            "El cambio se aplicará en el servidor."
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = viewModel::cancelAction,
                    enabled = !edit.isWorking
                ) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmAction,
                    enabled = !edit.isWorking
                ) {
                    Text(
                        if (edit.isWorking) {
                            "Procesando..."
                        } else {
                            "Confirmar"
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun RoleSelector(
    roles: List<String>,
    selectedRoles: Set<String>,
    enabled: Boolean,
    canChangeRole: (String) -> Boolean,
    onRoleToggled: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabels = selectedRoles
        .map(::userRoleLabel)
        .sorted()
        .joinToString(", ")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF7F9FA),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = "Roles asignados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedLabels.ifBlank { "Sin roles asignados" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { expanded = !expanded },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Modificar roles",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Outlined.KeyboardArrowUp
                    } else {
                        Icons.Outlined.KeyboardArrowDown
                    },
                    contentDescription = null
                )
            }

            if (expanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = Color(0xFFF7F9FA),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        roles.forEach { role ->
                            val canChange = canChangeRole(role)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        enabled = enabled && canChange,
                                        onClick = { onRoleToggled(role) }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(userRoleLabel(role))
                                    if (!canChange) {
                                        Text(
                                            text = "No disponible sin un perfil activo vinculado",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                Checkbox(
                                    checked = role in selectedRoles,
                                    onCheckedChange = null,
                                    enabled = enabled && canChange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(
                color = colors.outlineVariant
            )

            content()
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun UserFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable (() -> Unit))? = null
) {
    val colors = MaterialTheme.colorScheme

    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        label = label,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(12.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colors.outlineVariant,
            selectedBorderColor = colors.primary.copy(alpha = 0.24f)
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surface,
            labelColor = colors.onSurfaceVariant,
            iconColor = colors.primary,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
            selectedLeadingIconColor = colors.onPrimaryContainer
        )
    )
}

@Composable
private fun AdminField(
    label: String,
    value: String,
    enabled: Boolean,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = {
            Text(label)
        },
        leadingIcon = if (password) {
            {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null
                )
            }
        } else if (keyboardType == KeyboardType.Email) {
            {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null
                )
            }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        visualTransformation = if (password) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        shape = RoundedCornerShape(14.dp)
    )
}
