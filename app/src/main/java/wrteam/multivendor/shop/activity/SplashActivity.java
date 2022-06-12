package wrteam.multivendor.shop.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;


import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;

public class SplashActivity extends Activity {
    Session session;
    Activity activity;
    final int SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = SplashActivity.this;
        session = new Session(activity);
        session.setBoolean("update_skip", false);


        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            switch (data.getPath().split("/")[1]) {
                case "itemdetail": // Handle the item detail deep link
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id", data.getPath().split("/")[2]);
                    intent.putExtra(Constant.FROM, "share");
                    intent.putExtra("variantPosition", 0);
                    startActivity(intent);
                    finish();
                    break;
                case "refer": // Handle the refer deep link
                    if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                        Constant.FRIEND_CODE_VALUE = data.getPath().split("/")[2];
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", Constant.FRIEND_CODE_VALUE);
                        assert clipboard != null;
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(SplashActivity.this, R.string.refer_code_copied, Toast.LENGTH_LONG).show();
                        Intent referIntent = new Intent(this, LoginActivity.class);
                        referIntent.putExtra(Constant.FROM, "refer");
                        startActivity(referIntent);
                        finish();
                    } else {
                        new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
                        Toast.makeText(activity, activity.getString(R.string.msg_refer), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_splash);
            if (!session.getBoolean("is_first_time")) {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, WelcomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            } else {
                new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), SPLASH_TIME_OUT);
            }
        }
    }
}
