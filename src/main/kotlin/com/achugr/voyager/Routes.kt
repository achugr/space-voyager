import io.ktor.server.locations.*

object Routes {
    @Location("/homepage/app-has-permissions")
    object AppHasPermissions

    @Location("/api/graph")
    data class GetGraph(val query: String? = null)

    @Location("/api/graph")
    class GrabGraph()
}
