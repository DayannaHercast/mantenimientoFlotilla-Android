package com.example.transandina_app.data.db

import com.example.transandina_app.BuildConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCrypto
import org.bouncycastle.tls.crypto.impl.jcajce.JcaTlsCryptoProvider
import java.security.SecureRandom
import java.security.Security
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConfig {
    private val SERVER: String = BuildConfig.DB_SERVER
    private const val PORT: Int = 1433
    private val DATABASE: String = BuildConfig.DB_NAME
    private val USER: String = BuildConfig.DB_USER
    private val PASSWORD: String = BuildConfig.DB_PASSWORD

    // Cadena de conexión para Azure SQL con cifrado TLS 1.2 usando BouncyCastle JSSE
    private val CONNECTION_URL: String
        get() = "jdbc:sqlserver://$SERVER:$PORT;databaseName=$DATABASE;user=$USER;password=$PASSWORD;encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1.2;loginTimeout=15;"

    init {
        setupSecurityProviders()
        disableDriverAssertions()
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun setupSecurityProviders() {
        try {
            if (Security.getProvider("BC") == null) {
                Security.insertProviderAt(BouncyCastleProvider(), 1)
            }
            if (Security.getProvider("BCJSSE") == null) {
                val customCrypto = object : JcaTlsCryptoProvider() {
                    override fun create(random: SecureRandom?): JcaTlsCrypto {
                        return super.create(random ?: SecureRandom())
                    }
                }
                Security.insertProviderAt(BouncyCastleJsseProvider(false, customCrypto), 2)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun disableDriverAssertions() {
        try {
            val cl = DatabaseConfig::class.java.classLoader
            cl?.setDefaultAssertionStatus(false)
            cl?.setPackageAssertionStatus("com.microsoft.sqlserver", false)
            ClassLoader.getSystemClassLoader()?.setDefaultAssertionStatus(false)
            ClassLoader.getSystemClassLoader()?.setPackageAssertionStatus("com.microsoft.sqlserver", false)

            val classes = listOf(
                "com.microsoft.sqlserver.jdbc.TDSReader",
                "com.microsoft.sqlserver.jdbc.TDSChannel",
                "com.microsoft.sqlserver.jdbc.TDSWriter",
                "com.microsoft.sqlserver.jdbc.SQLServerConnection",
                "com.microsoft.sqlserver.jdbc.SQLServerStatement"
            )
            for (className in classes) {
                try {
                    val clazz = Class.forName(className, false, cl)
                    val field = clazz.getDeclaredField("\$assertionsDisabled")
                    field.isAccessible = true
                    field.setBoolean(null, true)
                } catch (_: Throwable) {
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @Throws(SQLException::class)
    fun getConnection(): Connection {
        setupSecurityProviders()
        disableDriverAssertions()
        return DriverManager.getConnection(CONNECTION_URL)
    }
}
