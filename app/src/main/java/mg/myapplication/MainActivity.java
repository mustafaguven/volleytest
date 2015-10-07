package mg.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    enum EndPointType{LOGIN, CUSTOMER_INFO, BASKET_COUNT, LOGOUT}
    String sessionId = "";

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        doLogin();

    }

    private void doLogin() {
        String url = "http://www.dr.com.tr/ApiCommon/Login/";
        String body = "{\n" +
                "\n" +
                "\"CheckoutAsGuest\":false,\n" +
                "\n" +
                "\"Email\":\"u.sargin@gmail.com\",\n" +
                "\n" +
                "\"UsernamesEnabled\":false,\n" +
                "\n" +
                "\"Username\":null, \n" +
                "\n" +
                "\"Password\":\"01135813\",\n" +
                "\n" +
                "\"RememberMe\":false,\n" +
                "\n" +
                "\"DisplayCaptcha\":false,\n" +
                "\n" +
                "\"CustomProperties\":{}\n" +
                "\n" +
                "}";
        sendRequest(EndPointType.LOGIN, url, body);
    }

    private void sendRequest(final EndPointType type, String url, String body) {


        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.POST, url, body, new Response.Listener<JSONObject>() {



                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        switch (type){
                            case LOGIN:
                                try {
                                    sessionId = response.getString("SessionObject");
                                    getCustomerInfo();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case CUSTOMER_INFO:
                                getBasketCount();
                                break;
                            case BASKET_COUNT:
                                try {
                                    ((TextView)(findViewById(R.id.lblBasketCount))).setText(response.getString("CartItemCount"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case LOGOUT:
                                break;
                            default:
                                break;
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });



        Volley.newRequestQueue(this).add(jsonRequest);
    }

    private Map<String, String> addHeaders() {

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", System.getProperty("http.agent"));
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }


    private void getBasketCount() {
        String url = "http://www.dr.com.tr/ApiCustomer/GetShoppingCartItemCount/?signedRequest=" + sessionId;
        try {
            url = URLEncoder.encode(url, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String body = "";
        sendRequest(EndPointType.BASKET_COUNT, url, body);
    }

    private void getCustomerInfo() {
        String url = "http://www.dr.com.tr/ApiCustomer/Info/?signedRequest=" + sessionId;
        String body = "";
        sendRequest(EndPointType.CUSTOMER_INFO, url, body);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
