package com.adobe.phonegap.push;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;
import android.view.WindowManager;
;

/**
 * Created by gjm on 07/06/17.
 */

public class BlankActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(() -> {
            SystemClock.sleep(100);
            BlankActivity.this.runOnUiThread(() -> {

                if( isTaskRoot() ) {
                    String url ="pushboot://";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.putExtra(PushConstants.PUSH_START, true);
                    intent.setPackage(getPackageName());
                    startActivity(intent);
                }

                finish();
            });
        }).start();
    }
}