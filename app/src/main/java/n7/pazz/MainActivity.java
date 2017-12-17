package n7.pazz;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.danil.recyclerbindableadapter.library.RecyclerBindableAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    String path;
    LoadFavorites adapter;
    public List<Model> sounds = new ArrayList<>();
    MediaPlayer mMediaPlayer = new MediaPlayer();
    TextView lastTextView=null;
    ImageView inimation;
    int clickId=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inimation= (ImageView) findViewById(R.id.imageView);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        inimation.setVisibility(View.INVISIBLE);
        utils.startSpriteAnim(MainActivity.this,inimation,"logo_anim.png",false,500,281,100,2);

        try {
            for (String a : getAssets().list("1"))
                sounds.add(new Model(a));
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        adapter = new LoadFavorites();
        adapter.addAll(sounds);
        recyclerView.setAdapter(adapter);
    }

    private void setAsRingtone(String name) {
        File myFile = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+getString(R.string.app_name)+File.separator + name);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, myFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, name.replace(".ogg",""));
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg");
        values.put(MediaStore.MediaColumns.SIZE, myFile.length());
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        ContentResolver contentResolver = getContentResolver();
        Uri generalaudiouri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        contentResolver.delete(generalaudiouri, MediaStore.MediaColumns.DATA + "='" + myFile.getAbsolutePath() + "'", null);
        Uri ringtoneuri = contentResolver.insert(generalaudiouri, values);
        RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_NOTIFICATION, ringtoneuri);

            Snackbar.make(inimation, R.string.established, Snackbar.LENGTH_SHORT).show();
    }

    public class Mp3Loader extends AsyncTask<String, Void, String> {
        private String url;
        private boolean setAsTone;

        private Mp3Loader(String url, boolean setAsTone) {
            this.url = url;
            this.setAsTone = setAsTone;
        }

        private void copyFile(InputStream in,OutputStream out) {
            byte[] buffer = new byte[1024];
            int read;
            try {
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer,0,read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... params) {

            AssetManager assetManager = getAssets();
            InputStream in=null;
            OutputStream out=null;
            try {
                in = assetManager.open("1/"+url);

                File outFile = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+getString(R.string.app_name),url);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return url;
        }

        @Override
        protected void onPostExecute(String name) {
            super.onPostExecute(name);
            if (setAsTone) setAsRingtone(name);
            else
                Snackbar.make(inimation,Environment.getExternalStorageDirectory().getPath()+File.separator+getString(R.string.app_name)+File.separator+name , Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri mydir = Uri.parse(Environment.getExternalStorageDirectory().getPath()+File.separator+getString(R.string.app_name));
                                intent.setDataAndType(mydir, "*/*");
                                startActivity(intent);
                            }
                        })
            .show();
        }
    }

    public class LoadFavorites extends RecyclerBindableAdapter<Model,LoadFavorites.ViewHolder> {

        @Override
        protected void onBindItemViewHolder(ViewHolder viewHolder, int position, int type) {
            viewHolder.setData(getItem(position));
            viewHolder.name.setTextColor(clickId==viewHolder.getAdapterPosition()? getResources().getColor(R.color.colorAccent):getResources().getColor(R.color.textColor));
        }

        @Override
        protected ViewHolder viewHolder(View view, int type) {
            return new ViewHolder(view);
        }

        @Override
        protected int layoutId(int type) {
            return R.layout.item;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            TextView name;

            ViewHolder(View itemView) {
                super(itemView);
                name=itemView.findViewById(R.id.name);
            }

            void setData(final Model model) {
            name.setText(model.getName().replace(".ogg",""));
            name.setOnClickListener(this);
            name.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                inimation.setVisibility(View.VISIBLE);
                if(getAdapterPosition()==RecyclerView.NO_POSITION) return;
                notifyItemChanged(clickId);
                clickId=getAdapterPosition();
                notifyItemChanged(clickId);

           try {
                    mMediaPlayer.release();
                    mMediaPlayer = new MediaPlayer();
                    AssetFileDescriptor afd = getAssets().openFd("1/"+name.getText().toString()+".ogg");
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    mMediaPlayer.prepareAsync();
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            inimation.setVisibility(View.INVISIBLE);
                            notifyItemChanged(clickId);
                            clickId=-1;
                        }
                    });
                } catch (Exception e) {
               e.printStackTrace();
                }
            }

            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(R.array.hero_responses_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                new Mp3Loader(name.getText().toString()+".ogg", false).execute();
                                break;
                            case 1:
                                new Mp3Loader(name.getText().toString()+".ogg", true).execute();
                                break;
                        }
                        dialog.dismiss();
                    }
                }).create().show();
                return true;
            }
        }
    }
}
