import org.jooq.meta.jaxb.Logging

plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jooq.jooq-codegen-gradle") version "3.19.28"
}

group = "com.ercode"
version = "0.1.0-SNAPSHOT"
description = "Product Description Service"

java {
    // Single-line seam for bumping the JDK later (25/26). The toolchain — not the
    // ambient JDK — is the source of truth for compilation and test execution.
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// Core langchain4j is published on the stable 1.x number; the Spring Boot starters and other
// not-yet-frozen modules are published on the parallel beta line (same base version).
val langchain4jVersion = "1.18.1"
val langchain4jStarterVersion = "1.18.1-beta28"

dependencies {
    // --- Spring Boot 4 (Jackson 3 provided transitively; do NOT add explicit Jackson deps) ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // --- Persistence: jOOQ (Boot manages 3.19.28) over PostgreSQL, schema owned by Liquibase ---
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    // --- LLM: langchain4j + OpenAI-compatible endpoint (MUST be the spring-boot4 starter variant) ---
    implementation("dev.langchain4j:langchain4j-open-ai-spring-boot4-starter:$langchain4jStarterVersion")
    implementation("dev.langchain4j:langchain4j:$langchain4jVersion")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    // Testcontainers 2.x (Boot-managed 2.0.2) renamed the module artifacts to a testcontainers-* prefix.
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    // Standalone (shaded) WireMock — its bundled Jackson is relocated, so it cannot clash with Boot's Jackson 3.
    testImplementation("org.wiremock:wiremock-standalone:3.13.1")

    // --- jOOQ code generation classpath (offline: LiquibaseDatabase runs the changelog on in-memory H2) ---
    jooqCodegen("org.jooq:jooq-meta-extensions-liquibase:3.19.28")
    // Align the codegen-time Liquibase parser with the runtime Liquibase (5.0.1).
    jooqCodegen("org.liquibase:liquibase-core:5.0.1")
}

jooq {
    configuration {
        logging = Logging.WARN
        generator {
            database {
                // No JDBC/Docker needed: parse the Liquibase changelog into an in-memory H2 schema.
                name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                properties {
                    // Resolve changelog includes from the resources dir on the filesystem.
                    property {
                        key = "rootPath"
                        value = "$projectDir/src/main/resources"
                    }
                    property {
                        key = "scripts"
                        value = "db/changelog/db.changelog-master.xml"
                    }
                    property {
                        key = "includeLiquibaseTables"
                        value = "false"
                    }
                    // Route DDL through jOOQ's parser so Postgres-isms translate to H2 where possible.
                    property {
                        key = "useParsingConnection"
                        value = "true"
                    }
                }
            }
            target {
                packageName = "com.ercode.productdescription.adapter.out.persistence.jooq"
                directory = "build/generated-src/jooq/main"
            }
        }
    }
}

// Add the generated jOOQ sources to the main source set and make compilation depend on codegen.
sourceSets {
    named("main") {
        java.srcDir(layout.buildDirectory.dir("generated-src/jooq/main"))
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("jooqCodegen"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
