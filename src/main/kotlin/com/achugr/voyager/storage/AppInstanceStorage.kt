package jvmMain.space.jetbrains.api.runtime.helpers

import com.achugr.voyager.config
import com.google.api.gax.rpc.NotFoundException
import com.google.cloud.secretmanager.v1.*
import com.google.protobuf.ByteString
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.jetbrains.api.runtime.SpaceAppInstance
import space.jetbrains.api.runtime.helpers.SpaceAppInstanceStorage
import java.nio.charset.Charset

class SecureGcloudAppInstanceStorage(private val projectId: String) :
    SpaceAppInstanceStorage {

    override suspend fun loadAppInstance(clientId: String): SpaceAppInstance? {
        return SecretManagerServiceClient.create().use { client ->
            try {
                client.accessSecretVersion(SecretVersionName.of(projectId, clientId, "latest"))?.let {
                    map(Json.decodeFromString<SpaceAppInstanceModel>(it.payload.data.toStringUtf8()))
                }
            } catch (e: NotFoundException) {
                return null
            }
        }
    }

    override suspend fun saveAppInstance(appInstance: SpaceAppInstance) {
        SecretManagerServiceClient.create().use { client ->
            val projectName: ProjectName =
                ProjectName.of(projectId)

            val secret: Secret = Secret.newBuilder()
                .setReplication(
                    Replication.newBuilder()
                        .setAutomatic(Replication.Automatic.newBuilder().build())
                        .build()
                )
                .build()
            val createdSecret: Secret =
                client.createSecret(projectName, appInstance.clientId, secret)

            val payload: SecretPayload =
                SecretPayload.newBuilder()
                    .setData(ByteString.copyFrom(Json.encodeToString(map(appInstance)), Charset.defaultCharset()))
                    .build()
            val addedVersion: SecretVersion =
                client.addSecretVersion(createdSecret.getName(), payload)

            client.accessSecretVersion(addedVersion.getName())
        }
    }
}

@Serializable
data class SpaceAppInstanceModel(
    val clientId: String,
    val clientSecret: String,
    val spaceServerUrl: String
)

fun map(app: SpaceAppInstance): SpaceAppInstanceModel =
    SpaceAppInstanceModel(app.clientId, app.clientSecret, app.spaceServer.serverUrl)

fun map(app: SpaceAppInstanceModel): SpaceAppInstance =
    SpaceAppInstance(app.clientId, app.clientSecret, app.spaceServerUrl)


val appInstanceStorage: SpaceAppInstanceStorage by lazy {
    if (config.getString("mode") == "local") {
        return@lazy SecureGcloudAppInstanceStorage(config.getString("gcp.project"))
    } else {
        return@lazy SecureGcloudAppInstanceStorage(config.getString("gcp.project"))
    }
}