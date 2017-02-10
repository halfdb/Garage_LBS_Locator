package ecnu.cs14.garagelocation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ecnu.cs14.garagelocation.data.Fingerprint;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public final class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    private MapView mMapView;
    private Button mSampleButton;
    private Button mSaveButton;
    private ProgressDialog waitingDialog;
    private Sniffer mSniffer;
    private final MainActivityHandler mHandler = new MainActivityHandler(this);


    private static final class MainActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivityRef;

        final static int MSG_SNIFFER = 0;
        final static int MSG_FINGERPRINT = 1;
        final static int MSG_POSITION_STRING = 2;

        MainActivityHandler(MainActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FINGERPRINT:
                {
                    mActivityRef.get().receiveFingerprint((Fingerprint) msg.obj);
                    break;
                }
                case MSG_SNIFFER:
                {
                    mActivityRef.get().receiveSniffer((Sniffer) msg.obj);
                    break;
                }
                case MSG_POSITION_STRING:
                {
                    mActivityRef.get().receivePositionString((String) msg.obj);
                    break;
                }
            }
        }
    }

    private void receiveSniffer(Sniffer sniffer) {
        if (sniffer == null) {
            Toast.makeText(this, "此环境无对应地图", Toast.LENGTH_LONG).show();
            return;
        }
        mSniffer = sniffer;
        mMapView.setVisibility(View.VISIBLE);
        mSampleButton.setVisibility(View.VISIBLE);
        mSampleButton.setClickable(true);
        mSaveButton.setVisibility(View.VISIBLE);
        mSaveButton.setClickable(true);
        mMapView.setMap(mSniffer.getMaps().get(mSniffer.getMapIndex()));
    }

    private boolean mFingerprintUpdated = false;
    private Fingerprint mFingerprint;
    private void receiveFingerprint(Fingerprint fingerprint) {
        mFingerprint = fingerprint;
        mFingerprintUpdated = true;
        if (mPositionUpdated) {
            storeSample();
        }
    }

    private Pair<Integer, Integer> mPosition;
    private boolean mPositionUpdated = false;
    private void receivePositionString(String string) {
        String[] stringPair = string.split("\\s");
        if (stringPair.length == 2) {
            mPosition = new Pair<>(
                    Integer.valueOf(stringPair[0]),
                    Integer.valueOf(stringPair[1])
            );
            if (mPosition.first != null && mPosition.second != null) {
                mPositionUpdated = true;
            }
        }
        if (!mPositionUpdated) {
            showPositionInputDialog();
        } else if (mFingerprintUpdated) {
            storeSample();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    msg.obj = new Sniffer(MainActivity.this);
                } catch (NullPointerException e) {
                    msg.obj = null;
                }
                msg.what = MainActivityHandler.MSG_SNIFFER;
                mHandler.sendMessage(msg);
            }
        }).start();
        tryRequestPermissions();

        waitingDialog = new ProgressDialog(this);
        waitingDialog.setMessage("请稍候");
        waitingDialog.setCancelable(false);
        waitingDialog.setIndeterminate(true);

        mMapView = (MapView) findViewById(R.id.map_view);
        if (mMapView == null) {
            finish();
        }
        View progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            mMapView.setEmptyView(progressBar);
        }
        mMapView.setVisibility(View.GONE);

        mSampleButton = (Button) findViewById(R.id.sample_button);
        if (mSampleButton == null) {
            finish();
        }
        mSampleButton.setClickable(false);
        mSampleButton.setVisibility(View.GONE);
        mSaveButton = (Button) findViewById(R.id.save_button);
        if (mSaveButton == null) {
            finish();
        }
        mSaveButton.setClickable(false);
        mSaveButton.setVisibility(View.GONE);
    }

    public void takeSample(View v) {
        if (mSniffer == null) {
            return;
        }
        mFingerprintUpdated = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.obj = mSniffer.getFingerprint();
                msg.what = MainActivityHandler.MSG_FINGERPRINT;
                mHandler.sendMessage(msg);
            }
        }).start();
        waitingDialog.show();
        showPositionInputDialog();
    }

    private void showPositionInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setText("300 400");
        editText.selectAll();
        builder.setTitle("输入当前坐标")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message msg = new Message();
                        msg.obj = editText.getText().toString();
                        msg.what = MainActivityHandler.MSG_POSITION_STRING;
                        mHandler.sendMessage(msg);
                    }
                })
                .setCancelable(false)
                .show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask()   {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 500);
    }

    private void storeSample() {
        if (!mFingerprintUpdated || !mPositionUpdated) {
            return;
        }
        mMapView.drawSampleDot(mPosition);
        mSniffer.storeSample(mPosition, mFingerprint);
        mFingerprintUpdated = false;
        mPositionUpdated = false;
        waitingDialog.dismiss();
    }

    public void saveMap(View view) {
        mSniffer.save();
    }

    @Override
    public void onBackPressed() {
        if (!mSniffer.needsSaving()) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("是否保存采集的数据？")
                .setCancelable(true)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveMap(null);
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void tryRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        boolean requestNeeded = false;

        for (String permission :
                permissions) {
            requestNeeded |= (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED);
        }
        if (requestNeeded) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result :
                grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "未获得权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSniffer != null) {
            mSniffer.finish();
        }
    }
}
