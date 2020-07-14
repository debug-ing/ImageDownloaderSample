package com.ninjageeks.imagedownloder;

import androidx.appcompat.app.AppCompatActivity;
import jp.wasabeef.glide.transformations.BlurTransformation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {

    private ImageView img;
    private TextView textFileSize;
    private TextView textMessageTime1;
    private ImageView imgMessageStatus;
    private ImageView blur;;
    ProgressBar progress;
    ProgressBar p_progress;
    ImageView download;
    ImageView p_download;
    ImageView P_ImageView;
    FrameLayout load_image;
    FrameLayout p_load_image;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        final String link = "https://s.goftino.com/dl/5dc533e009d1ba31c4001a73/dpxgpjeqft0c/image.png";
        //
        img = findViewById(R.id.row_chat_bubble_image_img);
        blur = findViewById(R.id.dogBlurImageView);
        progress = findViewById(R.id.progress);
        download = findViewById(R.id.download);
        textFileSize = findViewById(R.id.row_chat_bubble_image_size);
        load_image = findViewById(R.id.load_image);
        //
        imgMessageStatus = findViewById(R.id.row_chat_bubble_status);
        textMessageTime1 = findViewById(R.id.row_chat_bubble_time);
        //
        Log.e("LinkFile",link);
        String[] datas = link.split("/");
        int size = datas.length;
        String fileName = datas[size-2] + datas[size-1];
        final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SampleNinja/" + fileName);
        Log.e("Filepath",file.getPath());
        //

        //
        if(file.exists()){
            load_image.setVisibility(View.GONE);
            Glide.with(this).load(file.getPath())
                    .into(img);
            Glide.with(this)
                    .load(file.getPath())
                    .centerCrop()
                    .into(blur);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HandleValue.ImageFile =file;
                    MainActivity.context.startActivity(new Intent(context, ImageViewActivity.class));
                }
            });
        }
        else{
            load_image.setVisibility(View.VISIBLE);
            Glide.with(this).load(link.replace("dl","thumb"))
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 5)))
                    .into(img);
            //
            Glide.with(this)
                    .load(link.replace("dl","thumb"))
                    .centerCrop()
                    .into(blur);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    P_ImageView = img;
                    p_download = download;
                    p_load_image = load_image;
                    p_download.setVisibility(View.GONE);
                    progress.setVisibility(View.VISIBLE);
                    p_progress = progress;
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(link.replace("thumb","dl"));
                }
            });
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

    }

    @Override
    public void InitView() {

    }
    //
    class DownloadTask extends AsyncTask<String, Integer, String> {

        ImageView acimageView;

        /**
         * Set up a ProgressDialog
         */
        @Override
        protected void onPreExecute() {
            acimageView = P_ImageView;
        }

        /**
         *  Background task
         */
        @Override
        protected String doInBackground(String... params) {
            String path = params[0];
            int file_length;

            Log.i("Info: path", path);
            try {
                URL url = new URL(path);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                file_length = urlConnection.getContentLength();

                /**
                 * Create a folder
                 */
                File new_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "SampleNinja");
                if (!new_folder.exists()) {
                    if (new_folder.mkdir()) {
                        Log.i("Info", "Folder succesfully created");
                    } else {
                        Log.i("Info", "Failed to create folder");
                    }
                } else {
                    Log.i("Info", "Folder already exists");
                }

                /**
                 * Create an output file to store the image for download
                 */
                //
                String[] datas = path.split("/");
                int size = datas.length;
                String fileName = datas[size-2] + datas[size-1];
                //
                File output_file = new File(new_folder, fileName);
                OutputStream outputStream = new FileOutputStream(output_file);

                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                byte [] data = new byte[1024];
                int total = 0;
                int count;
                while ((count = inputStream.read(data)) != -1) {
                    total += count;

                    outputStream.write(data, 0, count);
                    int progress = 100 * total / file_length;
                    publishProgress(progress);

                    Log.i("Info", "Progress: " + Integer.toString(progress));
                }
                inputStream.close();
                outputStream.close();

                Log.i("Info", "file_length: " + Integer.toString(file_length));
                return new_folder.getPath() + "/"+ fileName;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error";
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            p_progress.setProgress(values[0]);
            //progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(final String result) {
            //progressDialog.hide();
            Log.e("result",result);
            Glide.with(context).load(result)
                    .into(acimageView);
            p_load_image.setVisibility(View.GONE);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(result);
                    HandleValue.ImageFile =file;
                    MainActivity.context.startActivity(new Intent(context, ImageViewActivity.class));
                }
            });
        }
    }
}
