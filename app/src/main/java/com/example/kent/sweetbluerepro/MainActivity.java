package com.example.kent.sweetbluerepro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView label;
    private int deviceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.button = (Button)this.findViewById(R.id.button);
        this.label = (TextView)this.findViewById(R.id.label);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceCount = 0;
                label.setText("Scanning");

                scan();
            }
        });
    }

    private void scan()
    {
        final BleManagerConfig.ScanFilter scanFilter = new BleManagerConfig.ScanFilter()
        {
            @Override public BleManagerConfig.ScanFilter.Please onEvent(BleManagerConfig.ScanFilter.ScanEvent e)
            {
                return BleManagerConfig.ScanFilter.Please.acknowledge();
            }
        };

        final BleManager.DiscoveryListener discoveryListener = new BleManager.DiscoveryListener()
        {
            @Override public void onEvent(DiscoveryEvent e)
            {
                if( e.was(LifeCycle.DISCOVERED) || e.was(LifeCycle.REDISCOVERED) )
                {
                    e.device().connect(new BleDevice.StateListener()
                    {
                        @Override public void onEvent(StateEvent e)
                        {
                            if (e.didEnter(BleDeviceState.INITIALIZED))
                            {
                                ++deviceCount;
                                label.setText("Found " + deviceCount + " devices. Latest: " + e.device().getName_native());
                            }
                        }
                    });
                }
            }
        };

        BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter() {
            @Override
            public Please onEvent(BluetoothEnablerEvent e) {
                if (e.isDone()) {
                    BleManagerConfig config = new BleManagerConfig();
                    config.loggingEnabled = true;
                    e.bleManager().setConfig(config);
                    e.bleManager().setListener_UhOh(
                            new BleManager.UhOhListener() {
                                @Override
                                public void onEvent(UhOhEvent uhOhEvent) {
                                    Log.e("", "Uh-oh occurred: " + uhOhEvent.uhOh());
                                }
                            }
                    );

                    e.bleManager().stopAllScanning();
                    e.bleManager().startScan(scanFilter, discoveryListener);
                }

                return super.onEvent(e);
            }
        });
    }
}