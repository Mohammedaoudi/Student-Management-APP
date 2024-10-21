package com.tp4.myapplication.adapter

import Student
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.tp4.myapplication.List_Student
import com.tp4.myapplication.R
import com.tp4.myapplication.EditStudentActivity
import com.tp4.myapplication.utils.SwipeGesture
import java.io.Serializable
import java.util.Timer
import kotlin.concurrent.schedule

class StudentAdapter(val context: Context, var studentList: ArrayList<Student>) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    private var previouslyDeletedStudent: Student? = null
    private var previouslyDeletedPosition: Int = -1

    private var deletionTimer: Timer? = null


    var fullStudentList: ArrayList<Student> = ArrayList(studentList)

    fun updateList(newList: ArrayList<Student>) {
        studentList = newList
        notifyDataSetChanged()
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val EDIT_STUDENT_REQUEST:Int = 2
        const val UNDO_DELAY: Long = 3000

    }

    fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete")
            .setIcon(R.drawable.baseline_warning_24)
            .setMessage("Are you sure you want to delete this information?")
            .setPositiveButton("Yes") { dialog, _ ->
                val studentId = studentList[position].id
                deleteStudent(studentId, position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun deleteStudent(studentId: Int, position: Int) {
        val studentToDelete = studentList[position]
        previouslyDeletedStudent = studentToDelete
        previouslyDeletedPosition = position

        studentList.removeAt(position)
        notifyItemRemoved(position)

        showUndoSnackbar(studentToDelete, position)

        deletionTimer = Timer()
        deletionTimer?.schedule(UNDO_DELAY) {
            deleteFromDatabase(studentId)
            (context as? List_Student)?.fetchStudentStats() // Update stats

        }
    }



    private fun deleteFromDatabase(studentId: Int) {
        val url = "http://160.168.0.64:4000/api/v1/students/$studentId"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(
            Request.Method.DELETE,
            url,
            { response ->
                Log.d("StudentAdapter", "Delete Response: $response")
                (context as? List_Student)?.fetchStudentStats()
            },
            { error ->
                Log.e("StudentAdapter", "Delete Error: ${error.message}")
                Toast.makeText(context, "Failed to delete student", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(stringRequest)
    }


    private fun showUndoSnackbar(student: Student, position: Int) {
        val snackbar = Snackbar.make((context as List_Student).findViewById(android.R.id.content),
            "Student deleted", Snackbar.LENGTH_LONG)

        snackbar.setAction("UNDO") {
            deletionTimer?.cancel()
            deletionTimer = null

            studentList.add(position, student)
            notifyItemInserted(position)
            Toast.makeText(context, "Student restored", Toast.LENGTH_SHORT).show()
            (context as? List_Student)?.fetchStudentStats()

        }

        snackbar.show()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nom: TextView = itemView.findViewById(R.id.fullname)
        val ville: TextView = itemView.findViewById(R.id.ville)
        val filiere: TextView = itemView.findViewById(R.id.filiere)
        val id: TextView = itemView.findViewById(R.id.id)
        val img: ImageView = itemView.findViewById(R.id.image)
        private val menuButton: ImageView = itemView.findViewById(R.id.mMenus)

        init {
            menuButton.setOnClickListener { showPopupMenu(it) }
        }

        private fun showPopupMenu(view: View) {
            val position = adapterPosition
            val popupMenu = PopupMenu(context, view).apply {
                inflate(R.menu.show_menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.editText -> {
                            showEditDialog(position)
                            true
                        }
                        R.id.delete -> {
                            showDeleteConfirmationDialog(position)
                            true
                        }
                        else -> true
                    }
                }
                setForceShowIcon(this)
            }
            popupMenu.show()
        }

        private fun setForceShowIcon(popupMenu: PopupMenu) {
            try {
                val menuField = PopupMenu::class.java.getDeclaredField("mPopup")
                menuField.isAccessible = true
                val menu = menuField.get(popupMenu)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menu, true)
            } catch (e: Exception) {
                Log.e("StudentAdapter", "Error forcing icons to show: ${e.message}")
            }
        }

        private fun showEditDialog(position: Int) {
            val student = studentList[position]
            val editIntent = Intent(context, EditStudentActivity::class.java).apply {
                putExtra("student", student as Serializable)
            }
            (context as? List_Student)?.startActivityForResult(editIntent, EDIT_STUDENT_REQUEST)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = studentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        val imageUrl = "http://160.168.0.64:4000/${student.image.trimStart('/')}"

        if (!student.image.isNullOrEmpty()) {
            Glide.with(holder.img.context)
                .load(imageUrl)
                .skipMemoryCache(true)

                .into(holder.img)
        } else {
            holder.img.setImageResource(R.drawable.prfl)
        }

        holder.nom.text = "${student.nom.uppercase()} ${student.prenom}"
        holder.ville.text = student.ville
        holder.filiere.text = student.filiere
        holder.id.text = student.id.toString()
    }

    fun attachSwipeToDelete(recyclerView: RecyclerView) {
        val swipeGesture = SwipeGesture(this)
        val itemTouchHelper = ItemTouchHelper(swipeGesture)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

}
