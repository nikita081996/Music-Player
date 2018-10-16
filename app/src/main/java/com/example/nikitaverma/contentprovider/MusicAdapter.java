package com.example.nikitaverma.contentprovider;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.BookViewHolder> {

    private final List<MediaModel> mediaModelList;
    private final Context mContext;
    private MainActivity mainActivity = new MainActivity();

    public MusicAdapter(List<MediaModel> bookList, Context context) {
        this.mediaModelList = bookList;
        this.mContext = context;
    }

    /**
     * Inflate xml file to java
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_model, parent, false);
        // itemView.setOnClickListener((View.OnClickListener) this);
        return new BookViewHolder(itemView);
    }

    /**
     * Bind each data to view
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull final BookViewHolder holder, int position) {
        holder.title.setText(mediaModelList.get(position).getTitle());
        holder.album.setText("" + mediaModelList.get(position).getAlbum());
        holder.path.setText("" + mediaModelList.get(position).getData());
        holder.artist.setText("" + mediaModelList.get(position).getArtist());
        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {

                //Toast.makeText(mContext,  mediaModelList.get(position).getData() + "", Toast.LENGTH_SHORT).show();
                mainActivity.onRecyclerItemClick(view, mContext, mediaModelList.get(position).getData(), position);
            }
        });
    }

    /**
     * @return size of listView
     */
    @Override
    public int getItemCount() {
        return mediaModelList.size();
    }

    public String getPath(int position) {
        return mediaModelList.get(position).getData();
    }

    public String getSongTitle(int position) {
        return mediaModelList.get(position).getTitle();
    }

    public String getAlbum(int position) {
        return mediaModelList.get(position).getAlbum();
    }

    public String getArtist(int position) {
        return mediaModelList.get(position).getArtist();
    }

    /**
     * holder to each object in view
     */
    public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView album;
        private TextView path;
        private TextView artist;
        private ItemClickListener clickListener;

        BookViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            album = view.findViewById(R.id.album);
            path = view.findViewById(R.id.path);
            artist = view.findViewById(R.id.artist);
            view.setOnClickListener(this);

        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition());
        }
    }

}