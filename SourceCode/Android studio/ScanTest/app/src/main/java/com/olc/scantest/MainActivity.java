package com.olc.scantest;
import java.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.olc.scantest.mode.Barcodemode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.olc.scantest.uint.TxtReader;
import com.olc.scantest.uint.AsyncTaskExt;
import com.olc.scantest.uint.ScanHelper;
import com.olc.scantest.uint.SysBarcodeUtil;
import com.olc.scantest.uint.AsyncTaskExt.ASynCallBack;

public class MainActivity extends Activity implements View.OnClickListener {
    private int scanmode = -1;
    private boolean bleft = false, bright = false, bsound = false;
    private int Index = 1;
    private MuiltSelAdapter adapter;
    private List<Barcodemode> readermodes = new ArrayList<Barcodemode>();
    private String m_Broadcastname;
    private String[] ItemName = { "Exportdata", "Clear Data", "Help" };
    public static long lastTime;
    //* ****************************************************************
    private ListView list_code;
    private Button btn_savacode;
    private ImageButton btn_main_help;
    protected static final String action = "com.barcode.sendBroadcast";

    //************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();

    }

    private void initview() {
        ItemName[0] = getResources().getString(R.string.menuexport);
        ItemName[1] = getResources().getString(R.string.clean);
        ItemName[2] = getResources().getString(R.string.menuhelp);
        list_code = (ListView) findViewById(R.id.list_code);
        btn_savacode = (Button) findViewById(R.id.btn_savacode);
        btn_savacode.setOnClickListener(this);
        btn_main_help = (ImageButton) findViewById(R.id.btn_main_help);
        btn_main_help.setOnClickListener(this);
        adapter = new MuiltSelAdapter(this, readermodes);
        list_code.setAdapter(adapter);
    }

    public void StartMenus() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setItems(ItemName, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                System.out.println(arg1);
                if (arg1 == 0) {
                    ExtenddateToText();
                }
                if (arg1 == 1) {
                    readermodes.clear();
                    list_code.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                if (arg1 == 2) {
                    Intent intentTo = new Intent();
                    intentTo.setClass(MainActivity.this, HelpActivity.class);
                    startActivity(intentTo);
                }
                arg0.dismiss();
            }
        });
        builder.show();
    }
    private void sendKeyCode1(int keyCode) {
        try {
            String keyCommand = "input keycode " + keyCode;
            Runtime.getRuntime().exec(keyCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendKeyEvent(final int KeyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void ScanSetting() {
        // 0 : fast; 1 : slow; 2 : broadcast
        String version = android.os.Build.VERSION.RELEASE;
        if (version.equals("4.2.2")) {
            scanmode = SysBarcodeUtil.getBarcodeSendMode(MainActivity.this);
            bleft = SysBarcodeUtil.getLeftSwitchState(MainActivity.this);
            bright = SysBarcodeUtil.getRightSwitchState(MainActivity.this);
            if (!bleft) {
                SysBarcodeUtil.setLeftSwitchState(MainActivity.this, true);
            }
            if (!bright) {
                SysBarcodeUtil.setRightSwitchState(MainActivity.this, true);
            }
            if (scanmode != 2) {
                SysBarcodeUtil.setBarcodeSendMode(MainActivity.this, 2);
            }
        } else {
            scanmode = ScanHelper.getBarcodeReceiveModel(MainActivity.this);
            bleft = ScanHelper.getScanSwitchLeft(MainActivity.this);
            bright = ScanHelper.getScanSwitchRight(MainActivity.this);
            bsound = ScanHelper.getScanSound(MainActivity.this);
            if (!bsound) {
                ScanHelper.setScanSound(MainActivity.this, true);
            }
            if (!bleft) {
                ScanHelper.setScanSwitchLeft(MainActivity.this, true);
            }
            if (!bright) {
                ScanHelper.setScanSwitchRight(MainActivity.this, true);
            }
            if (scanmode != 2)
                ScanHelper.setBarcodeReceiveModel(MainActivity.this, 2);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        String version = android.os.Build.VERSION.RELEASE;
        if (version.equals("4.2.2")) {
            SysBarcodeUtil.setLeftSwitchState(MainActivity.this, bleft);
            SysBarcodeUtil.setRightSwitchState(MainActivity.this, bright);
            SysBarcodeUtil.setBarcodeSendMode(MainActivity.this, scanmode);
        } else {
            ScanHelper.setScanSwitchLeft(MainActivity.this, bleft);
            ScanHelper.setScanSwitchRight(MainActivity.this, bright);
            ScanHelper.setBarcodeReceiveModel(MainActivity.this, scanmode);
            ScanHelper.setScanSound(MainActivity.this, bsound);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter intentFilter = new IntentFilter();
        m_Broadcastname = "com.barcode.sendBroadcast";// com.barcode.sendBroadcastScan
        intentFilter.addAction(m_Broadcastname);
        registerReceiver(receiver, intentFilter);
        ScanSetting();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        String version = android.os.Build.VERSION.RELEASE;
        if (version.equals("4.2.2")) {
            SysBarcodeUtil.setLeftSwitchState(MainActivity.this, bleft);
            SysBarcodeUtil.setRightSwitchState(MainActivity.this, bright);
            SysBarcodeUtil.setBarcodeSendMode(MainActivity.this, scanmode);
        } else {
            ScanHelper.setScanSwitchLeft(MainActivity.this, bleft);
            ScanHelper.setScanSwitchRight(MainActivity.this, bright);
            ScanHelper.setBarcodeReceiveModel(MainActivity.this, scanmode);
            ScanHelper.setScanSound(MainActivity.this, bsound);
        }
    }
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(m_Broadcastname)) {
                String str = arg1.getStringExtra("BARCODE");
                btn_savacode.setClickable(true);
                if (!"".equals(str)) {
                    Barcodemode code = new Barcodemode();
                    code.setBarcode(str);
                    code.setNumber("");
                    IshavaCode(code, 1);
                }
            }
        }
    };
    private Boolean IshavaCode(Barcodemode code, int number) {
        int count = readermodes.size();
        int newcount = 0;
        for (int i = 0; i < count; i++) {
            if (readermodes.get(i).getBarcode().equals(code.getBarcode())) {
                newcount = Integer.parseInt(readermodes.get(i).getCountNo());
                if (newcount >= 2000000000) {
                    newcount = 0;
                    readermodes.get(i).setCountNo("0");
                }
                readermodes.get(i).setCountNo(
                        String.valueOf((newcount + number)));

                adapter = new MuiltSelAdapter(this, readermodes);
                list_code.setAdapter(adapter);
                return true;
            }
        }

        Barcodemode model = new Barcodemode();
        model.setBarcode(code.getBarcode());
        model.setNumber("" + Index++);
        model.setCountNo(String.valueOf(number));
        readermodes.add(model);
        adapter = new MuiltSelAdapter(this, readermodes);
        list_code.setAdapter(adapter);
        return false;
    }
    class MuiltSelAdapter extends BaseAdapter {
        private Context context;
        private HashMap<Integer, Boolean> isSelected;
        private LayoutInflater inflater = null;
        private List<Barcodemode> models = new ArrayList<Barcodemode>();
        @SuppressLint("UseSparseArrays")
        public MuiltSelAdapter(Context context, List<Barcodemode> models) {
            this.context = context;
            this.models = models;
            inflater = LayoutInflater.from(context);
            isSelected = new HashMap<Integer, Boolean>();
            initData(false);
        }
        public void initData(boolean flag) {
            for (int i = 0; i < models.size(); i++) {
                isSelected.put(i, flag);
            }
        }
        @Override
        public int getCount() {
            return models.size();
        }
        @Override
        public Object getItem(int arg0) {
            return models.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }
        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listviewitem, null);
                holder.tv_code = (TextView) convertView
                        .findViewById(R.id.tv_Code);
                holder.tv_number = (TextView) convertView
                        .findViewById(R.id.tv_Number);
                holder.tv_countno = (TextView) convertView
                        .findViewById(R.id.tv_CountNo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Barcodemode model = readermodes.get(position);
            holder.tv_code.setText(model.getBarcode());
            holder.tv_number.setText(model.getNumber());
            holder.tv_countno.setText(model.getCountNo());
            return convertView;
        }

        public HashMap<Integer, Boolean> getIsSelected() {
            return isSelected;
        }

        public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
            this.isSelected = isSelected;
        }

        class ViewHolder {
            TextView tv_number;
            TextView tv_code;
            TextView tv_countno;
        }
    }

    private void ExtenddateToText() {
        ASynCallBack mASynCallBack;

        mASynCallBack = new ASynCallBack() {
            final String FOLDER_PATH = "sdcard" + "/ScanCodeLogs/";
            String FILE_PATH = "";

            @SuppressLint("SimpleDateFormat")
            @Override
            public void start() {
                SimpleDateFormat sDateFormat = new SimpleDateFormat(
                        "yyyyMMddhhmmss");
                String filedate = sDateFormat.format(new java.util.Date());
                FILE_PATH = FOLDER_PATH + filedate + ".txt";
            }
            @Override
            public String run() {
                //
                if (readermodes != null && readermodes.size() > 0) {
                    for (int i = 0; i < readermodes.size(); i++) {
                        TxtReader.saveData2File(readermodes.get(i).getNumber()
                                + "   " + readermodes.get(i).getBarcode()
                                + "   " + readermodes.get(i).getCountNo()
                                + "\r\n", FILE_PATH, FOLDER_PATH);
                    }
                    return "OK";
                } else {
                    return "";
                }
            }
            @Override
            public void end(String str) {
                if ("OK".equals(str)) {
                    Toast.makeText(
                            MainActivity.this,
                            getResources().getString(R.string.savesuccess)
                                    + FILE_PATH, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.nodata,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        AsyncTaskExt.doAsync(mASynCallBack, this, "",
                getResources().getString(R.string.saveing));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_savacode:
                // sendKeyEvent(220);
                Intent intent = new Intent();
                intent.setAction("com.barcode.sendBroadcastScan");
                sendBroadcast(intent);
                btn_savacode.setClickable(false);
                break;
            case R.id.btn_main_help:
                StartMenus();
                break;
            default:
                break;
        }
    }
    public boolean IsDoubClick() {
        boolean flag = false;
        long time = System.currentTimeMillis() - lastTime;
        if (time > 500) {
            flag = true;
        } else {
            flag = false;
        }
        lastTime = System.currentTimeMillis();
        return flag;
    }

}

