package com.chatid.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chatid.app.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class VerifyActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    protected Interpreter tflite;
    private  int imageSizeX;
    private  int imageSizeY;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;

    public Bitmap oribitmap,testbitmap;
    public static Bitmap cropped;
    Uri imageuri;
    Uri imageuri1;

    ImageView oriImage,testImage;
    Button buverify;
    TextView result_text;

    float[][] ori_embedding = new float[1][128];
    float[][] test_embedding = new float[1][128];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        storage=FirebaseStorage.getInstance();
        storageReference= storage.getReference();

        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Verify identity");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //upload all records and look for a specific child among them
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                username.setText(user.getUsername());
                try {
                    if (user.getImageURL().equals("default")){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    }else{
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }
                } catch (NullPointerException e){
                    Intent intent = new Intent(VerifyActivity.this, StartActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

            initComponents();
    }

    private void initComponents() {

        oriImage=(ImageView)findViewById(R.id.image1);
        testImage=(ImageView)findViewById(R.id.image2);
        buverify=(Button)findViewById(R.id.verify);
        result_text=(TextView)findViewById(R.id.result);

        try{
            tflite=new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(VerifyActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(VerifyActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    100);
        }



        oriImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);

                //Intent intent=new Intent();
                //intent.setType("image/*");
                //intent.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(Intent.createChooser(intent,"Select Picture"),12);
            }
        });

        testImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),13);
            }
        });

        buverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double distance=calculate_distance(ori_embedding,test_embedding);

                if(distance<6.0) {
                    result_text.setText("Result : Same Faces");
                    Toast.makeText(getApplicationContext(),
                            "Identification Successful!", Toast.LENGTH_SHORT)
                            .show();
                    uploadPicture(imageuri1);
                    startActivity(new Intent(VerifyActivity.this,VerifyActivity.class));
                    finish();
                }

                else {
                    result_text.setText("Result : Different Faces");
                    Toast.makeText(getApplicationContext(),
                            "Identification Failed", Toast.LENGTH_SHORT)
                            .show();
                    startActivity(new Intent(VerifyActivity.this, VerifyActivity.class));
                    finish();
                }
            }

        });

    }

    private void uploadPicture(Uri imageuri) {

        FirebaseUser fuser;
        String uid;
        fuser=FirebaseAuth.getInstance().getCurrentUser();
        uid=fuser.getUid();

        StorageReference riversRef = storageReference.child("images/"+uid);
        riversRef.putFile(imageuri);



        riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = (Uri) task.getResult();
                String mUri = downloadUri.toString();

                reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                HashMap<String, Object> map = new HashMap<>();
                map.put("idImageURL", mUri);
                reference.updateChildren(map);
            }
        });

    }

    private double calculate_distance(float[][] ori_embedding, float[][] test_embedding) {
        double sum =0.0;
        for(int i=0;i<128;i++){
            sum=sum+Math.pow((ori_embedding[0][i]-test_embedding[0][i]),2.0);
        }
        return Math.sqrt(sum);
    }

    private TensorImage loadImage(final Bitmap bitmap, TensorImage inputImageBuffer ) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor=activity.getAssets().openFd("Qfacenet.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }

    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==100) {
            imageuri = data.getData();
            try {

                oribitmap=(Bitmap) data.getExtras().get("data");

                //oribitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                oriImage.setImageBitmap(oribitmap);
                face_detector(oribitmap,"original");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(requestCode==13 && resultCode==RESULT_OK && data!=null) {
            imageuri1 = data.getData();
            try {
                testbitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri1);
                testImage.setImageBitmap(testbitmap);
                face_detector(testbitmap,"test");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void face_detector(final Bitmap bitmap, final String imagetype){

        final InputImage image = InputImage.fromBitmap(bitmap,0);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                // Task completed successfully
                                for (Face face : faces) {
                                    Rect bounds = face.getBoundingBox();
                                    cropped = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height());
                                    get_embaddings(cropped,imagetype);
                                }
                            }

                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
    }

    public void get_embaddings(Bitmap bitmap,String imagetype){

        TensorImage inputImageBuffer;
        float[][] embedding = new float[1][128];

        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType);

        inputImageBuffer = loadImage(bitmap,inputImageBuffer);

        tflite.run(inputImageBuffer.getBuffer(),embedding);

        if(imagetype.equals("original"))
            ori_embedding=embedding;
        else if (imagetype.equals("test"))
            test_embedding=embedding;
    }

    public void opencamera(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,0);
    }

}

