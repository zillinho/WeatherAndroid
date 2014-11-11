package com.example.dennis.d2xx;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyActivity extends Activity {

    FT_Device ftDev = null;
    D2xxManager ftD2xx = null;
    int devCount = 0;
    int bytesRead = 0;
    byte[] readBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        try {
            ftD2xx = D2xxManager.getInstance(this);
            devCount = ftD2xx.createDeviceInfoList(this);
            readBuffer = new byte[8192];
            if (devCount > 0) {
                ftDev = ftD2xx.openByIndex(this, 0);


                if (ftDev.isOpen()) {
                    TextView txt = (TextView) findViewById(R.id.txtRead);
                    txt.append("\nftDev is Open\n");

                    ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

                    ftDev.setBaudRate(38400);

                    ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1,
                            D2xxManager.FT_PARITY_NONE);

                    final byte XON = 0x11;    /* Resume transmission */
                    final byte XOFF = 0x13;

                    ftDev.setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, XON, XOFF);

                    txt.append("\nftDev configured\n");

                    ReadThread rt = new ReadThread(handler);
                    rt.start();

                }
                else {
                    TextView txt = (TextView) findViewById(R.id.txtRead);
                    txt.append("ftDev not Open");
                }

            }
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what)
            {
                case 1:
                    TextView txt = (TextView) findViewById(R.id.txtRead);
                    ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);

                    //////// Helligkeit regeln //////////////
                    /*if (bytesRead == 8) {
                        //char c = (char) readBuffer[2];
                        try {
                            String read = (new String(readBuffer, "UTF-8"));
                            Pattern p = Pattern.compile("\\d+");
                            Matcher m = p.matcher(read);
                            while (m.find()) {
                                int value = Integer.parseInt(m.group());
                                txt.append(Integer.toString(value) + "\n");
                                changeScreenBrightness(value);
                            }
                            //readBuffer = new byte[8192]

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }*/
                    if (bytesRead > 0) {
                        txt.append(Integer.toString(bytesRead) + " Bytes read\n");
                        try {
                            String receivedBytes = new String(readBuffer);
                            String[] parsedValues = receivedBytes.split(";");
                            //txt.append(parsedValues[3] + " LEngth: " + parsedValues[3].length() + " Length Trimmed:" + parsedValues[3].trim().length() + "\n");
                            //int v = Integer.parseInt(parsedValues[0].trim());
                            //txt.append(parsedValues[3].trim() +  "." + parsedValues[4].trim() + "\n");
                            //double lm73 = Double.parseDouble(parsedValues[3].trim() + "." + parsedValues[4].trim());
                            Measurement m = new Measurement(parsedValues);
                            printMeasurement(m);

                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);

                        } finally {
                            readBuffer = new byte[8192];
                        }
                    }
            }
        }
    };

    private void printMeasurement(Measurement m) {
        TextView txt = (TextView) findViewById(R.id.txtRead);
        txt.append("ID: " + m.getId() + "\n");
        txt.append("SHT21 Temperature: " + m.getSht21Temperature() + "\n");
        txt.append("SHT21 Humidity: " + m.getSht21Humidity() + "\n");
        txt.append("LM73 Temperature: " + m.getLm73Temperature() + "\n");
        txt.append("BMP180 Temperature: " + m.getBmp180Temperature() + "\n");
        txt.append("BMP180 Pressure: " + m.getBmp180Pressure() + "\n");
        txt.append(String.format("Median Temperature: %.2f \n", m.getMedianTemperature()));
        txt.append("\n");

        TextView temp1 = (TextView) findViewById(R.id.txtTemperature1);
        temp1.setText(Double.toString(m.getMedianTemperature()));
    }

    private void changeScreenBrightness(int value) {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        if (value > 225)
            layout.screenBrightness = 0F;
        else if (value > 200)
            layout.screenBrightness = 0.25F;
        else if (value > 175)
            layout.screenBrightness = 0.33F;
        else if (value > 140)
            layout.screenBrightness = 0.5F;
        else if (value > 100)
            layout.screenBrightness = 0.75F;
        else if (value > 50)
            layout.screenBrightness = 0.85F;
        else
            layout.screenBrightness = 1F;

        getWindow().setAttributes(layout);

    }

    class HandlerThread extends Thread {
        Handler mHandler;

        HandlerThread(Handler h)
        {
            mHandler = h;
        }

        @Override
        public void run() {
            //super.run();
            byte status;
            Message msg;
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    class ReadThread extends Thread {
        final int USB_DATA_BUFFER = 8192;
        Handler mHandler;
        ReadThread(Handler h)
        {
            mHandler = h;
            this.setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            //super.run();

            byte[] usbdata = new byte[USB_DATA_BUFFER];
            int readcount = 0;
            int iWriteIndex = 0;
            boolean threadLoop = true;

            while (threadLoop) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                readcount = ftDev.getQueueStatus();
                bytesRead = readcount;
                if (readcount > 63 ) {
                    //TextView txt = (TextView) findViewById(R.id.txtRead);
                    //txt.append(Integer.toString(readcount));
                    if (readcount > USB_DATA_BUFFER) {
                        readcount = USB_DATA_BUFFER;
                    }
                    ftDev.read(usbdata, readcount);

                    for (int count = 0; count < readcount; count++)
                    {
                        readBuffer[iWriteIndex] = usbdata[count];
                        iWriteIndex++;
                        //iWriteIndex %= MAX_NUM_BYTES;
                    }
                    iWriteIndex = 0;
                    Message msg = mHandler.obtainMessage(1);
                    mHandler.sendMessage(msg);
                }
                /*else if (readcount > 70) {
                    if (readcount > USB_DATA_BUFFER) {
                        readcount = USB_DATA_BUFFER;
                    }
                    ftDev.read(usbdata, readcount);

                    for (int count = 0; count < readcount; count++)
                    {
                        readBuffer[iWriteIndex] = usbdata[count];
                        iWriteIndex++;
                        //iWriteIndex %= MAX_NUM_BYTES;
                    }
                    iWriteIndex = 0;
                }*/
            }
        }
    }

}
