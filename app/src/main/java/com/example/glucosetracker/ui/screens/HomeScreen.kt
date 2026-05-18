package com.example.glucosetracker.ui.screens

@Composable
fun HomeScreen(viewModel: HomeViewModel) {

    val glucoseList by viewModel.glucoseList.collectAsState()
    val mealsList by viewModel.mealsList.collectAsState()

    var glucoseInput by remember {
        mutableStateOf("")
    }

    var mealName by remember {
        mutableStateOf("")
    }

    var carbsInput by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {

        item {
            Text(
                text = "CGM",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "$current ммоль/л",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Глюкоза в нормальном диапазоне")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

item {
    GlucoseChart(glucoseList)

    Spacer(modifier = Modifier.height(16.dp))
}

item {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Добавить уровень глюкозы")

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = glucoseInput,
                onValueChange = {
                    glucoseInput = it
                },
                label = {
                    Text("ммоль/л")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val value = glucoseInput.toFloatOrNull()

                    if (value != null) {
                        viewModel.addGlucose(value)
                        glucoseInput = ""
                    }
                }
            ) {
                Text("Сохранить")
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

item {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Добавить прием пищи")

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = mealName,
                onValueChange = {
                    mealName = it
                },
                label = {
                    Text("Название еды")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = carbsInput,
                onValueChange = {
                    carbsInput = it
                },
                label = {
                    Text("Углеводы")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val carbs = carbsInput.toIntOrNull()

                    if (carbs != null) {
                        viewModel.addMeal(mealName, carbs)

                        mealName = ""
                        carbsInput = ""
                    }
                }
            ) {
                Text("Добавить")
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

item {
    Text(
        text = "Последние приемы пищи",
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(8.dp))
}

items(mealsList) { meal ->

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = meal.mealName)
                Text(text = "${meal.carbs} г углеводов")
            }
        }
    }