package com.mmuhamadamirzaidi.sellynappserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mmuhamadamirzaidi.sellynappserver.Common.Common;
import com.mmuhamadamirzaidi.sellynappserver.Interface.ItemClickListener;
import com.mmuhamadamirzaidi.sellynappserver.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

//    public ImageView product_image, product_wishlist, product_share, product_quick_cart;
    public ImageView product_image;
    public TextView product_name, product_notification;

    private ItemClickListener itemClickListener;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        product_image = (ImageView) itemView.findViewById(R.id.product_image);

//        product_quick_cart = (ImageView) itemView.findViewById(R.id.product_quick_cart);
//        product_wishlist = (ImageView) itemView.findViewById(R.id.product_wishlist);
//        product_share = (ImageView) itemView.findViewById(R.id.product_share);

        product_name = (TextView) itemView.findViewById(R.id.product_name);
        product_notification = (TextView) itemView.findViewById(R.id.product_notification);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");

        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
