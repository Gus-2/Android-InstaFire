package gus.android.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import gus.android.models.Post
import gus.android.models.User
import kotlinx.android.synthetic.main.activity_post.*
import kotlin.math.sign

private const val TAG = "PostActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var fireStoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        Log.d(TAG, "repasse onCreate")
        // Create the layoyt file wich represent one post
        // Create data source
        posts = mutableListOf()
        // Create the adapter
        adapter = PostsAdapter(this, posts)
        // Bind the adapter and layout manager to the RV
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)

        fireStoreDb = FirebaseFirestore.getInstance()

        fabCreate.setOnClickListener{
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        refreshPosts()

        srlPosts.setOnRefreshListener {
            refreshPosts()
        }
    }

    fun refreshPosts() {

        var postsReference = fireStoreDb.collection("posts").limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        if(this::class.toString() == "class gus.android.instafire.ProfileActivity") {
            Log.d(TAG, "dedans")
            fireStoreDb.collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid as String)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedInUser = userSnapshot.toObject(User::class.java)

                    val username = signedInUser?.username

                    if (username != null) {
                        supportActionBar?.title = username
                        postsReference = postsReference.whereEqualTo("user.username", username)
                    }
                    displayRefreshedPosts(postsReference)
                }
                .addOnFailureListener{
                    Log.i(TAG, "Failure fetching signed in user", it)
                }
        } else {
            displayRefreshedPosts(postsReference)
        }
    }

    fun displayRefreshedPosts(postsReference: Query) {
        postsReference
            .get()
            .addOnFailureListener { exception ->
                Log.e(TAG, "Exception when querying posts !", exception)
                return@addOnFailureListener
            }.addOnSuccessListener { querySnapshot ->

                val postList = querySnapshot.toObjects(Post::class.java)
                posts.clear()
                posts.addAll(postList)
                adapter.notifyDataSetChanged()

                srlPosts.isRefreshing = false

            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}