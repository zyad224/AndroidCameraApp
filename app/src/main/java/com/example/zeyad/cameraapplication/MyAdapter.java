package com.example.zeyad.cameraapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 *
 * Te class that gets ImageElement objects in a list
 * and converts to bitmap and also gives position to other activities
 *
 *
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.View_Holder> {
    static private Context context;
    private static List<ImageElement> items;


    public MyAdapter(List<ImageElement> items) {

        this.items = items;
    }

    public MyAdapter(Context cont, List<ImageElement> items) {
        super();
        this.items = items;
        context = cont;
    }


    /**
     * Method that gives to holder
     * @param parent
     * @param viewType
     * @return holder
     */
    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image,
                parent, false);
        View_Holder holder = new View_Holder(v);
        context= parent.getContext();
        return holder;
    }

    /**
     * Method that sending the position to other activity
     *with using holder and position information
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView
        if (holder!=null && items.get(position)!=null) {
            if (items.get(position).image!=-1) {
                holder.imageView.setImageResource(items.get(position).image);
            }
            else if (items.get(position).file!=null){
                new UploadSingleImageTask().execute(new HolderAndPosition(position, holder));

            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowImageActivity.class);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            });
        }
    }


    // convenience method for getting data at click position
    ImageElement getItem(int id) {
        return items.get(id);
    }

    /**
     *
     * @return items size
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     *
     * Get the imageView from layout
     */
    public class View_Holder extends RecyclerView.ViewHolder  {
        ImageView imageView;
        View_Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_item);

        }

    }

    /**
     *
     * Method that decodes the bitmap from resource
     *
     * @param filePath is image path
     * @param reqWidth is image width
     * @param reqHeight is image height
     * @return the image size
     */
    public static Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Method is a getter method
     * @return items
     */
    public static List<ImageElement> getItems() {
        return items;
    }

    /**
     * Method is a setter method
     *  @param items
     */
    public static void setItems(List<ImageElement> items) {
        MyAdapter.items = items;
    }


    private class UploadSingleImageTask extends AsyncTask<HolderAndPosition, Void, Bitmap> {
        HolderAndPosition holdAndPos;
        @Override
        protected Bitmap doInBackground(HolderAndPosition... holderAndPosition) {
            holdAndPos=holderAndPosition[0];
            Bitmap myBitmap = decodeSampledBitmapFromResource(items.get(holdAndPos.getPosition()).file.getAbsolutePath(), 150, 150);
            return myBitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap){
            holdAndPos.getHolder().imageView.setImageBitmap(bitmap);
        }
    }


    /**
     * Method that keeps holder and position
     */
    private class HolderAndPosition{
        private View_Holder holder;
        private int position;

        public HolderAndPosition(int position,View_Holder holder){
            this.holder=holder;
            this.position=position;
        }
        public View_Holder getHolder(){
            return holder;
        }

        public int getPosition(){
            return position;
        }

    }
}