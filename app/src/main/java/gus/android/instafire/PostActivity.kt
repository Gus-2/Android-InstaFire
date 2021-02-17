package gus.android.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "PostActivity"

class PostActivity : AppCompatActivity() {

    private lateinit var fireStoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        fireStoreDb = FirebaseFirestore.getInstance()

        val postsReference = fireStoreDb.collection("posts")

        postsReference.addSnapshotListener{ snapshot, exception ->
            if(exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts !", exception)
                return@addSnapshotListener
            }

            for(document in snapshot.documents) {
                Log.i(TAG, "Document ${document.id}: ${document.data}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if( item.itemId == R.id.menu_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}