package com.tarkvaratehnika.demobackend.config

import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.webeid.security.exceptions.JceException
import org.webeid.security.nonce.NonceGenerator
import org.webeid.security.nonce.NonceGeneratorBuilder
import org.webeid.security.validator.AuthTokenValidator
import org.webeid.security.validator.AuthTokenValidatorBuilder
import java.io.IOException
import java.net.URI
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.cache.Cache
import javax.cache.CacheManager
import javax.cache.Caching
import javax.cache.configuration.CompleteConfiguration
import javax.cache.configuration.FactoryBuilder
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration

import javax.cache.configuration.FactoryBuilder.factoryOf

@Configuration
class ValidationConfiguration {

    private val LOG: Logger = LoggerFactory.getLogger(ValidationConfiguration::class.java)

    private val NONCE_TTL_MINUTES: Long = 5
    private val CACHE_NAME = "nonceCache"
    private val CERTS_RESOURCE_PATH = "/certs/"
    private val TRUSTED_CERTIFICATES_JKS = "trusted_certificates.jks"
    private val TRUSTSTORE_PASSWORD = "changeit"
    companion object {
        const val ROLE_USER : String = "ROLE_USER"
    }

    init {
        LOG.warn("Creating new ValidationConfiguration.")
    }

    @Bean
    fun cacheManager(): CacheManager {
        return Caching.getCachingProvider(CaffeineCachingProvider::class.java.name).cacheManager
    }

    @Bean
    fun nonceCache(): Cache<String, ZonedDateTime>? {
        val cacheManager: CacheManager = cacheManager()
        var cache =
            cacheManager.getCache<String?, ZonedDateTime?>(CACHE_NAME)

        if (cache == null) {
            LOG.warn("Creating new cache.")
            cache = createNonceCache(cacheManager)
        }
        return cache
    }

    @Bean
    fun generator(): NonceGenerator? {
        return NonceGeneratorBuilder()
            .withNonceTtl(java.time.Duration.ofMinutes(NONCE_TTL_MINUTES))
            .withNonceCache(nonceCache())
            .build()
    }

    private fun createNonceCache(cacheManager: CacheManager): Cache<String?, ZonedDateTime?>? {
        val cacheConfig: CompleteConfiguration<String, ZonedDateTime> = MutableConfiguration<String, ZonedDateTime>()
            .setTypes(String::class.java, ZonedDateTime::class.java)
            .setExpiryPolicyFactory(
                factoryOf(
                    CreatedExpiryPolicy(
                        Duration(
                            TimeUnit.MINUTES,
                            NONCE_TTL_MINUTES + 1
                        )
                    )
                )
            )
        return cacheManager.createCache(CACHE_NAME, cacheConfig)
    }

    @Bean
    fun loadTrustedCACertificatesFromCerFiles() : Array<X509Certificate> {
        val caCertificates = ArrayList<X509Certificate>()

        try {
            val certFactory = CertificateFactory.getInstance("X.509")
            val resolver = PathMatchingResourcePatternResolver()
            val resources = resolver.getResources("$CERTS_RESOURCE_PATH/*.cer")

            resources.forEach { resource ->
                val caCertificate = certFactory.generateCertificate(resource.inputStream) as X509Certificate
                caCertificates.add(caCertificate)
            }
        } catch (e : Exception) {
            when (e){
                is CertificateException, is IOException -> {
                    throw RuntimeException("Error initializing trusted CA certificates. $e")
                }
            }
        }
        return caCertificates.toTypedArray()
    }

    @Bean
    fun loadTrustedCACertificatesFromTrustStore() : Array<X509Certificate> {
        val caCertificates = ArrayList<X509Certificate>()

        ValidationConfiguration::class.java.getResourceAsStream("$CERTS_RESOURCE_PATH/$TRUSTED_CERTIFICATES_JKS").use { inputStream ->
            try {
                if (inputStream == null) {
                    // No truststore files found.
                    return arrayOf()
                }

                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(inputStream, TRUSTSTORE_PASSWORD.toCharArray())
                val aliases = keyStore.aliases()

                while (aliases.hasMoreElements()) {
                    val alias = aliases.nextElement()
                    val certificate = keyStore.getCertificate(alias) as X509Certificate
                    caCertificates.add(certificate)
                }


            } catch (e : Exception) {
                when (e) {
                    is IOException, is CertificateException, is KeyStoreException, is NoSuchAlgorithmException -> {
                        throw RuntimeException("Error initializing trusted CA certificates from trust store. $e")
                    }
                }
            }
        }

        return caCertificates.toTypedArray()
    }

    @Bean
    fun validator() : AuthTokenValidator {
        try {
            return AuthTokenValidatorBuilder()
                .withSiteOrigin(URI.create(ApplicationConfiguration.WEBSITE_ORIGIN_URL))
                .withNonceCache(nonceCache())
                .withTrustedCertificateAuthorities(*loadTrustedCACertificatesFromCerFiles())
                .withTrustedCertificateAuthorities(*loadTrustedCACertificatesFromTrustStore())
                .build()
        } catch (e : JceException) {
            throw RuntimeException("Error building the Web eID auth token validator.", e)
        }
    }


}