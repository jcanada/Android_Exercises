package com.example.joanericacanada.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by joanericacanada on 10/12/15.
 */
public class PhotoGalleryFragment extends Fragment {
    GridView gridView;
    ArrayList<GalleryItem> items;
    ThumbnailDownloader<ImageView> thumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemsTask().execute();

        thumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        thumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });

        thumbnailThread.start();
        thumbnailThread.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        gridView = (GridView)v.findViewById(R.id.gridView);

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailThread.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailThread.clearQueue();
    }

    void setupAdapter() {
        if (getActivity() == null || gridView == null) return;

        if (items != null)
            gridView.setAdapter(new GalleryItemAdapter(items));
         else
            gridView.setAdapter(null);

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> itms) {
            items = itms;
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> itms) {
            super(getActivity(), 0, itms);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brian_up_close);
            GalleryItem galleryItem = getItem(position);
            thumbnailThread.queueThumbnail(imageView, galleryItem.getUrl());

            return convertView;
        }
    }
}