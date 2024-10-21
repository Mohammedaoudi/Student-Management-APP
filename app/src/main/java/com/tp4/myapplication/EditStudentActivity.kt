package com.tp4.myapplication

import Student
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.projetws.util.VolleyMultipartRequest
import com.tp4.myapplication.adapter.StudentAdapter.Companion.PICK_IMAGE_REQUEST
class EditStudentActivity : AppCompatActivity() {

    private lateinit var editNom: EditText
    private lateinit var editPrenom: EditText
    private lateinit var editVille: EditText
    private lateinit var editFiliere: Spinner
    private lateinit var radioBtns: RadioGroup
    private lateinit var dialogImageView: ImageView
    private lateinit var buttonSelectImage: Button

    private var student: Student? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_student)

        editNom = findViewById(R.id.editTextNom)
        editPrenom = findViewById(R.id.editTextPrenom)
        editVille = findViewById(R.id.editTextVille)
        editFiliere = findViewById(R.id.spinnerFiliere)
        radioBtns = findViewById(R.id.radioBtns)
        dialogImageView = findViewById(R.id.imageViewStudent)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        student = intent.getSerializableExtra("student") as? Student
            ?: throw IllegalArgumentException("Student must be passed")

        student?.let {
            editNom.setText(it.nom)
            editPrenom.setText(it.prenom)
            editVille.setText(it.ville)
            editFiliere.setSelection(getFilierePosition(it.filiere))
            radioBtns.check(if (it.sexe == "M") R.id.radioMale else R.id.radioFemale)

            val imageUrl = "http://160.168.0.64:4000/${it.image.trimStart('/')}"
            Glide.with(this).load(imageUrl).into(dialogImageView)
        }

        buttonSelectImage.setOnClickListener { selectImage() }

        findViewById<Button>(R.id.buttonSave).setOnClickListener { updateStudent() }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun updateStudent() {
        student?.let {

            it.nom = editNom.text.toString()
            it.prenom = editPrenom.text.toString()
            it.ville = editVille.text.toString()
            it.filiere = editFiliere.selectedItem.toString()
            it.sexe = if (radioBtns.checkedRadioButtonId == R.id.radioMale) "M" else "F"

            val url = "http://160.168.0.64:4000/api/v1/students/${it.id}"
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            Log.d("EditStudentActivity", "Updating student: ${it.nom}, ${it.prenom}, ${it.ville}, ${it.filiere}, ${it.sexe}")

            val multipartRequest = object : VolleyMultipartRequest(
                Request.Method.PUT,
                url,
                { response ->
                    Log.d("EditStudentActivity", "Update Response: $response")
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()

                    // Return updated student
                    val resultIntent = Intent().apply {
                        putExtra("updatedStudent", it)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
                { error ->
                    Log.e("EditStudentActivity", "Update Error: ${error.message}")
                    Toast.makeText(this, "Failed to update student", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "nom" to it.nom,
                        "prenom" to it.prenom,
                        "ville" to it.ville,
                        "sexe" to it.sexe,
                        "filiere" to it.filiere
                    )
                }

                override fun getByteData(): Map<String, DataPart>? {
                    return if (selectedImageUri != null) {
                        val imageData = uriToByteArray(selectedImageUri!!)
                        Log.d("EditStudentActivity", "Image data size: ${imageData?.size}")
                        val mimeType = contentResolver.getType(selectedImageUri!!)
                        val dep = "/api/v1/students/uploads/"
                        val fileName1 = "${it.filiere}_${it.nom}.${mimeType?.split('/')?.get(1)}"
                        val fileName = "${it.filiere}_${it.nom}"

                        it.image = dep +fileName+"_"+fileName

                        Log.d("EditStudentActivity", "Uploading image: $fileName, MIME type: $mimeType")
                        mapOf("image" to DataPart(fileName, imageData!!, mimeType ?: "image/png"))
                    } else {
                        Log.e("EditStudentActivity", "No new image selected for upload")
                        emptyMap()
                    }
                }
            }

            requestQueue.add(multipartRequest)
        }
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        val inputStream = contentResolver.openInputStream(uri)
        return inputStream?.readBytes()
    }

    private fun getFilierePosition(filiere: String?): Int {
        val filiereArray = resources.getStringArray(R.array.filieres)
        return filiereArray.indexOf(filiere)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                student?.image = uri.toString()
                Glide.with(this).load(uri).into(dialogImageView)
            }
        }
    }
}
