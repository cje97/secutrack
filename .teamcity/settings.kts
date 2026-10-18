import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
 * Pipeline TeamCity de SecuTrack, versionné avec le code (Kotlin DSL).
 * TeamCity lit ce fichier quand les « Versioned Settings » du projet sont activés (voir README).
 */

version = "2025.11"

project {
    description = "SecuTrack : API Spring Boot + frontend Angular"

    buildType(Backend)
    buildType(Frontend)

    buildTypesOrder = arrayListOf(Backend, Frontend)
}

object Backend : BuildType({
    name = "Backend - build et tests Maven"
    description = "Compile l'API, exécute les tests JUnit et produit le jar"

    artifactRules = "backend/target/*.jar"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "mvnw verify"
            workingDir = "backend"
            scriptContent = """
                export JAVA_HOME=${'$'}JDK21
                ./mvnw -B verify
            """.trimIndent()
        }
    }

    triggers {
        vcs { }        // un build à chaque push
    }

    features {
        xmlReport {    // résultats JUnit visibles dans l'onglet Tests
            reportType = XmlReport.XmlReportType.JUNIT
            rules = "backend/target/surefire-reports/*.xml"
        }
    }
})

object Frontend : BuildType({
    name = "Frontend - tests et build Angular"
    description = "npm ci, tests Vitest, build de production"

    artifactRules = "frontend/dist/frontend/browser/** => secutrack-web.zip"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "npm ci, ng test, ng build"
            workingDir = "frontend"
            scriptContent = """
                npm ci
                npx ng test --watch=false
                npx ng build
            """.trimIndent()
        }
    }

    triggers {
        vcs { }
    }
})
