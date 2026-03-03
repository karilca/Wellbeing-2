package com.gymflow.app.domain.model

data class Exercise(
    val id: Int = 0,
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Float = 0f,
    val formScore: Float = 0f,
    val muscleGroup: String = "",
    val durationSeconds: Int = 0,
    val description: String = "",
    val instructions: List<String> = emptyList()
) {
    val formScorePercentage: Int
        get() = (formScore * 100).toInt()

    val formStatus: FormStatus
        get() = when {
            formScore >= 0.8f -> FormStatus.GOOD
            formScore >= 0.5f -> FormStatus.WARNING
            else -> FormStatus.BAD
        }

    companion object {
        val DEFAULT_EXERCISES = listOf(
            Exercise(
                name = "Cucanj",
                muscleGroup = "quadriceps,glutes,hamstrings",
                description = "Osnovna vjezba za noge",
                instructions = listOf(
                    "Stanite u sirinuramena",
                    "Spustite kukove prema dolje",
                    "Drzite leda ravno",
                    "Koljena ne smiju prelaziti prste"
                )
            ),
            Exercise(
                name = "Bench Press",
                muscleGroup = "chest,triceps,shoulders",
                description = "Osnovna vjezba za prsa",
                instructions = listOf(
                    "Lezite na klupu",
                    "Uhvatite sipku u sirini ramena",
                    "Spustite sipku do prsa",
                    "Podignite eksplozivno gore"
                )
            ),
            Exercise(
                name = "Deadlift",
                muscleGroup = "back,glutes,hamstrings",
                description = "Osnovna vjezba za leda",
                instructions = listOf(
                    "Stanite uz sipku",
                    "Savijte kukove i koljena",
                    "Uhvatite sipku u sirini ramena",
                    "Podignite drzeci leda ravno"
                )
            ),
            Exercise(
                name = "Sklekovi",
                muscleGroup = "chest,triceps,shoulders",
                description = "Vjezba s tjelesnom tezinom",
                instructions = listOf(
                    "Postavite ruke u sirinu ramena",
                    "Tijelo drzite ravno",
                    "Spustite prsa prema podu",
                    "Podignite se do pocetnog polozaja"
                )
            ),
            Exercise(
                name = "Pull-up",
                muscleGroup = "back,biceps",
                description = "Vjezba za leda i biceps",
                instructions = listOf(
                    "Uhvatite sipku hvatom prema van",
                    "Visiti opusteno",
                    "Povucite se gore dok brada ne prede sipku",
                    "Polako se spustite"
                )
            )
        )
    }
}

enum class FormStatus {
    GOOD,
    WARNING,
    BAD
}