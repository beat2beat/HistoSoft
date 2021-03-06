package com.example.windows10.leerimagen;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VistaPrevia extends AppCompatActivity {

    /*
    int[][] r;
    int[][] g;
    int[][] b;
    int[][] a;
    int[][] gr;
    */

///Inicializacion de parámetros./////////////////////////////////////////////////////////////////////

    int r,g,b,a,gr;

    ImageView iv1;
    static Bitmap imagen;
    Bitmap imagenFiltrada;
    String currentDirectory;
    static String currentFileName;
    static String nameFileDirectory;
    RelativeLayout layout1;
    boolean  destruido=false;
    int grados=90;
    Spinner s1;

    int[] pixels;


    final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE=2;
    final int REQUEST_TAKE_PHOTO=4;
    final int SELECT_PICTURE=8;

/////////////////////////////////////////////////////////////////////////////////////////////////////
    DrawerLayout drawerLayout;
    NavigationView navView;
    NotificationCompat.Builder mBuilder;
    private int REQUEST_CROP_ICON=16;
    public static Uri selectedImage;
    static boolean AgregarMuestra=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Capturar imagen");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        //StatusBar///////////////////////////////////////////////////////////////
        mBuilder =
                new NotificationCompat.Builder(VistaPrevia.this)
                        .setSmallIcon(R.drawable.ic_save_white_24dp)
                        .setLargeIcon((((BitmapDrawable)getResources()
                                .getDrawable(R.mipmap.ic_launcher)).getBitmap()))
                        .setContentTitle("Imagen original guardada")
                        .setContentText("Toque para ver la imagen previa")
                        .setContentInfo("4")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setTicker("¡Guardado!");

        Intent notIntent =
                new Intent(VistaPrevia.this, VistaPrevia.class);
        /*PendingIntent contIntent =
                PendingIntent.getActivity(
                        VistaPrevia.this, 0, notIntent, 0);*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(VistaPrevia.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        ///////////////////////////////////////////////////////////////////

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navView=(NavigationView)findViewById(R.id.navview);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.agregarMuestra:
                        Intent agregarMuestra=new Intent(VistaPrevia.this,MainActivityPrincipal.class);
                        AgregarMuestra=true;
                        startActivity(agregarMuestra);
                        finish();
                        break;
                    case R.id.nav_camera:
                        //item.setChecked(true);
                        foto(null);
                       // getSupportActionBar().setTitle(item.getTitle());
                        break;
                    case R.id.nav_gallery:
                        //item.setChecked(true);
                        abrirImagen(null);
                        //getSupportActionBar().setTitle(item.getTitle());
                        break;
                    case R.id.guardarImagen:
                        //item.setChecked(true);
                        guardarImagen(null);
                        //getSupportActionBar().setTitle(item.getTitle());
                        break;
                    case R.id.nav_share:
                        //item.setChecked(true);
                        if(currentFileName!=null) {
                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/jpg");
                            final File photoFile = new File(currentFileName);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
                            startActivity(Intent.createChooser(shareIntent, "Eliga donde compartir"));
                        }else{
                            Toast.makeText(VistaPrevia.this, "No existe imagen previa", Toast.LENGTH_SHORT).show();
                        }
                        //getSupportActionBar().setTitle(item.getTitle());
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        //setTitle("Imagen Previa");

        iv1=(ImageView)findViewById(R.id.iv1);
        layout1=(RelativeLayout)findViewById(R.id.layout1);

        //s1=(Spinner)findViewById(R.id.s1);

        if(currentFileName!=null) {
            imagen = BitmapFactory.decodeFile(currentFileName);
            iv1.setImageBitmap(imagen);
        }

        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                        if(currentFileName!=null) {
//                            File file = new File(currentFileName);
//                            Uri uri = Uri.fromFile(file);
//                            Intent i = new Intent(Intent.ACTION_VIEW);
//                            i.setDataAndType(uri, "image/*");
//                            startActivity(i.createChooser(i,"Eliga la aplicación"));
//                        }else {
//                            Toast.makeText(VistaPrevia.this, "No existe imagen previa", Toast.LENGTH_SHORT).show();
//                        }
                if(imagen!=null){
                    Intent i=new Intent(VistaPrevia.this,MainActivityVisualizarFoto.class);
                    startActivity(i);
                }
                else{
                    Toast.makeText(VistaPrevia.this, "No existe imagen previa...", Toast.LENGTH_SHORT).show();
                }

            }
        });


///Al realizar un click largo aparece un cuadro de dialogo para seleccionar los grados de rotación.////////////
///*
//        iv1.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                final AlertDialog.Builder builder=new AlertDialog.Builder(VistaPrevia.this);
//                builder.setTitle("¿Quieres rotar la imagen?");
//
//                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String [] items={"90°","180°","270°"};
//                        final AlertDialog.Builder builder=new AlertDialog.Builder(VistaPrevia.this);
//                        builder.setTitle("Seleccione los grados de rotación");
//                        builder.setItems(items, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                switch (which){
//                                    case 0:
//                                        rotarImagen(90);
//                                        break;
//                                    case 1:
//
//                                        rotarImagen(180);
//                                        break;
//                                    case 2:
//                                        rotarImagen(270);
//                                        break;
//                                }
//                            }
//                        });
//                        builder.show();
//                    }
//                });
//                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
//                builder.show();
//                return true;
//            }
//        });
//        */

/////////Es un Spinner que contiene la lista de ediciones////////////////////////////////////////////

//        /*
//        List list=new ArrayList();
//        list.add("None");
//        list.add("Editar");
//        list.add("Aplicar filtro");
//        list.add("Guardar imagen");
//        list.add("Abrir imagen");
//
//        ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
//        s1.setAdapter(arrayAdapter);
//
//        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                switch (position){
//                    case 0:
//                        break;
//                    case 1:
//                        editar(null);
//                        break;
//                    case 2:
//                        aplicarFiltro(null);
//                        break;
//                    case 3:
//                        guardarImagen(null);
//                        break;
//                    case 4:
//                        abrirImagen(null);
//
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        */

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //imagen= BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
        //iv1.setImageBitmap(imagen);


///Algoritomo de manejo de permisos/////////////////////////////////////////////////////////////////////////////////

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(VistaPrevia.this,"El permiso de usar la camara fue denegado",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }//Fin del onCreate.

///Manejo de los resultados de los permisos./////////////////////////////////////////////////////////////

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void editar(View v){
        if(currentFileName!=null){
            Intent intent = new Intent(this, Edicion.class);
            startActivity(intent);
        }else{
            Toast.makeText(this,"No existe imagen previa",Toast.LENGTH_SHORT).show();
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void abrirImagen(View v){

        Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i,SELECT_PICTURE);
    }

///Método que realiza la extracción de cada pixel del bitmap par
/// a luego extraerle el ARGB.
///Posteriormente se promedia el rgb y se lo setea a cada pixel de la imagen.

    public void aplicarFiltro(View v){
        if(imagen!=null) {
            Intent intent = new Intent(this, Filtros.class);
            startActivity(intent);
        }else{
            Toast.makeText(this,"No existe imagen previa",Toast.LENGTH_SHORT).show();
        }

    }

////////////////////////////////////////////////////////////////////////////////////////////////////
    private void guardarEnCache() {

        SharedPreferences preferences=getSharedPreferences("datos", Context.MODE_PRIVATE);
        nameFileDirectory=preferences.getString("nombre","")+preferences.getString("apellido","")
                +"/"+preferences.getString("proyecto","");
        File file = new File(getExternalCacheDir(), "imagenCache"+nameFileDirectory);
        file.mkdirs();
        if (!file.mkdirs()) {
            Log.e("directorio", "Directory not created");
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("ddMMyyyyhhmmss");
        String date=simpleDateFormat.format(new Date());
        String name="img"+date+".jpg";
        String file_name=file.getAbsolutePath()+"/"+name;
        File new_file=new File(file_name);

        currentFileName=new_file.getAbsolutePath();
        selectedImage=Uri.fromFile(new_file);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new_file);
            imagen.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

///Método que guarda la imagen en un directorio especifico de la memoria externa.////////////////////

    public void guardarImagen(View v){
        if(imagen!=null) {
            FileOutputStream outputStream = null;
            File file =createFilePath();
            currentFileName=file.getAbsolutePath();
            try {
                outputStream = new FileOutputStream(file);
                imagen.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show();
                outputStream.flush();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            galleryAddPic();
        }else{
            Toast.makeText(this,"No existe imagen previa",Toast.LENGTH_SHORT).show();
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////

///Método que abre la cámara, realiza una captura y guarda la imagen en la ubicación definida previamente.////

    public void foto(View v){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createFilePath();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.windows10.leerimagen.fileprovider",
                        photoFile);
                selectedImage = photoURI;
                Log.d("fotoUri",photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("return-data",true);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////

////Se crea la dirección y el nombre completo de la foto.//////////////////////////////////////////////////

    public  File createFilePath() {

        SharedPreferences preferences=getSharedPreferences("datos", Context.MODE_PRIVATE);
        nameFileDirectory="HistoSoft"+"/"+preferences.getString("nombre","")+" "+preferences.getString("apellido","")
                         +"/"+preferences.getString("area","")+"/"+preferences.getString("muestra","");

        File file=getAlbumStorageDir(nameFileDirectory);
        currentDirectory=file.getAbsoluteFile().toString();
        if(!file.exists()&&!file.mkdirs()){
            Toast.makeText(this,"No se pudo crear el directorio para grabar la imagen",Toast.LENGTH_SHORT).show();
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("ddMMyyyyhhmmss");
        String date=simpleDateFormat.format(new Date());
        String name="img"+date+".jpg";
        String file_name=file.getAbsolutePath()+"/"+name;
        File new_file=new File(file_name);
        currentFileName=file_name;
        return new_file;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////Método que obtiene el directorio donde se va a ubicar la imágen.///////////////////////////////////

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        file.mkdirs();
        if (!file.mkdirs()) {
            Log.e("directorio", "Directory not created");
        }
        return file;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////

////Manejo del resultado de la cámara./////////////////////////////////////////////////////////////////////
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //Toast.makeText(this,"Imagen guardada en: "+currentFileName,Toast.LENGTH_LONG).show();
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
            Snackbar.make(layout1, "Imagen guardada en: "+currentFileName, Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    })
                    .show();
           // selectedImage=data.getData();
            setPic();
            galleryAddPic();
            invalidateOptionsMenu();
        }
        if(requestCode==SELECT_PICTURE && resultCode == RESULT_OK){

            if(imagen!=null)
                imagen.recycle();

            selectedImage = data.getData();

            InputStream is;
            try {
                is = getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                imagen = BitmapFactory.decodeStream(bis);
                String path= getRealPathFromUri(selectedImage);
                Bitmap imagenRotada= null;
                try {
                    imagenRotada = rotateImageIfRequired(imagen,path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //imagen.recycle();
                imagen=imagenRotada;
                iv1.setImageBitmap(imagen);
                guardarEnCache();
                invalidateOptionsMenu();
            } catch (FileNotFoundException e) {}

        }if(requestCode==REQUEST_CROP_ICON && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            if(extras != null ) {
                Bitmap photo = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            // The stream to write to a file or directly using the
            }
        }
        if(requestCode==SELECT_PICTURE && resultCode==RESULT_CANCELED){
           Toast.makeText(this,"No seleccionó ninguna imagen",Toast.LENGTH_SHORT).show();
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////

///Escala la imagen y setea la imagen resultante en el imageView.//////////////////////////////////////////

    private void setPic() {


        // Get the dimensions of the View
        int targetW = iv1.getWidth();
        int targetH = iv1.getHeight();
        //if(!(targetW==0||targetH==0)) {
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentFileName, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;


        imagen = BitmapFactory.decodeFile(currentFileName, bmOptions);

        Bitmap imagenRotada= null;
        try {
            imagenRotada = rotateImageIfRequired(imagen,currentFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagen=imagenRotada;
        iv1.setImageBitmap(imagen);
        guardarEnCache();
        //}else{}
        //Drawable imagen2=Drawable.createFromPath(mCurrentPhotoPath);
        //mImageView.setImageDrawable(imagen2);
    }

    private Bitmap rotateImageIfRequired(Bitmap image, String path) throws IOException{

        //String path= getRealPathFromUri(imageUri);
        ExifInterface ei= new ExifInterface(path);
        int orientation= ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);

        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(image,90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(image,180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(image,270);
            default:
                return image;
        }

    }

    private static Bitmap rotateImage(Bitmap img, int degree){
        Matrix matrix= new Matrix();
        matrix.postRotate(degree);
        Bitmap imagenRotada= Bitmap.createBitmap(img,0,0,img.getWidth(),img.getHeight(),matrix,true);
        //img.recycle();
        return imagenRotada;
    }

    private String getRealPathFromUri(Uri imgUri){
        String res=null;
        String[] proj={MediaStore.Images.Media.DATA};
        Cursor cursor= getContentResolver().query(imgUri,proj,null,null,null);
        if(cursor.moveToFirst()){
            int colunm_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res= cursor.getString(colunm_index);
        }
        cursor.close();
        return res;

    }

///Método que realiza un escaneo de los archivos de multimedia para que aparezca en la galería.///////
    private void galleryAddPic() {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(currentFileName);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    this.sendBroadcast(mediaScanIntent);
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
    public void rotarImagen(int grados){
    if(imagen!=null) {
        Matrix matrix = new Matrix();
        matrix.postRotate(grados);
        int w = imagen.getWidth();
        int h = imagen.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(imagen, 0, 0, w, h, matrix, false);
        //imagen.recycle();
        imagen = bitmap;
        iv1.setImageBitmap(imagen);
        guardarEnCache();
    }else{
        Toast.makeText(this,"No existe imagen previa",Toast.LENGTH_SHORT).show();
    }
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu2,menu);

        if(imagen==null){
            menu.findItem(R.id.aplicarFiltro).setVisible(false);
            menu.findItem(R.id.cortar).setVisible(false);
            menu.findItem(R.id.medir).setVisible(false);
            menu.findItem(R.id.editar).setVisible(false);
            menu.findItem(R.id.rotar).setVisible(false);
            menu.findItem(R.id.cortar_automatico).setVisible(false);
            menu.findItem(R.id.recuentoKi67).setVisible(false);
        }else{
            menu.findItem(R.id.aplicarFiltro).setVisible(true);
            menu.findItem(R.id.cortar).setVisible(true);
            menu.findItem(R.id.medir).setVisible(true);
            menu.findItem(R.id.editar).setVisible(true);
            menu.findItem(R.id.rotar).setVisible(true);
            menu.findItem(R.id.cortar_automatico).setVisible(true);
            menu.findItem(R.id.recuentoKi67).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.cortar:
                if(currentFileName != null) {
//                    Intent intentCortar = new Intent("com.android.camera.action.CROP");
//                    //intentCortar.setClassName("com.android.camera", "com.android.camera.CropImage");
//                    intentCortar.setDataAndType(selectedImage,"image/*");
//                    intentCortar.putExtra("crop", "true");
//                    intentCortar.putExtra("aspectX", 1);
//                    intentCortar.putExtra("aspectY", 1);
//                    intentCortar.putExtra("outputX", 96);
//                    intentCortar.putExtra("outputY", 96);
//                    intentCortar.putExtra("noFaceDetection", true);
//                    intentCortar.putExtra("return-data", true);
//                    startActivityForResult(intentCortar, REQUEST_CROP_ICON);
//                    if(imagen!=null)
//                        imagen.recycle();
                    Intent intentCortar = new Intent(this,CortarImagen.class);
                    startActivity(intentCortar);

                }else{
                    Toast.makeText(this,"No existe imagen previa...",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cortar_automatico:
                if(currentFileName != null) {
                    final ProgressDialog dialog=new ProgressDialog(this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage("Procesando...");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap imagenCortada;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.show();
                                }
                            });
                            imagenCortada= RecortarAutomatico.recortarImagenAutomatico(VistaPrevia.this,imagen,getResources().getDisplayMetrics().density);
                            if(!(imagenCortada==null)){
                                imagen=imagenCortada.copy(Bitmap.Config.ARGB_8888,true);
                                imagenCortada.recycle();
                                guardarEnCache();
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(VistaPrevia.this,"Debes elegir u obtener una imagen histologica no recortada previamente..",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            dialog.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv1.setImageBitmap(imagen);
                                }
                            });
                        }
                    }).start();

                }else{
                    Toast.makeText(this,"No existe imagen previa...",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.medir:
                if(currentFileName != null) {
//                    if(imagen!=null)
//                        imagen.recycle();
                    Intent intentMedir = new Intent(this, MedicionesCuantitativas.class);
                    startActivity(intentMedir);
                }else{
                    Toast.makeText(this,"No existe imagen previa...",Toast.LENGTH_SHORT).show();
                }
                break;
//            case R.id.actualizar:
//                recreate();
//                Toast.makeText(this,"Actualizado...",Toast.LENGTH_SHORT).show();
//                break;
            case R.id.editar:
//                if(imagen!=null)
//                    imagen.recycle();
                editar(null);
                break;
            case R.id.aplicarFiltro:
//                if(imagen!=null)
//                    imagen.recycle();
                aplicarFiltro(null);
                break;
//            case R.id.guardarImagen:
//                guardarImagen(null);
//                break;
            case R.id.rotarDerecha:
                rotarImagen(90);
                break;
            case R.id.rotarIzquierda:
                rotarImagen(-90);
                break;
            case R.id.abrirImagen:
                abrirImagen(null);
                break;
            case R.id.recuentoKi67A:
                Intent intentRecuentoA= new Intent(this, RecuentoKi67Activity.class);
                startActivity(intentRecuentoA);
                break;
            case R.id.recuentoKi67M:
                Intent intentRecuentoM= new Intent(this, RecuentoKi67ManualActivity.class);
                startActivity(intentRecuentoM);
                break;
            case R.id.acercaDe:
                Intent intentAcercaDe=new Intent(this,AcercaDe2.class);
                startActivity(intentAcercaDe);
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentFileName!=null) {
            imagen = BitmapFactory.decodeFile(currentFileName);
            iv1.setImageBitmap(imagen);
            invalidateOptionsMenu();
        }else{
            invalidateOptionsMenu();
            iv1.setImageResource(R.drawable.ic_image_black_36dp);
        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("¿Estas seguro que quieres salir?");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAndRemoveTask();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
