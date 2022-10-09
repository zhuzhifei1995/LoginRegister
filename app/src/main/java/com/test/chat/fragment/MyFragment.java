package com.test.chat.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.chat.R;
import com.test.chat.activity.LoginActivity;
import com.test.chat.activity.PhotoShowActivity;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private View myFragmentView;
    private Activity activity;
    private Context context;
    private ProgressDialog progressDialog;
    private boolean IS_SHOW_MY_MESSAGE;
    private ImageView photo_my_ImageView;
    private Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            super.handleMessage(message);
            Intent intent = new Intent(context, LoginActivity.class);
            SharedPreferencesUtils.removeKey(context, "status", "user");
            File userPhotoFile = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/tmp/user", "photo.png.cache");
            if (userPhotoFile.delete()) {
                Log.e(TAG, "临时头像图片删除成功：" + userPhotoFile.getAbsolutePath());
            } else {
                Log.e(TAG, "无临时头像文件图片：" + userPhotoFile.getAbsolutePath());
            }
            TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend"));
            TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/message"));
            TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend"));
            TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image"));
            TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/voice"));
            startActivity(intent);
            activity.finish();
            progressDialog.dismiss();
        }
    };
    private Handler uploadUpdatePhotoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    photo_my_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(
                            Environment.getExternalStorageDirectory().getPath() + "/tmp/update", "photo.png.cache"));
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                    TmpFileUtil.copyFile(
                            new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update", "photo.png.cache"),
                            new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache"));
                } else {
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                    SharedPreferencesUtils.removeKey(context, "status", "user");
                    startActivity(new Intent(context, LoginActivity.class));
                    activity.finish();
                }
            } catch (JSONException e) {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
                photo_my_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(
                        Environment.getExternalStorageDirectory().getPath()
                                + "/tmp/user", "photo.png.cache"));
                e.printStackTrace();
            }
            super.handleMessage(message);
            progressDialog.dismiss();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activity = getActivity();
        context = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        myFragmentView = layoutInflater.inflate(R.layout.fragment_my, viewGroup, false);
        initMyFragmentView();
        return myFragmentView;
    }

    private void initMyFragmentView() {
        photo_my_ImageView = myFragmentView.findViewById(R.id.photo_my_ImageView);
        photo_my_ImageView.setOnClickListener(this);
        LinearLayout my_message_LinearLayout = myFragmentView.findViewById(R.id.my_message_LinearLayout);
        my_message_LinearLayout.setOnClickListener(this);
        TextView login_out_TextView = myFragmentView.findViewById(R.id.login_out_TextView);
        login_out_TextView.setOnClickListener(this);
        initTitleView();
        initMyMessageView();
    }

    private void initTitleView() {
        TextView top_title_TextView = myFragmentView.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("我的");
        Button title_right_Button = myFragmentView.findViewById(R.id.title_right_Button);
        title_right_Button.setText("");
        Button title_left_Button = myFragmentView.findViewById(R.id.title_left_Button);
        title_left_Button.setText("");
    }

    private void initMyMessageView() {
        Bitmap bitmap = ImageUtil.getBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache");
        if (bitmap != null) {
            Log.e(TAG, "图片加载正常");
            photo_my_ImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "图片为空,加载失败");
            photo_my_ImageView.setImageResource(R.drawable.user_default_photo);
        }
        TextView nike_name_TextView = myFragmentView.findViewById(R.id.nike_name_TextView);
        String nick_name = SharedPreferencesUtils.getString(context, "nick_name", "", "user");
        if (!nick_name.equals("")) {
            nike_name_TextView.setText(nick_name);
        } else {
            nike_name_TextView.setText("未设置昵称");
        }
        TextView login_number_TextView = myFragmentView.findViewById(R.id.login_number_TextView);
        String login_number = SharedPreferencesUtils.getString(context, "login_number", "", "user");
        login_number_TextView.setText(login_number);
        TextView phone_number_TextView = myFragmentView.findViewById(R.id.phone_number_TextView);
        String phone = SharedPreferencesUtils.getString(context, "phone", "", "user");
        phone_number_TextView.setText(phone);
        TextView create_time_TextView = myFragmentView.findViewById(R.id.create_time_TextView);
        String create_time = SharedPreferencesUtils.getString(context, "create_time", "", "user");
        create_time_TextView.setText(create_time);
        TextView password_TextView = myFragmentView.findViewById(R.id.password_TextView);
        String password = SharedPreferencesUtils.getString(context, "password", "", "user");
        password_TextView.setText(password);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_my_ImageView:
                updateMyPhoto();
                break;
            case R.id.my_message_LinearLayout:
                showMyMessage();
                break;
            case R.id.login_out_TextView:
                loginOut();
                break;
            default:
                break;
        }

    }

    private void loginOut() {
        progressDialog = new ProgressDialog(context);
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            progressDialog.setCancelable(true);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.login_out_progress_bar);
        }
        TextView login_out_confirm_TextView = progressDialog.findViewById(R.id.login_out_confirm_TextView);
        login_out_confirm_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Window window = progressDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    progressDialog.setCancelable(true);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.loading_progress_bar);
                    TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                    prompt_TextView.setText("退出登陆中.......");
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            waitHandler.sendMessage(new Message());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        TextView login_out_cancel_TextView = progressDialog.findViewById(R.id.login_out_cancel_TextView);
        login_out_cancel_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
            }
        });
    }

    private void showMyMessage() {
        ImageView message_show_ImageView = myFragmentView.findViewById(R.id.message_show_ImageView);
        LinearLayout my_show_message_LinearLayout = myFragmentView.findViewById(R.id.my_show_message_LinearLayout);
        View line_message_View = myFragmentView.findViewById(R.id.line_message_View);
        if (IS_SHOW_MY_MESSAGE) {
            message_show_ImageView.setImageResource(R.drawable.message_no_show);
            my_show_message_LinearLayout.setVisibility(View.GONE);
            line_message_View.setVisibility(View.GONE);
            IS_SHOW_MY_MESSAGE = false;
        } else {
            message_show_ImageView.setImageResource(R.drawable.message_show);
            my_show_message_LinearLayout.setVisibility(View.VISIBLE);
            line_message_View.setVisibility(View.VISIBLE);
            IS_SHOW_MY_MESSAGE = true;
        }
    }

    private void updateMyPhoto() {
        progressDialog = new ProgressDialog(context);
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            progressDialog.setCancelable(true);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.photo_view_progress_bar);
        }
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == 4) {
                    dialogInterface.dismiss();
                }
                return false;
            }
        });
        TextView photo_show_TextView = progressDialog.findViewById(R.id.photo_show_TextView);
        photo_show_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyPhoto(progressDialog);
            }
        });
        TextView album_TextView = progressDialog.findViewById(R.id.album_TextView);
        album_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                selectFromPhoto();
            }
        });
        TextView photo_TextView = progressDialog.findViewById(R.id.photo_TextView);
        photo_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                selectFromAlbum();
            }
        });
        TextView cancel_TextView = progressDialog.findViewById(R.id.cancel_TextView);
        cancel_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
            }
        });
    }

    private void selectFromAlbum() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
        } else {
            photoFromCapture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(context, "未找到图片查看器", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFromCapture();
                }
        }
    }

    private void selectFromPhoto() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Toast.makeText(context, "未找到图片查看器", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void photoFromCapture() {
        Intent intent;
        Uri pictureUri;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update");
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                }
            }
        }
        File pictureFile = new File(Environment.getExternalStorageDirectory().getPath()
                + "/tmp/update", "photo.png.cache");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileProvider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void showMyPhoto(ProgressDialog progressDialog) {
        progressDialog.dismiss();
        Intent intent = new Intent(context, PhotoShowActivity.class);
        intent.putExtra("flag", 1);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {

    }

    private void setSmallImageToImageView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photoBitmap = extras.getParcelable("data");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update");
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                    } else {
                        Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                    }
                }
                File file = new File(dirFile, "photo.png.cache");
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(file);
                    if (photoBitmap != null) {
                        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } else {
                        Log.e(TAG, "图片文件不存在！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            photo_my_ImageView.setImageBitmap(photoBitmap);
            uploadUpdatePhoto();
        }
    }

    private void uploadUpdatePhoto() {
        progressDialog.setCancelable(false);
        Window window = progressDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            progressDialog.setCancelable(false);
            progressDialog.show();
            progressDialog.setContentView(R.layout.loading_progress_bar);
            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
            prompt_TextView.setText("上传更新头像中......");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("id", SharedPreferencesUtils.getString(context, "id", "0", "user"));
                    File updatePhotoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update"
                            , "photo.png.cache");
                    Message message = new Message();
                    message.obj = new HttpUtil(context).upLoadImageFile(updatePhotoFile,
                            ActivityUtil.NET_URL + "/update_user_photo", parameter);
                    uploadUpdatePhotoHandler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startBigPhotoZoom(Uri uri) {
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update");
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                }
            }
            file = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update", "photo.png.cache");
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileProvider", file), "image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 500);
            intent.putExtra("outputY", 500);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
        } else {
            Toast.makeText(context, "剪切图片失败", Toast.LENGTH_LONG).show();
        }
    }

    private void setBigImageToImageView() {
        File photoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update", "photo.png.cache");
        Uri tempPhotoUri = Uri.fromFile(photoFile);
        try {
            Bitmap photoBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(tempPhotoUri));
            photo_my_ImageView.setImageBitmap(photoBitmap);
            uploadUpdatePhoto();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startSmallPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SMALL_IMAGE_CUTTING:
                    if (data != null) {
                        setSmallImageToImageView(data);
                    }
                    break;
                case REQUEST_BIG_IMAGE_CUTTING:
                    if (data != null) {
                        setBigImageToImageView();
                    }
                    break;
                case REQUEST_IMAGE_GET:
                    try {
                        if (data != null) {
                            startSmallPhotoZoom(data.getData());
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    File temp = new File(Environment.getExternalStorageDirectory().getPath()
                            + "/tmp/update", "photo.png.cache");
                    startBigPhotoZoom(Uri.fromFile(temp));
                    break;
                default:
                    break;
            }
        }
    }
}