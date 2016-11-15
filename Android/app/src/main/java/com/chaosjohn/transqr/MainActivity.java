package com.chaosjohn.transqr;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://freebsd.chaosjohn.com:3000");
        } catch (URISyntaxException e) {
        }
    }

    EditText msg;
    TextView history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("chat message", String.valueOf(System.currentTimeMillis()));
            }
        });

        mSocket.connect();
        mSocket.on("response", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (0 < args.length && args[0] instanceof String)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(getApplicationContext(), args[0].toString(), Toast.LENGTH_LONG).show();
                            history.setText(history.getText().toString() + "\n" + args[0].toString());
                        }
                    });
            }
        });

        history = (TextView) findViewById(R.id.history);

        msg = (EditText)findViewById(R.id.msg);
        msg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String message = msg.getText().toString();
                    mSocket.emit("chat message", "[" + getDeviceName() + "]" + message);
                    history.setText(history.getText().toString() + "\n[Me]" + message);
                    msg.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("static-access")
    public static String getDeviceName(){
        return new Build().MODEL;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_disconnect) {
            mSocket.disconnect();
//            mSocket.connect();
            return true;
        } else if (id == R.id.action_connect) {
            mSocket.connect();
//            mSocket.connect();
            return true;
        } else if (id == R.id.action_reconnect) {
            final Handler reconnectHandler = new Handler();
            Runnable reconncetRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mSocket.connected())
                        reconnectHandler.postDelayed(this, 100);
                    else
                        mSocket.connect();
                }
            };
            mSocket.disconnect();
            reconnectHandler.post(reconncetRunnable);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
