package com.example.bezru.converter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    private boolean fromItemChanged;
    private boolean toItemChanged;
    private double courseValue;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText currencyNumber;
    static CurrencyFullList currencyFullList;
    private Toast toast;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private IntentFilter intentFilter;
    private MyReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        receiver = new MyReceiver();
        currencyFullList = new CurrencyFullList(getApplicationContext());
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        if (savedInstanceState != null) {
        }
        fromItemChanged = true;
        toItemChanged = true;
        currencyNumber = findViewById(R.id.currencyNumber);

        //Item selected listener makes possible to change the converted value when changing the currency
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromItemChanged = true;
                onSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toItemChanged = true;
                onSelected();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        currencyNumber.addTextChangedListener(new TextWatcher() {
            private EditText resultValue = findViewById(R.id.currencyNumberResult);

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    resultValue.setText("");
                } else {
                    if (!isNetworkConnected()) {
                        if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(".")) {
                            if (spinnerFrom.getSelectedItem() != null && spinnerFrom.getSelectedItem() != null) {
                                if (spinnerFrom.getSelectedItem().toString().equals(spinnerTo.getSelectedItem().toString())) {
                                    resultValue.setText(currencyNumber.getText());
                                } else {
                                    String value = currencyFullList.getValue(spinnerFrom.getSelectedItem().toString(), spinnerTo.getSelectedItem().toString());
                                    if (value != null) {
                                        resultValue.setText(String.valueOf((Double.parseDouble(s.toString())) * Double.parseDouble(value)));
                                    } else {
                                        toast = Toast.makeText(getApplicationContext(), "Результат не может быть вычислен",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }
                            }
                        }
                    } else {
                        if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(".")) {
                            // create a new request to get data if currency names where changed
                            if ((fromItemChanged == true || toItemChanged == true)) {
                                changeData(s.toString(), resultValue);
                            } else {
                                //use the same value if the currencies were not changed
                                Double res = courseValue * Double.parseDouble(s.toString());
                                resultValue.setText(res.toString());
                            }
                        }
                    }
                }
            }
        });

    }

    private void onSelected() {
        if (currencyNumber != null) {
            if (!currencyNumber.getText().toString().equals("") &&
                    !currencyNumber.getText().toString().equals("."))
                if (!isNetworkConnected() && currencyNumber != null) {
                    EditText resultValue = findViewById(R.id.currencyNumberResult);
                    if (spinnerFrom.getSelectedItem().toString().equals(spinnerTo.getSelectedItem().toString())) {
                        resultValue.setText(currencyNumber.getText());
                    } else {
                        String value = currencyFullList.getValue(spinnerFrom.getSelectedItem().toString(), spinnerTo.getSelectedItem().toString());
                        if (value != null) {
                            resultValue.setText(String.valueOf((Double.parseDouble(currencyNumber.getText().toString())) * Double.parseDouble(value)));
                        } else {
                            toast = Toast.makeText(getApplicationContext(), "Результат не может быть вычислен",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                } else {
                    changeData(currencyNumber.getText().toString(),
                            findViewById(R.id.currencyNumberResult));
                }
        }
    }

    private void reloadToCachedValues() {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        System.out.println("NOOOT CONNNECTEEED");
        toast = Toast.makeText(getApplicationContext(), "Интернет соединение отсутствует",
                Toast.LENGTH_SHORT);
        toast.show();
        //todo: ограниченный список
        List<String> listOfCurrencies = currencyFullList.getShortCurrenciesNamesList();
        if (listOfCurrencies.size() != 0) {
            //if (currencyNumber != null) currencyNumber.setText("");
            setSpinnerAdapters(listOfCurrencies);
            System.out.println(listOfCurrencies);
        } else {
            toast = Toast.makeText(getApplicationContext(), "Данные отсутствуют. " +
                            "Повторите попытку при подключении к Интернету",
                    Toast.LENGTH_SHORT);
            toast.show();
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void reloadToUncachedValues() {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        List<String> listOfCurrencies = currencyFullList.getAllCurrenciesNames();
        if (listOfCurrencies.size() != 0) {
            setSpinnerAdapters(listOfCurrencies);
            //if data is not in cache, use AsyncTask to get the data using a request
        } else {
            try {
                GettingValuesAsyncTask asyncTask = new GettingValuesAsyncTask();
                asyncTask.execute();
                List<String> list = asyncTask.get();
                if (list != null) {
                    setSpinnerAdapters(list);
                }
                //cache data
                currencyFullList.addCurrencies(list);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void setSpinnerAdapters(List<String> list) {
        spinnerFrom.setEmptyView(spinnerFrom.getEmptyView());
        spinnerTo.setEmptyView(spinnerTo.getEmptyView());
        SpinnerAdapter adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
    }

    private void changeData(String s, EditText resultValue) {
        if (spinnerTo.getSelectedItem() != null && spinnerFrom.getSelectedItem() != null) {
            if (spinnerFrom.getSelectedItem().toString().equals(spinnerTo.getSelectedItem().toString())) {
                resultValue.setText(currencyNumber.getText());
            } else {
                ConvectValuesAsyncTask asyncTask = new ConvectValuesAsyncTask();
                asyncTask.execute(spinnerFrom.getSelectedItem().toString(), spinnerTo.getSelectedItem().toString());
                String result;
                try {
                    result = asyncTask.get();
                    if (result == null) {
                        toast = Toast.makeText(getApplicationContext(), "Результат не может быть вычислен",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        System.out.println("Result: " + result);
                        courseValue = Double.parseDouble(result);
                        String res = new DecimalFormat("#0.00").format(courseValue * Double.parseDouble(s));
                        resultValue.setText(res);
                        fromItemChanged = false;
                        toItemChanged = false;
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String actionOfIntent = intent.getAction();
            boolean isConnected = isNetworkConnected();
            if (actionOfIntent.equals(CONNECTIVITY_ACTION)) {
                if (isConnected) {
                    reloadToUncachedValues();
                    if (currencyFullList.getLastCachedDateTime() != null) {
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime update = currencyFullList.getLastCachedDateTime();
                        long minutes = ChronoUnit.MINUTES.between(update, now);
                        if (minutes > 60) {
                            currencyFullList.addCacheValues(currencyFullList.getShortCurrenciesNamesList());
                            toast = Toast.makeText(getApplicationContext(), "Подождите. Мы сохраняем данные, чтобы Вы могли работать оффлайн.", Toast.LENGTH_SHORT);
                        }
                    } else {
                        currencyFullList.addCacheValues(currencyFullList.getShortCurrenciesNamesList());
                        toast = Toast.makeText(getApplicationContext(), "Подождите. Мы сохраняем данные, чтобы Вы могли работать оффлайн.", Toast.LENGTH_SHORT);

                    }
                } else {
                    reloadToCachedValues();
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}


