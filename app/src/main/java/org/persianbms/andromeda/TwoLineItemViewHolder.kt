package org.persianbms.andromeda

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class TwoLineItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val image: ImageView = itemView.findViewById(R.id.image)
    val primary: MaterialTextView = itemView.findViewById(R.id.primary)
    val secondary: MaterialTextView = itemView.findViewById(R.id.secondary)

}