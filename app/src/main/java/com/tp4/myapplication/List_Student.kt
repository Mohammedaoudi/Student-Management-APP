package com.tp4.myapplication

import Student
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.projetws.util.VolleyMultipartRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tp4.myapplication.adapter.StudentAdapter
import com.tp4.myapplication.data.FiliereStats
import com.tp4.myapplication.utils.SwipeGesture
import java.io.InputStream

class List_Student : AppCompatActivity() {

    private lateinit var textViewFiliere1: TextView
    private lateinit var textViewFiliere2: TextView
    private lateinit var textViewFiliere3: TextView

    private lateinit var tabHost: TabHost
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private var studentsList: ArrayList<Student> = ArrayList()
    private lateinit var addBtn: FloatingActionButton
    private var menu: Menu? = null

    private val PICK_IMAGE_REQUEST = 1
    private val EDIT_STUDENT_REQUEST = 2

    private var selectedImageUri: Uri? = null

    private val getUrl = "http://160.168.0.64:4000/api/v1/students"
    private val TAG = "volley get"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_student)

        textViewFiliere1 = findViewById(R.id.textViewFiliere1)
        textViewFiliere2 = findViewById(R.id.textViewFiliere2)
        textViewFiliere3 = findViewById(R.id.textViewFiliere3)

        tabHost = findViewById(R.id.tabHost)
        tabHost.setup()

        val homeTab = tabHost.newTabSpec("Home")
            .setIndicator("Home")
            .setContent(R.id.homeTab)
        tabHost.addTab(homeTab)

        val statsTab = tabHost.newTabSpec("Stats")
            .setIndicator("Stats")
            .setContent(R.id.statsTab)
        tabHost.addTab(statsTab)

        recyclerView = findViewById(R.id.recyclerView)
        addBtn = findViewById(R.id.addButton)
        recyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(this, studentsList)
        recyclerView.adapter = studentAdapter
        studentAdapter.attachSwipeToDelete(recyclerView)

        fetchStudents()
        fetchStudentStats()

        val swipeGesture = SwipeGesture(studentAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeGesture)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        addBtn.setOnClickListener { addInfo() }

        tabHost.setOnTabChangedListener { tabId ->
            addBtn.visibility = if (tabId == "Home") View.VISIBLE else View.GONE
            showSearchMenuItem(tabId == "Home")
        }

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    fun fetchStudentStats() {
        val statsUrl = "http://160.168.0.64:4000/api/v1/students/count-per-filiere"
        val reqQueue: RequestQueue = Volley.newRequestQueue(this)

        val request = JsonArrayRequest(Request.Method.GET, statsUrl, null, { response ->
            Log.d(TAG, "Stats Response: $response")

            val gson = Gson()
            val statsListType = object : TypeToken<List<FiliereStats>>() {}.type
            val stats: List<FiliereStats> = gson.fromJson(response.toString(), statsListType)

            val filiereCounts = mutableMapOf<String, Int>(
                "2ITE" to 0,
                "ISIC" to 0,
                "CCN" to 0
            )

            for (stat in stats) {
                filiereCounts[stat.filiere] = stat.count.toInt()
            }

            textViewFiliere1.text = "Filiere 2ITE: ${filiereCounts["2ITE"]}"
            textViewFiliere2.text = "Filiere ISIC: ${filiereCounts["ISIC"]}"
            textViewFiliere3.text = "Filiere CCN: ${filiereCounts["CCN"]}"

        }, { error ->
            Log.e(TAG, "Error fetching student stats: ${error.message}")
            Toast.makeText(this, "Failed to fetch student stats", Toast.LENGTH_SHORT).show()
        })

        reqQueue.add(request)
    }


    private fun fetchStudents() {
        val reqQueue: RequestQueue = Volley.newRequestQueue(this)

        val request = JsonArrayRequest(Request.Method.GET, getUrl, null, { response ->
            Log.d(TAG, "Response: $response")

            val jsonString = response.toString()
            val gson = Gson()
            val studentListType = object : TypeToken<List<Student>>() {}.type
            val students: List<Student> = gson.fromJson(jsonString, studentListType)

            studentsList.clear()
            studentsList.addAll(students)

            studentAdapter.updateList(ArrayList(students))
            studentAdapter.fullStudentList = ArrayList(students)

            studentAdapter.notifyDataSetChanged()

        }, { error ->
            Log.e(TAG, "Error: ${error.toString()}")
            Toast.makeText(this, "Failed to fetch students", Toast.LENGTH_SHORT).show()
        })

        request.retryPolicy = DefaultRetryPolicy(
            10 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        reqQueue.add(request)
    }

    private var imageViewToUpdate: ImageView? = null

    private fun addInfo() {
        val inflater = LayoutInflater.from(this)
        val v = inflater.inflate(R.layout.add_student, null)
        imageViewToUpdate = v.findViewById(R.id.imageViewStudent)

        val nom = v.findViewById<EditText>(R.id.editTextNom)
        val prenom = v.findViewById<EditText>(R.id.editTextPrenom)
        val ville = v.findViewById<EditText>(R.id.editTextVille)
        val sexeGroup = v.findViewById<RadioGroup>(R.id.radioBtns)
        var selectedSexe = ""
        sexeGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedSexe = when (checkedId) {
                R.id.radioMale -> "M"
                R.id.radioFemale -> "F"
                else -> ""
            }
        }

        val filiereSpinner = v.findViewById<Spinner>(R.id.spinnerFiliere)
        val dialogImageView = v.findViewById<ImageView>(R.id.imageViewStudent)
        val buttonSelectImage = v.findViewById<Button>(R.id.buttonSelectImage)

        buttonSelectImage.setOnClickListener {
            selectImageForAdd()
        }

        selectedImageUri?.let {
            dialogImageView.setImageURI(it)
        }

        val addDialog = AlertDialog.Builder(this)
        addDialog.setView(v)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            val studentNom = nom.text.toString()
            val studentPrenom = prenom.text.toString()
            val studentVille = ville.text.toString()
            val selectedFiliere = filiereSpinner.selectedItem.toString()

            if (selectedImageUri != null) {
                val student = Student(
                    id = 0,
                    nom = studentNom,
                    prenom = studentPrenom,
                    ville = studentVille,
                    sexe = selectedSexe,
                    filiere = selectedFiliere,
                    image = selectedImageUri.toString()
                )

                sendPostRequest(student)
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        addDialog.create()
        addDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            imageViewToUpdate?.setImageURI(selectedImageUri)
        } else if (requestCode == StudentAdapter.EDIT_STUDENT_REQUEST && resultCode == RESULT_OK) {
            val updatedStudent = data?.getSerializableExtra("updatedStudent") as? Student
            updatedStudent?.let {
                val index = studentAdapter.studentList.indexOfFirst { student -> student.id == it.id }
                if (index != -1) {
                    studentAdapter.studentList[index] = it
                    studentAdapter.notifyItemChanged(index)
                }
            }
            fetchStudentStats()
        }
    }

    fun selectImageForAdd() {
        selectedImageUri = null
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun sendPostRequest(student: Student) {
        val url = "http://160.168.0.64:4000/api/v1/students/create"
        val requestQueue = Volley.newRequestQueue(this)

        val multipartRequest = object : VolleyMultipartRequest(
            Request.Method.POST,
            url,
            { response ->
                Log.d(TAG, "Response: $response")
                Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                fetchStudents()
                fetchStudentStats()
            },
            { error ->
                Log.e(TAG, "Error: ${error.toString()}")
                Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String>? {
                return mapOf(
                    "nom" to student.nom,
                    "prenom" to student.prenom,
                    "ville" to student.ville,
                    "sexe" to student.sexe,
                    "filiere" to student.filiere
                )
            }

            override fun getByteData(): Map<String, DataPart>? {
                val imageData = selectedImageUri?.let { uriToByteArray(it) }
                return if (imageData != null) {
                    val mimeType = contentResolver.getType(selectedImageUri!!)
                    val fileName = "${student.filiere}_${student.nom}.${mimeType?.split('/')?.get(1)}"

                    mapOf("image" to DataPart(fileName, imageData, mimeType!!))
                } else {
                    null
                }
            }
        }

        multipartRequest.retryPolicy = DefaultRetryPolicy(
            10 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(multipartRequest)
    }

    private fun uriToByteArray(uri: Uri): ByteArray {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        return inputStream?.readBytes() ?: ByteArray(0)
    }

    private fun showSearchMenuItem(isVisible: Boolean) {
        menu?.findItem(R.id.search)?.isVisible = isVisible
    }

    private fun performSearch(query: String?) {
        if (query.isNullOrEmpty()) {
            studentAdapter.updateList(ArrayList(studentsList))
        } else {
            val filteredList = studentsList.filter { student ->
                student.nom.lowercase().contains(query.lowercase()) ||
                        student.prenom.lowercase().contains(query.lowercase())
            }
            studentAdapter.updateList(ArrayList(filteredList))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return true
            }
        })
        return true
    }

}
