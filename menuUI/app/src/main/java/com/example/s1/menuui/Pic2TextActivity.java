package com.example.s1.menuui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Pic2TextActivity extends AppCompatActivity {
    private TessBaseAPI m_Tess; //Tess API reference
    private String mDataPath = ""; //언어데이터가 있는 경로
    Button button;
    ImageView imageView;
    TextView textView;
    private Context mContext;
    private Uri image_uri;  //사진 경로
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic2text);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                change();
//            }
//        });

        image_uri = getIntent().getParcelableExtra("image");
        //이미지 디코딩을 위한 초기화
        try {
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_uri); //샘플이미지파일
        } catch (IOException e) {
            e.printStackTrace();
        }

        m_Tess = new TessBaseAPI();
        mDataPath = getFilesDir()+ "/tesseract/";
        checkFile(new File(mDataPath + "tessdata/"));
        
        String lang = "kor";
        m_Tess.init(mDataPath, lang);

        imageView.setImageURI(image_uri);
        change();
    }

    private void checkFile(File dir) {
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = mDataPath+ "/tessdata/kor.traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        if (requestCode == 0) {

        } else {

        }
    }

    // 이미지를 원본과 같게 회전시킨다.
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    //copy file to device
    private void copyFiles() {
        try{
            String filepath = mDataPath + "/tessdata/kor.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/kor.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void change()
    {
        button.setEnabled(false);
        button.setText("텍스트 인식중...");
        preprocessing(image);
        new AsyncTess().execute(image);

//        surfaceView.capture(new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] bytes, Camera camera) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 8;
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                bitmap = GetRotatedBitmap(bitmap, 90);
//
//                imageView.setImageBitmap(bitmap);
//
//                button.setEnabled(false);
//                button.setText("텍스트 인식중...");
//                new AsyncTess().execute(bitmap);
//
//                camera.startPreview();
//            }
//        });
    }

    private void preprocessing(Bitmap image) {
        Bitmap image1;
        OpenCVLoader.initDebug();

        Mat img1 = new Mat();
        Utils.bitmapToMat(image,img1);

        Mat imageGray = new Mat();
        Mat imageCny = new Mat();

        Imgproc.cvtColor(img1,imageGray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(imageGray, imageCny, 160, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imageCny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for(int i=0;i>=0;i=(int)hierarchy.get(0,i)[0]){
            MatOfPoint matOfPoint = contours.get(i);
            Rect rect = Imgproc.boundingRect(matOfPoint);

            if(rect.width < 30 || rect.height < 30 || rect.width <= rect.height || rect.x < 20 || rect.y < 20
                    || rect.width <= rect.height * 3 || rect.width >= rect.height * 6) continue; // 사각형 크기에 따라 출력 여부 결정


        }
    }

//    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
//        if (degrees != 0 && bitmap != null) {
//            Matrix m = new Matrix();
//            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
//            try {
//                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
//                if (bitmap != b2) {
//                    bitmap = b2;
//                }
//            } catch (OutOfMemoryError ex) {
//                ex.printStackTrace();
//            }
//        }
//        return bitmap;
//    }

    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {
        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            m_Tess.setImage(mRelativeParams[0]);
            return m_Tess.getUTF8Text();
        }

        protected void onPostExecute(String result) {
            textView.append(result);
            Toast.makeText(Pic2TextActivity.this, ""+result, Toast.LENGTH_LONG).show();

            button.setEnabled(true);
            button.setText("Recognition Finished");
        }
    }
    
    //region Thread
//    public class OCRThread extends Thread
//    {
//        private Bitmap rotatedImage;
//        OCRThread(Bitmap rotatedImage)
//        {
//            this.rotatedImage = rotatedImage;
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            // 사진의 글자를 인식해서 옮긴다
//            String OCRresult = null;
//            m_Tess.setImage(rotatedImage);
//            OCRresult = m_Tess.getUTF8Text();
//
//            Message message = Message.obtain();
//            message.what = ConstantDefine.RESULT_OCR;
//            message.obj = OCRresult;
//            m_messageHandler.sendMessage(message);
//
//        }
//    }
    //endregion

    //Process an Image
//    public void processImage(View view) {
//        OCRThread ocrThread = new OCRThread(image);
//        ocrThread.setDaemon(true);
//        ocrThread.start();
//    }


}
