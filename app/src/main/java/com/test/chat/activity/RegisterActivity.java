package com.test.chat.activity;

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
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.test.chat.R;
import com.test.chat.util.HttpUtils;
import com.test.chat.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class RegisterActivity extends Activity implements View.OnClickListener {

    private static final String TAG = Utils.TAG;
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static String IMAGE_FILE_NAME = "photo.png.cache";
    private static String TMP_PHOTO_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/register";
    private static boolean IS_SET_PHOTO_FLAG = false;
    private ProgressDialog progressDialog;
    private TextView local_TextView;
    private CheckBox check_agree_CheckBox;
    private EditText phone_number_EditText;
    private EditText verification_code_EditText;
    private EditText register_password_EditText;
    private Button regain_verification_code_Button;
    private TextView regain_verification_code_TextView;
    private MyCount myCount;
    private ImageView set_photo_image_ImageView;
    private String phone;
    private String password;
    private String verificationCode;

    private Handler codeHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            try {
                final JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    initViewVerificationCode();
                    String verification_code = jsonObject.getString("verification_code");
                    Toast.makeText(RegisterActivity.this, "已经向您手机发送验证码，请查看", Toast.LENGTH_SHORT).show();
                    verificationCode = verification_code;
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    Toast.makeText(RegisterActivity.this, jsonObject.getString("status"), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Toast.makeText(RegisterActivity.this, "网络异常！", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private Handler registerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            progressDialog.dismiss();
                            finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Toast.makeText(RegisterActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(RegisterActivity.this, "网络异常！", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setCancelable(false);
        initViewRegister();
    }

    @Override
    public void onBackPressed() {
        exitRegister();
    }

    private void initViewRegister() {
        Log.e(TAG, "initViewRegister: " + getPackageName());
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("手机验证");
        LinearLayout password_setting_LinearLayout = findViewById(R.id.password_setting_LinearLayout);
        password_setting_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this, "请先验证你的手机号", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout photo_setting_LinearLayout = findViewById(R.id.photo_setting_LinearLayout);
        photo_setting_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this, "请先验证你的手机号", Toast.LENGTH_SHORT).show();
            }
        });
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        Button send_verification_code_Button = findViewById(R.id.send_verification_code_Button);
        send_verification_code_Button.setOnClickListener(this);
        TextView read_QQ_Server_TextView = findViewById(R.id.read_QQ_Server_TextView);
        read_QQ_Server_TextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        read_QQ_Server_TextView.setOnClickListener(this);
        local_TextView = findViewById(R.id.local_TextView);
        TextView local_setting_TextView = findViewById(R.id.local_setting_TextView);
        local_setting_TextView.setOnClickListener(this);
        check_agree_CheckBox = findViewById(R.id.check_agree_CheckBox);

        phone_number_EditText = findViewById(R.id.phone_number_EditText);
        phone_number_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                phone_number_EditText.setSelection(phone_number_EditText.getText().length());
                phone_number_EditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(getCurrentFocus(), 0);
                }
            }
        }, 100);
    }

    private void initViewVerificationCode() {
        setContentView(R.layout.verification_code);
        Button submit_code_Button = findViewById(R.id.submit_code_Button);
        submit_code_Button.setOnClickListener(this);
        regain_verification_code_TextView = findViewById(R.id.regain_verification_code_TextView);
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("手机验证");
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        // TODO
//        myCount = new RegisterActivity.MyCount(10000, 1000);
        myCount = new RegisterActivity.MyCount(6 * 10000, 1000);
        myCount.start();

        regain_verification_code_Button = findViewById(R.id.regain_verification_code_Button);
        regain_verification_code_Button.setOnClickListener(this);
        LinearLayout password_setting_LinearLayout = findViewById(R.id.password_setting_LinearLayout);
        password_setting_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this, "请输入收到的验证码", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout photo_setting_LinearLayout = findViewById(R.id.photo_setting_LinearLayout);
        photo_setting_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this, "请输入收到的验证码", Toast.LENGTH_SHORT).show();
            }
        });
        verification_code_EditText = findViewById(R.id.verification_code_EditText);
        verification_code_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                verification_code_EditText.setSelection(verification_code_EditText.getText().length());
                verification_code_EditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(getCurrentFocus(), 0);
                }
            }
        }, 100);
    }

    private void initViewPasswordSet() {
        setContentView(R.layout.password_set);
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("设置密码");
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        LinearLayout photo_setting_LinearLayout = findViewById(R.id.photo_setting_LinearLayout);
        photo_setting_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this, "请设置登录密码", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout phone_verification_LinearLayout = findViewById(R.id.phone_verification_LinearLayout);
        phone_verification_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(register_password_EditText.getWindowToken(), 0);
                Window window = progressDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.exit_progress_bar);
                }
                TextView dialog_message_TextView = progressDialog.findViewById(R.id.dialog_message_TextView);
                dialog_message_TextView.setText(new String("点击后，需要重新验证手机号"));
                TextView cancel_exit_register_TextView = progressDialog.findViewById(R.id.cancel_exit_register_TextView);
                cancel_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                    }
                });
                TextView confirm_exit_register_TextView = progressDialog.findViewById(R.id.confirm_exit_register_TextView);
                confirm_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                        setContentView(R.layout.activity_register);
                        initViewRegister();
                    }
                });
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == 4) {
                            dialogInterface.dismiss();
                        }
                        return false;
                    }
                });
            }
        });
        register_password_EditText = findViewById(R.id.register_password_EditText);
        register_password_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                register_password_EditText.setSelection(register_password_EditText.getText().length());
                register_password_EditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(getCurrentFocus(), 0);
                }
            }
        }, 100);

        Button submit_set_photo_button = findViewById(R.id.submit_set_photo_button);
        submit_set_photo_button.setOnClickListener(this);
    }

    private void initViewPhotoSet() {
        setContentView(R.layout.photo_set);
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("设置头像");
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        LinearLayout phone_verification_LinearLayout = findViewById(R.id.phone_verification_LinearLayout);
        phone_verification_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(register_password_EditText.getWindowToken(), 0);
                Window window = progressDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setCancelable(true);
                    progressDialog.setContentView(R.layout.exit_progress_bar);
                }
                TextView dialog_message_TextView = progressDialog.findViewById(R.id.dialog_message_TextView);
                dialog_message_TextView.setText(new String("点击后，需要重新验证手机号"));
                TextView cancel_exit_register_TextView = progressDialog.findViewById(R.id.cancel_exit_register_TextView);
                cancel_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                    }
                });
                TextView confirm_exit_register_TextView = progressDialog.findViewById(R.id.confirm_exit_register_TextView);
                confirm_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                        setContentView(R.layout.activity_register);
                        deletePhotoCacheFile();
                        initViewRegister();
                    }
                });
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == 4) {
                            progressDialog.dismiss();
                        }
                        return false;
                    }
                });
            }
        });
        LinearLayout password_setting_LinearLayout = findViewById(R.id.password_setting_LinearLayout);
        password_setting_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(register_password_EditText.getWindowToken(), 0);
                Window window = progressDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setCancelable(true);
                    progressDialog.setContentView(R.layout.exit_progress_bar);
                }
                TextView dialog_message_TextView = progressDialog.findViewById(R.id.dialog_message_TextView);
                dialog_message_TextView.setText(new String("点击后，需要重新设置登录密码"));
                TextView cancel_exit_register_TextView = progressDialog.findViewById(R.id.cancel_exit_register_TextView);
                cancel_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                    }
                });
                TextView confirm_exit_register_TextView = progressDialog.findViewById(R.id.confirm_exit_register_TextView);
                confirm_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                        setContentView(R.layout.password_set);
                        deletePhotoCacheFile();
                        initViewPasswordSet();
                    }
                });
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == 4) {
                            progressDialog.dismiss();
                        }
                        return false;
                    }
                });
            }
        });
        set_photo_image_ImageView = findViewById(R.id.set_photo_image_ImageView);
        set_photo_image_ImageView.setOnClickListener(this);

        Button submit_register_Button = findViewById(R.id.submit_register_Button);
        submit_register_Button.setOnClickListener(this);
    }

    private void showSelectLocal() {
        final ProgressDialog localPhoneProgressDialog = new ProgressDialog(RegisterActivity.this);
        Window window = localPhoneProgressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setCancelable(true);
            localPhoneProgressDialog.show();
            localPhoneProgressDialog.setContentView(R.layout.local_phone_progress_bar);
        }
        TextView _86_TextView = localPhoneProgressDialog.findViewById(R.id._86_TextView);
        _86_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localPhoneProgressDialog.dismiss();
                local_TextView.setText(new String("+86中国大陆"));
            }
        });

        TextView _853_TextView = localPhoneProgressDialog.findViewById(R.id._853_TextView);
        _853_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localPhoneProgressDialog.dismiss();
                local_TextView.setText(new String("+853中国澳门"));
            }
        });

        TextView _852_TextView = localPhoneProgressDialog.findViewById(R.id._852_TextView);
        _852_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localPhoneProgressDialog.dismiss();
                local_TextView.setText(new String("+852中国香港"));
            }
        });

        TextView _886_TextView = localPhoneProgressDialog.findViewById(R.id._886_TextView);
        _886_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localPhoneProgressDialog.dismiss();
                local_TextView.setText(new String("+886中国台湾"));
            }
        });

        TextView cancel_TextView = localPhoneProgressDialog.findViewById(R.id.cancel_TextView);
        cancel_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localPhoneProgressDialog.dismiss();
            }
        });
    }

    private void sendMobileNoCode() {
        phone = phone_number_EditText.getText().toString();
        //TODO
//        phone = "15079069986";
        if (!check_agree_CheckBox.isChecked()) {
            Toast.makeText(this, "请确认是否已经阅读《腾讯QQ服务条款》", Toast.LENGTH_SHORT).show();
        }
        if (!Utils.isMobileNO(phone)) {
            Toast.makeText(this, "请正确填写手机号，我们将向您发送一条验证码短信", Toast.LENGTH_SHORT).show();
            phone_number_EditText.requestFocus();
        }
        if (Utils.isMobileNO(phone) && check_agree_CheckBox.isChecked()) {
            InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(phone_number_EditText.getWindowToken(), 0);
            getMobileNoCode();
        }
    }

    private void getMobileNoCode() {
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setContentView(R.layout.loading_progress_bar);
        }
        TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
        prompt_TextView.setText("获取验证码中，请稍后......");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("phone", phone);
                Message message = new Message();
                message.obj = new HttpUtils(RegisterActivity.this)
                        .postRequest(Utils.NET_URL + "/phone_is_register_user", parameter);
                codeHandler.sendMessage(message);
            }
        }).start();
    }

    private void reGetMobileNoCode() {
        myCount.start();
        getMobileNoCode();
        regain_verification_code_TextView.setVisibility(View.VISIBLE);
        regain_verification_code_Button.setVisibility(View.GONE);
    }

    private void exitRegister() {
        InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (register_password_EditText != null) {
            inputMethodManager.hideSoftInputFromWindow(register_password_EditText.getWindowToken(), 0);
        }
        if (phone_number_EditText != null) {
            inputMethodManager.hideSoftInputFromWindow(phone_number_EditText.getWindowToken(), 0);
        }
        if (verification_code_EditText != null) {
            inputMethodManager.hideSoftInputFromWindow(verification_code_EditText.getWindowToken(), 0);
        }
        final ProgressDialog exitRegisterProgressDialog = new ProgressDialog(RegisterActivity.this);
        Window window = exitRegisterProgressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setCancelable(true);
            exitRegisterProgressDialog.show();
            exitRegisterProgressDialog.setContentView(R.layout.exit_progress_bar);
        }
        TextView cancel_exit_register_TextView = exitRegisterProgressDialog.findViewById(R.id.cancel_exit_register_TextView);
        cancel_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitRegisterProgressDialog.dismiss();
            }
        });
        TextView confirm_exit_register_TextView = exitRegisterProgressDialog.findViewById(R.id.confirm_exit_register_TextView);
        confirm_exit_register_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        exitRegisterProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == 4) {
                    dialogInterface.dismiss();
                }
                return false;
            }
        });
    }

    private void submitCode() {
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setContentView(R.layout.loading_progress_bar);
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == 4) {
                        progressDialog.dismiss();
                    }
                    return false;
                }
            });
        }
        TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
        prompt_TextView.setText("验证码验证中......");
        // TODO
//        verificationCode = new String("123456");
        String inputVerificationCode = verification_code_EditText.getText().toString();
        if (inputVerificationCode.equals(verificationCode)) {
            InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(verification_code_EditText.getWindowToken(), 0);
            initViewPasswordSet();
            myCount.cancel();
        } else {
            Toast.makeText(RegisterActivity.this, "验证码输入错误", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    private void submitSetPhoto() {
        progressDialog.setMessage("密码验证中......");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        EditText re_register_password_EditText = findViewById(R.id.re_register_password_EditText);
        password = register_password_EditText.getText().toString();
        String re_password = re_register_password_EditText.getText().toString();

        // TODO
//        password = "123456789";
//        re_password = "123456789";

        if (Utils.isPassword(password)) {
            if (password.equals(re_password)) {
                initViewPhotoSet();
            } else {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "密码格式错误，请输入8·12位字母和数字组合", Toast.LENGTH_LONG).show();
        }
        progressDialog.dismiss();
    }

    private void submitRegister() {
        if (IS_SET_PHOTO_FLAG) {
            Window window = progressDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER;
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.setContentView(R.layout.loading_progress_bar);
            }
            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
            prompt_TextView.setText("注册中，请稍后......");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "submitRegister: " + password);
                    Log.e(TAG, "submitRegister: " + phone);
                    File file = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
                    Log.e(TAG, "run: " + file);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("password", password);
                    parameter.put("phone", phone);
                    Message message = new Message();
                    message.obj = new HttpUtils(RegisterActivity.this).upLoadImageFile(file, Utils.NET_URL + "/register_user", parameter);
                    registerHandler.sendMessage(message);
                }
            }).start();
        } else {
            Toast.makeText(this, "未设置头像，请先设置头像！", Toast.LENGTH_LONG).show();
            setPhotoImageView();
        }
    }

    private void setPhotoImageView() {
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setCancelable(true);
            progressDialog.show();
            progressDialog.setContentView(R.layout.register_photo_progress_bar);
        }
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == 4) {
                    progressDialog.dismiss();
                }
                return false;
            }
        });
        TextView send_image_album_message_TextView = progressDialog.findViewById(R.id.send_image_album_message_TextView);
        send_image_album_message_TextView.setOnClickListener(this);
        TextView send_image_photo_message_TextView = progressDialog.findViewById(R.id.send_image_photo_message_TextView);
        send_image_photo_message_TextView.setOnClickListener(this);
    }

    private void selectFromPhoto() {
        progressDialog.dismiss();
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Toast.makeText(RegisterActivity.this, "未找到图片查看器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectFromAlbum() {
        progressDialog.dismiss();
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
        } else {
            photoFromCapture();
        }
    }


    private void photoFromCapture() {
        Intent intent;
        Uri pictureUri;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(TMP_PHOTO_FILE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                } else {
                    Log.e("TAG", "文件夹创建成功");
                }
            }
        }
        File pictureFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileProvider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        Log.e(TAG, "photoFromCapture: " + pictureFile.getAbsolutePath());

        Log.e(TAG, "photoFromCapture: " + pictureUri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
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

    private void setSmallImageToImageView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dirFile = new File(TMP_PHOTO_FILE_PATH);
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.e("TAG", "文件夹创建失败");
                    } else {
                        Log.e("TAG", "文件夹创建成功");
                    }
                }
                File file = new File(dirFile, IMAGE_FILE_NAME);
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(file);
                    assert photo != null;
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            set_photo_image_ImageView.setImageBitmap(photo);
            IS_SET_PHOTO_FLAG = true;
        }
    }

    private void startBigPhotoZoom(Uri uri) {
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(TMP_PHOTO_FILE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                } else {
                    Log.e("TAG", "文件夹创建成功");
                }
            }
            file = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(FileProvider.getUriForFile(RegisterActivity.this,
                    getPackageName() + ".fileProvider", file), "image/*");
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
            Toast.makeText(RegisterActivity.this, "剪切图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setBigImageToImageView() {
        File photoFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
        Uri tempPhotoUri = Uri.fromFile(photoFile);
        try {
            Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(tempPhotoUri));
            set_photo_image_ImageView.setImageBitmap(photo);
            IS_SET_PHOTO_FLAG = true;
        } catch (FileNotFoundException e) {
            IS_SET_PHOTO_FLAG = false;
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
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
                    File temp = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
                    startBigPhotoZoom(Uri.fromFile(temp));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(RegisterActivity.this, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFromCapture();
                }
        }
    }

    private void toReadQQServer() {
        InputMethodManager inputMethodManager = (InputMethodManager) RegisterActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(phone_number_EditText.getWindowToken(), 0);
        Intent intent = new Intent(RegisterActivity.this, WebNetActivity.class);
        intent.putExtra("url", "https://www.qq.com/contract.shtml");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_setting_TextView:
                showSelectLocal();
                break;
            case R.id.title_left_ImageView:
                exitRegister();
                break;
            case R.id.send_verification_code_Button:
                sendMobileNoCode();
                break;
            case R.id.regain_verification_code_Button:
                reGetMobileNoCode();
                break;
            case R.id.submit_code_Button:
                submitCode();
                break;
            case R.id.submit_set_photo_button:
                submitSetPhoto();
                break;
            case R.id.set_photo_image_ImageView:
                setPhotoImageView();
                break;
            case R.id.read_QQ_Server_TextView:
                toReadQQServer();
                break;
            case R.id.submit_register_Button:
                submitRegister();
                break;
            case R.id.send_image_album_message_TextView:
                selectFromPhoto();
                break;
            case R.id.send_image_photo_message_TextView:
                selectFromAlbum();
                break;
            default:
                break;
        }
    }

    private void deletePhotoCacheFile() {
        File photoCacheFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
        IS_SET_PHOTO_FLAG = false;
        if (photoCacheFile.delete()) {
            Log.e(TAG, "deletePhotoCacheFile: 临时图片删除成功");
        } else {
            Log.e(TAG, "deletePhotoCacheFile: 无生成的图片");
        }
    }

    @Override
    protected void onDestroy() {
        deletePhotoCacheFile();
        super.onDestroy();
    }

    public class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long second = millisUntilFinished / 1000;
            regain_verification_code_TextView.setText(new String(second + " 秒后可以重新获得验证码"));
            if (second == 10) {
                regain_verification_code_TextView.setText(new String(9 + " 秒后可以重新获得验证码"));
            }
        }

        @Override
        public void onFinish() {
            regain_verification_code_TextView.setVisibility(View.GONE);
            regain_verification_code_Button.setVisibility(View.VISIBLE);
        }
    }

}
