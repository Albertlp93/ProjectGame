pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Configuración que causa el problema
    repositories {
        google() // Repositorio de Google
        mavenCentral() // Repositorio Maven Central
        // Añade otros repositorios aquí si es necesario
    }
}

rootProject.name = "ProjectGame"
include(":app")
 