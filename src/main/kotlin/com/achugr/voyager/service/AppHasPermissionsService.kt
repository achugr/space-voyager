import com.achugr.voyager.SpaceTokenInfo
import com.achugr.voyager.appSpaceClient
import kotlinx.serialization.Serializable
import space.jetbrains.api.runtime.resources.permissions
import space.jetbrains.api.runtime.types.ApplicationIdentifier
import space.jetbrains.api.runtime.types.GlobalPermissionTarget
import space.jetbrains.api.runtime.types.PrincipalIn

class AppHasPermissionsService(private val spaceTokenInfo: SpaceTokenInfo) {
    suspend fun appHasPermissions(): AppHasPermissionsResponse {
        val hasMeetingsPermission = spaceTokenInfo.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            "Meeting.View",
            target = GlobalPermissionTarget
        )
        val hasProfilesPermission = spaceTokenInfo.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            "Profile.View",
            target = GlobalPermissionTarget
        )
        val hasCodeReviewPermission = spaceTokenInfo.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            "Project.CodeReview.View",
            target = GlobalPermissionTarget
        )

        val hasProjectPermission = spaceTokenInfo.appSpaceClient().permissions.checkPermission(
            principal = PrincipalIn.Application(ApplicationIdentifier.Me),
            "Project.View",
            target = GlobalPermissionTarget
        )

        return AppHasPermissionsResponse(
            hasProfilesPermission && hasMeetingsPermission
                    && hasCodeReviewPermission && hasProjectPermission
        )
    }
}

@Serializable
data class AppHasPermissionsResponse(
    val hasPermissions: Boolean
)
