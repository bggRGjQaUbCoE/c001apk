package com.example.c001apk.adapter

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.activity.CoolPicActivity
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.google.android.material.imageview.ShapeableImageView

class ImageSquareScrollCardAdapter(
    private val mContext: Context,
    private val imageSquareScrollCardList: List<HomeFeedResponse.Entities>
) :
    RecyclerView.Adapter<ImageSquareScrollCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val imageSquareScrollCard: ShapeableImageView =
            view.findViewById(R.id.imageSquareScrollCard)
        var url = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_image_square_scroll_card_item, parent, false)
        val padding =
            if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 78.dp
            else 95.dp
        val imageWidth =
            if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                DensityTool.getScreenWidth(parent.context) - padding
            else
                DensityTool.getScreenWidth(parent.context) / 2 - padding
        view.layoutParams.width = (imageWidth / 5)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            IntentUtil.startActivity<CoolPicActivity>(parent.context) {
                putExtra("title", viewHolder.title.text.toString().replace("#", ""))
            }
        }
        return viewHolder
    }

    override fun getItemCount() = imageSquareScrollCardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageSquareScrollCard = imageSquareScrollCardList[position]
        holder.title.text = imageSquareScrollCard.title
        ImageUtil.showUserCover(holder.imageSquareScrollCard, imageSquareScrollCard.pic)
    }

}