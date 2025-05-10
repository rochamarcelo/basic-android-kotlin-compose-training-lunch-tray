/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.datasource.ScreenRoute
import com.example.lunchtray.model.OrderUiState
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen
import com.example.lunchtray.ui.formatPrice

// TODO: Screen enum

// TODO: AppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(

) {
    // TODO: Create Controller and initialization

    // Create ViewModel
    val viewModel: OrderViewModel = viewModel()
    val navController: NavHostController = rememberNavController();
    val backStackEntry by  navController.currentBackStackEntryAsState();
    val currentScreen = ScreenRoute.valueOf(backStackEntry?.destination?.route ?: ScreenRoute.Start.name)

    Scaffold(
        topBar = {
            // TODO: AppBar
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Start.name,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = ScreenRoute.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {navController.navigate(ScreenRoute.EntreeMenu.name)},
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(route = ScreenRoute.EntreeMenu.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = { cancel(viewModel, navController) },
                    onSelectionChanged = { viewModel.updateEntree(it)},
                    onNextButtonClicked = { navController.navigate(ScreenRoute.SideDishMenu.name) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(route = ScreenRoute.SideDishMenu.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onSelectionChanged = { viewModel.updateSideDish(it)},
                    onNextButtonClicked = { navController.navigate(ScreenRoute.AccompanimentMenu.name)},
                    onCancelButtonClicked = { cancel(viewModel, navController) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(route = ScreenRoute.AccompanimentMenu.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onSelectionChanged = { viewModel.updateAccompaniment(it) },
                    onCancelButtonClicked = { cancel(viewModel, navController) },
                    onNextButtonClicked = { navController.navigate(ScreenRoute.Checkout.name) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable(route = ScreenRoute.Checkout.name) {
                val context = LocalContext.current;
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = { share(context, uiState)},
                    onCancelButtonClicked = { cancel(viewModel, navController) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
private fun cancel(viewModel: OrderViewModel, navHostController: NavHostController) {
    viewModel.resetOrder();
    navHostController.popBackStack(ScreenRoute.Start.name, inclusive = false);
}

private fun share(context: Context, uiState: OrderUiState) {
    val summary = context.getString(
        R.string.order_share,
        uiState.entree?.name ?: "",
        uiState.entree?.getFormattedPrice() ?: "",
        uiState.sideDish?.name ?: "",
        uiState.sideDish?.getFormattedPrice() ?: "",
        uiState.accompaniment?.name ?: "",
        uiState.accompaniment?.getFormattedPrice() ?: "",
        uiState.itemTotalPrice.formatPrice(),
        uiState.orderTax.formatPrice(),
        uiState.orderTotalPrice.formatPrice()
    );
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.order_summary))
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.order_summary)
        )
    )
}
