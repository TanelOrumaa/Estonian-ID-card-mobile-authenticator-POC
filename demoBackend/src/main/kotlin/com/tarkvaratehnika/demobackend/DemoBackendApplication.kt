package com.tarkvaratehnika.demobackend

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude=[SecurityAutoConfiguration::class])
class DemoBackendApplication

fun main(args: Array<String>) {
    runApplication<DemoBackendApplication>(*args)
}
