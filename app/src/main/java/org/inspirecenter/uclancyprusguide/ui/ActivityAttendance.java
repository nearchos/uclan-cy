package org.inspirecenter.uclancyprusguide.ui;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.inspirecenter.uclancyprusguide.R;
import org.inspirecenter.uclancyprusguide.data.TimetableSession;
import org.inspirecenter.uclancyprusguide.db.DatabaseHelper;

import java.util.Arrays;
import java.util.List;

/**
 * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
 */
public class ActivityAttendance extends AppCompatActivity {

    public static final String TAG = "uclan-cy";

    public static final String BARCODE_SCAN_ACTION = "com.google.zxing.client.android.SCAN";

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    private Button scanNfcButton;
    private Button scanBarcodeButton;
    private Spinner spinner;
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        this.scanNfcButton = (Button) findViewById(R.id.activity_attendance_scan_nfc);
        this.scanBarcodeButton = (Button) findViewById(R.id.activity_attendance_scan_barcode);
        this.spinner = (Spinner) findViewById(R.id.activity_attendance_spinner);
        this.label = (TextView) findViewById(R.id.activity_attendance_label);

        final ActionBar actionBar =  getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null)
        {
            Toast.makeText(this, R.string.Missing_NFC_hardware, Toast.LENGTH_SHORT).show();
            scanNfcButton.setEnabled(false);
//            finish();
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        resolveIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mAdapter != null) {
//            if (!mAdapter.isEnabled()) {
//                showWirelessSettingsDialog();
//            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }

        // check if barcode scanning is available and enable/disable the button
        scanBarcodeButton.setEnabled(isIntentAvailable(this, new Intent(BARCODE_SCAN_ACTION)));

        // edit the spinner to select session
        final TimetableSession [] timetableSessions = DatabaseHelper.getTimetableSessions();
        Arrays.sort(timetableSessions);
        final SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timetableSessions);
        spinner.setAdapter(spinnerAdapter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        print("action: " + action);
        Log.d(TAG, "action: " + action);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable [] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage [] messages;
            if (rawMessages != null) {
                messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                    print("i[" + i + "] -> " + messages[i]);
                }
            } else {
                // Unknown tag type
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                print("tag detected with id: " + getHex(id) + " (hex), " + getDec(id) + " (dec), " + getReversed(id) + " (reversed)");
            }
        }
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

//    private void showWirelessSettingsDialog() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(R.string.NFC_disabled);
//        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialogInterface, int i) {
//                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//            }
//        });
//        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialogInterface, int i) {
//                finish();
//            }
//        });
//        builder.create().show();
//    }

    public static final int BARCODE_SCAN_CODE = 42;

    public void scanNfc(View view) {

print("scanning nfc...");
        final boolean nfcExists = NfcAdapter.getDefaultAdapter(this) != null;
        final boolean nfcEnabled = nfcExists && NfcAdapter.getDefaultAdapter(this).isEnabled();
        final String message = nfcExists ? (nfcEnabled ? getString(R.string.NFC_enabled) : getString(R.string.NFC_disabled)) : getString(R.string.NFC_missing);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setIcon(R.drawable.ic_nfc_black_24dp)
                .setTitle(R.string.Nfc_scan)
                .setMessage(message)
                .setNeutralButton(R.string.Dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        if(nfcExists && !nfcEnabled) {
            builder.setPositiveButton(R.string.Enable, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                }
            });
        };
        builder.create().show();
    }

    public void scanBarcode(View view) {
        print("scanning barcode...");
        final Intent scanIntent = new Intent(BARCODE_SCAN_ACTION);
        startActivityForResult(scanIntent, BARCODE_SCAN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null && requestCode == BARCODE_SCAN_CODE) {
            final String barcode = intent.getStringExtra("SCAN_RESULT");
            Log.d(TAG, "extras: " + intent.getExtras());
            print("barcode scanner read: " + barcode);
        }
    }

    static public boolean isIntentAvailable(final Context context, final android.content.Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void print(final String message) {
        label.setText(message + "\n" + label.getText());
    }
}
