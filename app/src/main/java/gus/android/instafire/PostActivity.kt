package gus.android.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import gus.android.models.Post

private const val TAG = "PostActivity"

class PostActivity : AppCompatActivity() {

    private lateinit var fireStoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Create the layoyt file wich represent one post
        // Create data source
        // Create the adapter
        // Bind the adapter and layout manager to the RV

        fireStoreDb = FirebaseFirestore.getInstance()

        val postsReference = fireStoreDb.collection("posts").limit(20).orderBy("creation_time_ms", Query.Direction.DESCENDING)

        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts !", exception)
                return@addSnapshotListener
            }

            val postList = snapshot.toObjects(Post::class.java)

            for (post in postList) {
                Log.i(TAG, "Post $post")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}