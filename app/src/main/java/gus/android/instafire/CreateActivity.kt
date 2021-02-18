package gus.android.instafire

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import gus.android.models.Post
import gus.android.models.User
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.item_post.*

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1

class CreateActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private var signedInUser: User? = null
    private lateinit var fireStoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        storageReference = FirebaseStorage.getInstance().reference

        fireStoreDb = FirebaseFirestore.getInstance()

        fireStoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: ${signedInUser}")
            }
            .addOnFailureListener{
                Log.i(TAG, "Failure fetching signed in user", it)
            }

        btnAddImage.setOnClickListener{
            Log.i(TAG, "btnAddImageClicked !")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"

            if(imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnPost.setOnClickListener {
            handleOnPostButtonClick()
        }
    }

    private fun handleOnPostButtonClick() {
        if(photoUri == null) {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }

        if(etDescription.text.toString().isBlank()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if( signedInUser == null ) {
            Toast.makeText(this, "No signed user, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        btnPost.isEnabled = false
        // Upload photo to Firebase Storage
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        photoReference
            .putFile(photoUri as Uri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                // Retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                // Create a post object with the image URL and add that to the posts collection
                var post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser
                )
                fireStoreDb
                    .collection("posts")
                    .add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnPost.isEnabled = true
                if(!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception during Firebase Operation", postCreationTask.exception)
                    Toast.makeText(this, "Failed to publish the post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                ivPicture.setImageResource(0)
                Toast.makeText(this, "Success !", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                // Removing activity from the back stack
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_PHOTO_CODE) {
            if(resultCode == Activity.RESULT_OK ) {
                photoUri = data?.data
                Log.i(TAG, "photoUri: $photoUri")
                ivPicture.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
            }
        }

    }

}