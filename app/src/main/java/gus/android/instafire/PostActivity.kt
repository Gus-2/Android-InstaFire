package gus.android.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import gus.android.models.Post
import gus.android.models.User
import kotlinx.android.synthetic.main.activity_post.*
import kotlin.math.sign

private const val TAG = "PostActivity"
private const val LIMIT_POSTS_LOADED: Long = 3
const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private var postList: MutableList<Post> = mutableListOf()
    private lateinit var fireStoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isLoading = false
    private var lastVisible: DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        Log.d(TAG, "repasse onCreate")
        // Create the layout file which represent one post
        // Create data source
        posts = mutableListOf()
        // Create the adapter
        adapter = PostsAdapter(this, posts)
        // Bind the adapter and layout manager to the RV
        rvPosts.adapter = adapter
        layoutManager = LinearLayoutManager(this)
        rvPosts.layoutManager = layoutManager

        rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val pastVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
                    val total = adapter.itemCount

                    Log.d(TAG, "" + visibleItemCount + " " + pastVisibleItem + " " + total)
                    if (!isLoading) {
                        if ((visibleItemCount + pastVisibleItem) >= total) {
                            pgPosts.isVisible = true
                            refreshPosts()
                        }
                    }
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })

        fireStoreDb = FirebaseFirestore.getInstance()

        fabCreate.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        refreshPosts()

        srlPosts.setOnRefreshListener {
            refreshPosts()
        }
    }

    fun refreshPosts() {
        isLoading = true
        var postsReference: Query

        postsReference = if (lastVisible == null) {
            fireStoreDb
                .collection("posts")
                .orderBy( "creation_time_ms", Query.Direction.DESCENDING)
                .limit(LIMIT_POSTS_LOADED)
        } else {
            fireStoreDb
                .collection("posts")
                .orderBy("creation_time_ms", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!.data?.get("creation_time_ms"))
                .limit(LIMIT_POSTS_LOADED)
        }

        if (this::class.toString() == "class gus.android.instafire.ProfileActivity") {

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
                .addOnFailureListener {
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
                if(querySnapshot.size() != 0 ) {
                    lastVisible = querySnapshot.documents[querySnapshot.size() - 1]

                    var postRetrieved: List<Post>  = querySnapshot.toObjects(Post::class.java)

                    postList.clear()
                    posts.addAll(postRetrieved)
                    adapter.notifyDataSetChanged()

                    srlPosts.isRefreshing = false
                    isLoading = false
                } else {
                    Toast.makeText(this, "No more posts !", Toast.LENGTH_SHORT).show()
                }
                pgPosts.isVisible = false

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