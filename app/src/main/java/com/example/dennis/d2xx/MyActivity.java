package com.example.dennis.d2xx;

import android.app.Activity;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Activity of App
 */
public class MyActivity extends Activity {

    FT_Device ftDev = null;
    D2xxManager ftD2xx = null;
    int devCount = 0;
    int bytesRead = 0;
    byte[] readBuffer;

    ReadThread rt;
    boolean stopThread = false;

    boolean connectionOpened = false;

    SqlLiteMeasurementHelper dbMeasurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

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

        // Start/Stop Button pressed
        if (id == R.id.action_start_stop_measure) {

            //If conneciton closed, try opening connection
            if (!connectionOpened) {
                try {
                    ftD2xx = D2xxManager.getInstance(this);
                    devCount = ftD2xx.createDeviceInfoList(this);
                    readBuffer = new byte[8192];
                    if (devCount > 0) {
                        ftDev = ftD2xx.openByIndex(this, 0);

                        // Configure inteface
                        if (ftDev.isOpen()) {

                            ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

                            ftDev.setBaudRate(38400);

                            ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1,
                                    D2xxManager.FT_PARITY_NONE);

                            final byte XON = 0x11;    //* Resume transmission *//*
                            final byte XOFF = 0x13;

                            ftDev.setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, XON, XOFF);

                            item.setIcon(R.drawable.ic_action_stop);
                            item.setTitle(R.string.stop_measurement);
                            Toast.makeText(getApplicationContext(), "Connection openend", Toast.LENGTH_SHORT).show();
                            connectionOpened = true;

                            // Start the listener Thread.
                            stopThread = false;
                            rt = new ReadThread(handler);
                            rt.start();

                            return true;
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error opening Connection", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "No Device Connected", Toast.LENGTH_SHORT).show();
                    }
                } catch (D2xxManager.D2xxException e) {
                    e.printStackTrace();
                }
            }

            // If connection already opened, close connection
            else {
                ftDev.close();
                stopThread = true;
                Toast.makeText(getApplicationContext(), "Connection closed", Toast.LENGTH_SHORT).show();
                connectionOpened = false;

                item.setIcon(R.drawable.ic_action_play);
                item.setTitle(R.string.start_measurement);

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler which enables communication between main-Thread and ReadThread
     */
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what)
            {
                // Message is beeing send when ReadThread reads a Datapackage
                case 1:
                    if (bytesRead > 0) {
                        try {
                            String receivedBytes = new String(readBuffer);
                            String[] parsedValues = receivedBytes.split(";");

                            Measurement m = new Measurement(parsedValues);
                            if (!m.isErrorParsingNumbers()) {
                                printMeasurement(m);
                                InsertMeasurement im = new InsertMeasurement(m);
                                im.execute();
                            }
                        } finally {
                            readBuffer = new byte[8192];
                        }
                    }
            }
        }
    };

    /**
     * Class which Asyncly handles the insertion of a Measurement into to the SQLite DB.
     */
    private class InsertMeasurement extends AsyncTask<Void, Void, Void> {
        private Measurement measurement;
        private long rows;

        public InsertMeasurement(Measurement m) {
            this.measurement = m;
        }

        @Override
        protected Void doInBackground(Void... params) {
            dbMeasurements = new SqlLiteMeasurementHelper(getApplicationContext());
            dbMeasurements.insertMeasurement(measurement);
            rows = dbMeasurements.countRows();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //TODO Remove Toast which displays number of rows in DB
            Toast.makeText(getApplicationContext(), Long.toString(rows), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prints the Read values from one of the Enddevices
     * @param m Holds the measured values
     */
    private void printMeasurement(Measurement m) {

        if (m.getId() == 1) {
            TextView temp1 = (TextView) findViewById(R.id.txtTemperature1);
            temp1.setText(String.format("%.2f°C", m.getMedianTemperature()));

            TextView humidity1 = (TextView) findViewById(R.id.txtHumidity1);
            humidity1.setText(String.format("%.2f%%", m.getSht21Humidity()));

            TextView pressure1 = (TextView) findViewById(R.id.txtPressure1);
            pressure1.setText(String.format("%.2f hPa", m.getBmp180Pressure()));

            TextView updated1 = (TextView) findViewById(R.id.txtUpdated1);
            updated1.setText(convertUnixTimeToString(m.getMeasuredTime()));
        }
        else if (m.getId() == 2) {
            TextView temp2 = (TextView) findViewById(R.id.txtTemperature2);
            temp2.setText(String.format("%.2f°C", m.getMedianTemperature()));

            TextView humidity2 = (TextView) findViewById(R.id.txtHumidity2);
            humidity2.setText(String.format("%.2f%%", m.getSht21Humidity()));

            TextView pressure2 = (TextView) findViewById(R.id.txtPressure2);
            pressure2.setText(String.format("%.2f hPa", m.getBmp180Pressure()));

            TextView updated2 = (TextView) findViewById(R.id.txtUpdated2);
            updated2.setText(convertUnixTimeToString(m.getMeasuredTime()));
        }
        else if (m.getId() == 3) {
            TextView temp3 = (TextView) findViewById(R.id.txtTemperature3);
            temp3.setText(String.format("%.2f°C", m.getMedianTemperature()));

            TextView humidity3 = (TextView) findViewById(R.id.txtHumidity3);
            humidity3.setText(String.format("%.2f%%", m.getSht21Humidity()));

            TextView pressure3 = (TextView) findViewById(R.id.txtPressure3);
            pressure3.setText(String.format("%.2f hPa", m.getBmp180Pressure()));

            TextView updated3 = (TextView) findViewById(R.id.txtUpdated3);
            updated3.setText(convertUnixTimeToString(m.getMeasuredTime()));
        }
    }

    /**
     * Converts the Unixtime (Milliseconds) into Time.
     * @param unixTime The time value in Unix Time
     * @return Returns time in this Format: hh:mm:ss
     */
    private String convertUnixTimeToString(long unixTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(unixTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        //return hour + ":" + minute + ":" + second;
        return  String.format("%02d:%02d:%02d", hour, minute, second);
    }

    /**
     * This class listens to the Driver for incoming data.
     */
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

            // Clear data from driver buffer.
            ftDev.purge(D2xxManager.FT_PURGE_RX);
            ftDev.purge(D2xxManager.FT_PURGE_TX);

            // Let Thread Sleep for 10 ms
            while (!stopThread) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Check if Data is available to read from interface
                readcount = ftDev.getQueueStatus();
                bytesRead = readcount;

                // If size of incoming Data > 63 Bytes then a comlete package was send.
                // 63 Bytes size of one Datapackage
                if (readcount > 63 ) {
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

                    // Send message to main Thread
                    Message msg = mHandler.obtainMessage(1);
                    mHandler.sendMessage(msg);
                }
            }


        }
    }

}
