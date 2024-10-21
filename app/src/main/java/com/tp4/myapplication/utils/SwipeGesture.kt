package com.tp4.myapplication.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.tp4.myapplication.R
import com.tp4.myapplication.adapter.StudentAdapter

class SwipeGesture(
    private val adapter: StudentAdapter
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private lateinit var deleteIcon: Drawable
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int

    private val backgroundPaint: Paint = Paint().apply {
        color = Color.RED
    }

    init {
        deleteIcon = adapter.context.getDrawable(R.drawable.baseline_delete_2)!! // Get the trash icon drawable
        intrinsicWidth = deleteIcon.intrinsicWidth
        intrinsicHeight = deleteIcon.intrinsicHeight
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.showDeleteConfirmationDialog(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView


        if (dX > 0) {
            c.drawRect(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                dX,
                itemView.bottom.toFloat(),
                backgroundPaint
            )

            val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
            val iconBottom = iconTop + intrinsicHeight
            val iconLeft = itemView.left + 16
            val iconRight = iconLeft + intrinsicWidth

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon.draw(c)

        } else if (dX < 0) {
            c.drawRect(
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                backgroundPaint
            )

            val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
            val iconBottom = iconTop + intrinsicHeight
            val iconRight = itemView.right - 16
            val iconLeft = iconRight - intrinsicWidth

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}
