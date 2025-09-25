import com.tomo.memento.UploadResponse
import com.tomo.memento.Post
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.GET

interface ApiService {
    @Multipart
    @POST("upload")
    fun uploadPost(
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("user_uid") user_uid: RequestBody
    ): Call<UploadResponse>
    @GET("posts")
    fun getPosts(): Call<List<Post>>
    @POST("users")
    fun createUser(@Body user: UserRequest): Call<CreateUserResponse>
}

data class UserRequest(
    val user_uid: String,
    val username: String,
    val profile_photo: String? = null,
    val bio: String? = null
)
data class CreateUserResponse(val ok: Boolean, val user: User?)
data class User(val user_uid: String, val username: String)
