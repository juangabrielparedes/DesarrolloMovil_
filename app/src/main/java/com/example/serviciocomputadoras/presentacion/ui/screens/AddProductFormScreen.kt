package com.example.serviciocomputadoras.presentacion.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun ScreenForm(navController: NavController){
    var codProd by remember{ mutableStateOf("")}

    var nameProd by remember{ mutableStateOf("")}

    var priceProd by remember{ mutableStateOf("")}

    var unitProd by remember{ mutableStateOf("")}

    Column(Modifier.padding(16.dp)){
        Text("Registro de nuevo producto", style=MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = codProd,
            onValueChange = {codProd=it},
            label={Text("Código del producto")},
            isError = codProd.isBlank()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = nameProd,
            onValueChange = {nameProd=it},
            label={Text("Nombre del producto")},
            isError = nameProd.isBlank()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = priceProd,
            onValueChange = {priceProd=it},
            label={Text("Precio del producto")},
            isError = priceProd.isBlank()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = unitProd,
            onValueChange = {unitProd=it},
            label={Text("Unidades del producto")},
            isError = unitProd.isBlank()
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if(codProd.isNotBlank() && nameProd.isNotBlank() && priceProd.isNotBlank() && unitProd.isNotBlank()){
                navController.popBackStack()
            }
        }){
            Text("Guardar")
        }

    }
}