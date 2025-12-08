package com.example.rentify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.data.repository.PropertyRemoteRepository
import com.example.rentify.ui.viewmodel.PropiedadViewModel
import com.example.rentify.ui.viewmodel.PropiedadViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarPropiedadScreen(
    navController: NavController,
    context: android.content.Context
) {
    val db = RentifyDatabase.getInstance(context)
    val viewModel: PropiedadViewModel = viewModel(
        factory = PropiedadViewModelFactory(
            propiedadDao = db.propiedadDao(),
            catalogDao = db.catalogDao(),
            remoteRepository = PropertyRemoteRepository()
        )
    )

    var codigo by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var precioMensual by remember { mutableStateOf("") }
    var divisa by remember { mutableStateOf("CLP") }
    var m2 by remember { mutableStateOf("") }
    var nHabit by remember { mutableStateOf("") }
    var nBanos by remember { mutableStateOf("") }
    var petFriendly by remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf("") }
    var tipoId by remember { mutableStateOf<Long?>(null) }
    var comunaId by remember { mutableStateOf<Long?>(null) }

    var mostrarTipos by remember { mutableStateOf(false) }
    var mostrarComunas by remember { mutableStateOf(false) }

    val tipos = remember { listOf("Casa" to 1L, "Departamento" to 2L, "Oficina" to 3L) }
    val comunas = remember { listOf("Santiago" to 1L, "Providencia" to 2L, "Las Condes" to 3L) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Propiedad") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioMensual,
                onValueChange = { precioMensual = it },
                label = { Text("Precio Mensual") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = divisa,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Divisa") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = m2,
                onValueChange = { m2 = it },
                label = { Text("Metros cuadrados") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nHabit,
                    onValueChange = { nHabit = it },
                    label = { Text("Habitaciones") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nBanos,
                    onValueChange = { nBanos = it },
                    label = { Text("Baños") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Acepta mascotas", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = petFriendly,
                    onCheckedChange = { petFriendly = it }
                )
            }

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = mostrarTipos,
                onExpandedChange = { mostrarTipos = it }
            ) {
                OutlinedTextField(
                    value = tipos.find { it.second == tipoId }?.first ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de propiedad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarTipos) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = mostrarTipos,
                    onDismissRequest = { mostrarTipos = false }
                ) {
                    tipos.forEach { (nombre, id) ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                tipoId = id
                                mostrarTipos = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = mostrarComunas,
                onExpandedChange = { mostrarComunas = it }
            ) {
                OutlinedTextField(
                    value = comunas.find { it.second == comunaId }?.first ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Comuna") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarComunas) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = mostrarComunas,
                    onDismissRequest = { mostrarComunas = false }
                ) {
                    comunas.forEach { (nombre, id) ->
                        DropdownMenuItem(
                            text = { Text(nombre) },
                            onClick = {
                                comunaId = id
                                mostrarComunas = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (codigo.isNotBlank() && titulo.isNotBlank() && precioMensual.isNotBlank() &&
                        m2.isNotBlank() && nHabit.isNotBlank() && nBanos.isNotBlank() &&
                        tipoId != null && comunaId != null) {

                        scope.launch {
                            isLoading = true
                            val repository = PropertyRemoteRepository()
                            val result = repository.crearPropiedad(
                                codigo = codigo,
                                titulo = titulo,
                                precioMensual = precioMensual.toDouble(),
                                divisa = divisa,
                                m2 = m2.toDouble(),
                                nHabit = nHabit.toInt(),
                                nBanos = nBanos.toInt(),
                                petFriendly = petFriendly,
                                direccion = direccion,
                                tipoId = tipoId!!,
                                comunaId = comunaId!!
                            )

                            when (result) {
                                is com.example.rentify.data.remote.ApiResult.Success -> {
                                    snackbarHostState.showSnackbar("Propiedad creada exitosamente")
                                    navController.navigateUp()
                                }
                                is com.example.rentify.data.remote.ApiResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                            isLoading = false
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor complete todos los campos")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Propiedad")
                }
            }
        }
    }
}