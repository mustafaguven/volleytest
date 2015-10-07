package mg.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    enum EndPointType {LOGIN, CUSTOMER_INFO, BASKET_COUNT, LOGOUT}
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "DnR.customer";
    private SharedPreferences _preferences;
    String sessionId = "";
    Map<String, String> mHeaders = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _preferences = PreferenceManager.getDefaultSharedPreferences(this);

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

    private void sendRequest(final EndPointType type, final String url, String body) {


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {
                // the response is already constructed as a JSONObject!

                switch (type) {
                    case LOGIN:
                        try {
                            sessionId = response.getString("SessionObject");
                            Log.e("SENDREQUEST - LOGIN", type + " " + response.getString("Success") + " " + url);
                            getCustomerInfo();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case CUSTOMER_INFO:
                        try {
                            Log.e("SENDREQUEST - CUS_INFO", response.getString("CustomerInfo") + " " + url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getBasketCount();
                        break;
                    case BASKET_COUNT:
                        try {
                            Log.e("SENDREQUEST - BASKET", response.getString("success") + " " + url);
                            ((TextView) (findViewById(R.id.lblBasketCount))).setText(response.getString("CartItemCount"));
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

        }) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                checkSessionCookie(response.headers);
                //mHeaders = response.headers;
                return super.parseNetworkResponse(response);
            }

            /* (non-Javadoc)
             * @see com.android.volley.Request#getHeaders()
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();

                if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<>();
                }

                //headers.put("Accept", "application/json");
                //headers.put("Set-Cookie", "DnR.customer=173fb43c-ab20-422a-b45c-53229219c923; expires=Thu, 06-Oct-2016 14:57:12 GMT; path=/; HttpOnly");
                //headers.put("Content-Type", "application/json; charset=utf-8");
                //headers.put("Accept-Language", "en-US,en;q=0.8,en-GB;q=0.6,tr;q=0.4,ko;q=0.2,ro;q=0.2");
                addSessionCookie(headers);
/*                if(mHeaders!=null)
                    headers = mHeaders;*/

                return headers;
            }
        }



/*        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");

                return params;
            }
        }*/

                ;


        Volley.newRequestQueue(this).add(jsonRequest);
    }



    private void getBasketCount() {
        String url = "http://www.dr.com.tr/ApiCustomer/GetShoppingCartItemCount/?signedRequest=" + sessionId;
        String body = "";

        sendRequest(EndPointType.BASKET_COUNT, url, body);
    }

    private void getCustomerInfo() {
        String url = "http://www.dr.com.tr/ApiCustomer/info/?signedRequest=" + sessionId;
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


    public final void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)

                ) {
            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                SharedPreferences.Editor prefEditor = _preferences.edit();
                prefEditor.putString(SESSION_COOKIE, cookie);
                prefEditor.commit();
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String sessionId = _preferences.getString(SESSION_COOKIE, "");
        if (sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, "DnR.AUTH=89F62881F3911DB32EA3C8F5697E56A7D1BCEEE2337AB8B6C209ECED407E62A4E48787DBB66B62BABF4696A261C2A1E3980101AFF64DEE4FBE7125220C451D8D67BAB631274E73809852F05A9EEA2780385C4ED3CE16D49C99856ED80051AD1A9500432EC92903F5D3EC388ED739E269D1B2E82F0CAC4909FFBD46F41E37A502C1F4CAD0021E48235F181864E5E644334183DAA5AD03D29980DDB100E8564F50;  ");
        }
    }
}
