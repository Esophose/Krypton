plugins {
    kotlin("plugin.serialization")
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.4.20"
}

repositories {
    jcenter()
}

task<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

task<Jar>("javadocJar") {
    from(tasks["dokkaJavadoc"])
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            val releasesRepoURL = uri("https://repo.bristermitten.me/repository/maven-releases/")
            val snapshotsRepoURL = uri("https://repo.bristermitten.me/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoURL else releasesRepoURL

            credentials {
                username = if (project.hasProperty("maven.username")) project.property("maven.username").toString() else System.getenv("MAVEN_USERNAME")
                password = if (project.hasProperty("maven.password")) project.property("maven.password").toString() else System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenKotlin") {
            group = "org.kryptonmc"
            artifactId = "krypton-api"
            version = rootProject.extra["globalVersion"].toString()

            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Krypton API")
                description.set("The official API for Krypton, the fast and lightweight Minecraft server written in Kotlin!")
                url.set("https://www.kryptonmc.org")
                inceptionYear.set("2021")

                packaging = "jar"

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("bombardygamer")
                        name.set("Callum Seabrook")
                        email.set("callum.seabrook@prevarinite.com")
                        timezone.set("Europe/London")

                        organization.set("KryptonMC")
                        organizationUrl.set("https://github.com/KryptonMC")

                        roles.set(setOf("Lead Developer"))
                    }

                    developer {
                        id.set("knightzmc")
                        name.set("Alexander Wood")
                        email.set("alexwood2403@gmail.com")
                        timezone.set("Europe/London")

                        organization.set("KryptonMC")
                        organizationUrl.set("https://github.com/KryptonMC")

                        roles.set(setOf("Developer"))
                    }

                    developer {
                        id.set("esophose")
                        name.set("Nicole Barningham")
                        email.set("esophose@gmail.com")
                        timezone.set("America/Boise")

                        organization.set("KryptonMC")
                        organizationUrl.set("https://github.com/KryptonMC")

                        roles.set(setOf("Developer"))
                    }
                }

                contributors {
                    contributor {
                        name.set("Brandon Li")
                        email.set("brandonli2006ma@gmail.com")
                        timezone.set("America/New_York")
                        roles.set(setOf("Contributor"))
                    }
                }

                organization {
                    name.set("KryptonMC")
                    url.set("https://github.com/KryptonMC")
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/KryptonMC/Krypton/issues")
                }

                ciManagement {
                    system.set("Jenkins")
                    url.set("https://ci.kryptonmc.org/job/Krypton")
                }

                distributionManagement {
                    downloadUrl.set("https://ci.kryptonmc.org/job/Krypton/lastSuccessfulBuild/Krypton")
                }

                scm {
                    connection.set("scm:git:git://github.com/KryptonMC/Krypton.git")
                    developerConnection.set("scm:git:ssh://github.com:KryptonMC/Krypton.git")
                    url.set("https://github.com/KryptonMC/Krypton")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenKotlin"])
}

//publishing {
//    repositories {
//        maven {
//            def releasesRepoUrl = "https://repo.bristermitten.me/repository/maven-releases/"
//            def snapshotsRepoUrl = "https://repo.bristermitten.me/repository/maven-snapshots/"
//            url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
//
//            credentials {
//                username project.hasProperty('maven.username') ? project.property('maven.username') : System.getenv('MAVEN_USERNAME')
//                password project.hasProperty('maven.password') ? project.property('maven.password') : System.getenv('MAVEN_PASSWORD')
//            }
//        }
//    }
//
//    publications {
//        mavenJava(MavenPublication) {
//            group 'org.kryptonmc'
//            artifactId = 'krypton-api'
//            version globalVersion
//
//            from components.kotlin
//            artifact sourcesJar
//            artifact javadocJar
//
//            pom {
//                name = 'Krypton API'
//                description = 'The official API for Krypton, the fast and lightweight Minecraft server written in Kotlin!'
//                url = 'https://www.kryptonmc.org'
//
//                packaging = 'jar'
//
//                licenses {
//                    license {
//                        name = 'MIT License'
//                        url = 'https://opensource.org/licenses/MIT'
//                    }
//                }
//
//                developers {
//                    developer {
//                        id = 'bombardygamer'
//                        name = 'Callum Seabrook'
//                        email = 'callum.seabrook@prevarinite.com'
//                        timezone = 'Europe/London'
//                    }
//                }
//
//                scm {
//                    connection = 'scm:git:git://github.com/KryptonMC/Krypton.git'
//                    developerConnection = 'scm:git:ssh://github.com:KryptonMC/Krypton.git'
//                    url = 'https://github.com/KryptonMC/Krypton'
//                }
//            }
//        }
//    }
//}
//
//signing {
//    sign publishing.publications.mavenJava
//}