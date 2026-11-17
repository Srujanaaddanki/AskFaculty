package com.example.askfaculty.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.core.ui.components.ErrorBanner
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (User) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Science & Engineering") }
    var academicYear by remember { mutableStateOf("3rd Year") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var yearExpanded by remember { mutableStateOf(false) }
    val years = listOf("1st Year", "2nd Year", "3rd Year", "Final Year", "Postgraduate")

    LaunchedEffect(uiState.user) {
        if (uiState.isRegistrationSuccess && uiState.user != null) {
            onRegisterSuccess(uiState.user!!)
            viewModel.clearRegistrationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AskFacultyRadius.xl),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        )
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AskFacultySpacing.xl),
                    ) {
                        Text(
                            text = "Create Student Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Join your college academic doubt network",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(Modifier.height(AskFacultySpacing.lg))

                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; viewModel.clearError() },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Aarav Sharma") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Student ID
                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it.uppercase(); viewModel.clearError() },
                            label = { Text("Student ID / College Roll No") },
                            placeholder = { Text("e.g. 22CS104") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; viewModel.clearError() },
                            label = { Text("College Email Address") },
                            placeholder = { Text("student@askfaculty.edu") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Department
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it; viewModel.clearError() },
                            label = { Text("Department / Branch") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Academic Year Dropdown
                        ExposedDropdownMenuBox(
                            expanded = yearExpanded,
                            onExpandedChange = { yearExpanded = it },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = academicYear,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Academic Year") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                            )
                            ExposedDropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false },
                            ) {
                                years.forEach { y ->
                                    DropdownMenuItem(
                                        text = { Text(y) },
                                        onClick = {
                                            academicYear = y
                                            yearExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; viewModel.clearError() },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; viewModel.clearError() },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.registerStudent(name, studentId, email, department, academicYear, password, confirmPassword)
                            }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Error Banner
                        AnimatedVisibility(
                            visible = uiState.error != null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Column {
                                Spacer(Modifier.height(AskFacultySpacing.md))
                                ErrorBanner(message = uiState.error ?: "", onDismiss = viewModel::clearError)
                            }
                        }

                        Spacer(Modifier.height(AskFacultySpacing.xl))

                        AskFacultyButton(
                            text = "Register as Student",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.registerStudent(name, studentId, email, department, academicYear, password, confirmPassword)
                            },
                            isLoading = uiState.isLoading,
                            enabled = name.isNotBlank() && studentId.isNotBlank() && email.isNotBlank() && password.length >= 6,
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Already registered?", style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = onBackToLogin) {
                                Text("Sign In", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(AskFacultySpacing.xl))
            }
        }
    }
}
