package com.locname.distribution;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;



import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText inputEmail, inputPassword;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;

    private SharedPreferences mToken;
    private static String email, password;
    private static int status;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set inputLayout
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);

        // set EditText
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        /*
        * make check if there is token or not
        * */
        mToken = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);
        if (mToken.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null){
            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }



    }


    public void login(View view) {

        /*
        * check if email and password is valid
        * */
        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        /*
        * check if is online or not if online call server
        * */
        if (isOnline()) {
            email = inputEmail.getText().toString().trim();
            password = inputPassword.getText().toString().trim();

            requestData("http://distribution.locname.com/laravel/api/login");

        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }

    private void requestData(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Login...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
//                        flowerList = FlowerJSONParser.parseFeed(response);
//                        updateDisplay();
                        parseFeed(response);
                        pDialog.dismiss();

                        if (status == 200) {
                            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                            finish();
                        }else
                            return;

                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("email", email);
                map.put("password", password);

                return map;
            }


        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
    public void parseFeed(String content) {

        try {
            JSONObject jsonRootObject = new JSONObject(content);//done
            status = Integer.parseInt(jsonRootObject.optString("status").toString());

            if(status != 200){
                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                String mes = jsonObject.optString("message");
                Toast.makeText(LoginActivity.this, mes, Toast.LENGTH_SHORT).show();
            }else {
//                Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
//                startActivity(intent);
                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                String token = jsonObject.optString("access_token").toString();
                String user_name = jsonObject.optString("user_first_name");
                String user_last_name = jsonObject.optString("user_last_name");
               // Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
                //put sharedPreference
                SharedPreferences.Editor editor = mToken.edit();
                editor.putString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, token);
                editor.putString(ShareValues.APP_PREFERENCES_USER_NAME, user_name);
                editor.putString(ShareValues.APP_PREFERENCES_USER_LAST_NAME, user_last_name);
                editor.commit();
            }


        } catch (JSONException e) {
            e.printStackTrace();

        }

    }
    /*
    * check is email valid or not
    * */
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    /*
    *  set focus on specific view in this state edit text will focused
    * */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    /*
    * check is password valid or not
    * */

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }


    /*
    * text watcher
    * */
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.email:
                    validateEmail();
                    break;
                case R.id.password:
                    validatePassword();
                    break;
            }
        }
    }


}
