package com.example.android_mission_11;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class MainActivity extends Activity
{
    TextView txtViewEnvoie;
    TextView txtViewDate ;
    TextView txtViewClient;
    TextView txtViewFormation ;

    TextView TxtMessage ;
    Button BtnEnvoyer ;
    EditText EditTxtMessage ;

    String lienClient = "http://192.168.1.27:8080/WebApplicationsSessionsAutorisees/webresources/SessionFormation/Inscription/Client";
    String lienFormation = "http://192.168.1.27:8080/WebApplicationsSessionsAutorisees/webresources/SessionFormation/nomFormation";
    URL urlCon;
    HttpURLConnection urlConnection;
    private static final int PERMISSION_REQUEST_CODE = 1;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialisations();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            {
                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {android.Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }
        }

        AccesWebServicesClient accesClient = new AccesWebServicesClient();
        AccesWebServicesFormation accesFormation = new AccesWebServicesFormation();

        LocalDate todaysDate = LocalDate.now();

        txtViewDate.setText("JOUR : " +  todaysDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE", java.util.Locale.FRENCH)));

        if(todaysDate.getDayOfWeek().getValue() == 1 )
        {
            txtViewEnvoie.setText("(Envoie Possible)");

            try // affiche formation de la semaine
            {
                String repFormation = accesFormation.execute().get();
                JSONArray jsonTab = new JSONArray(repFormation);

                String texte = "Formation de la semaine : ";
                String Libelle = "" ;

                for (int i=0; i < jsonTab.length(); i++)
                {
                    try
                    {
                        Libelle = jsonTab.getJSONObject(i).getString("libelle");

                        texte +=  Libelle + " | ";
                    }
                    catch (JSONException e)
                    { }
                }

                txtViewFormation.setText(texte);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try // affiche client inscript
            {
                String repClient = accesClient.execute().get();
                JSONArray jsonTab = new JSONArray(repClient);

                String texte = "Client Inscrit : ";
                String Nom = "" ;


                for (int i=0; i < jsonTab.length(); i++)
                {
                    try
                    {
                        Nom = jsonTab.getJSONObject(i).getString("nom");
                        texte +=  Nom + " | ";
                    }
                    catch (JSONException e)
                    { }
                }

                txtViewClient.setText(texte);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            BtnEnvoyer.setVisibility(View.VISIBLE);
            EditTxtMessage.setVisibility(View.VISIBLE);
            TxtMessage.setVisibility(View.VISIBLE);

            EditTxtMessage.setText("Rappelle Formation cette semaine !");

            BtnEnvoyer.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("deprecation")
                public void onClick(View v)
                {
                    String msg = EditTxtMessage.getText().toString();
                    AccesWebServicesClient accesClient2 = new AccesWebServicesClient();

                    try // Envoie un sms a tout les clients .
                    {
                        String repClient = accesClient2.execute().get();
                        JSONArray jsonTab = new JSONArray(repClient);
                        String Num = "" ;

                        for (int i=0; i < jsonTab.length(); i++)
                        {
                            try
                            {
                                Num = jsonTab.getJSONObject(i).getString("tel");
                                SmsManager.getDefault().sendTextMessage(Num, null, msg, null, null);
                            }
                            catch (JSONException e)
                            { }
                        }
                    }
                    catch (Exception e)
                    {e.printStackTrace();}

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setMessage("Rapelle envoyer !");

                    builder.setTitle("Info !");

                    builder.setPositiveButton("ok", (DialogInterface.OnClickListener) (dialog, which) -> {
                        //finish();
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
        else
        {
            txtViewEnvoie.setText("(Envoie Impossible)");
        }

    }
    public void initialisations()
    {
        txtViewEnvoie = (TextView) findViewById(R.id.txtViewEnvoie);
        txtViewDate = (TextView) findViewById(R.id.txtViewDate);
        txtViewFormation = (TextView) findViewById(R.id.txtViewFormation);
        txtViewClient = (TextView) findViewById(R.id.txtViewClient);
        BtnEnvoyer = (Button) findViewById(R.id.BtnEnvoyer) ;
        EditTxtMessage = (EditText) findViewById(R.id.EditTxtMessage);
        TxtMessage = (TextView) findViewById(R.id.TxtMessage);
    }

    // WEBSERVICE CLIENT
    private class AccesWebServicesClient extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Début du traitement asynchrone", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                urlCon = new URL(lienClient);
                urlConnection = (HttpURLConnection) urlCon.openConnection();
                // Transformation du HttpUrlConnection en chaine
                InputStream in = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(in));
                String retour = rd.readLine();
                return retour;
            }
            catch (Exception ex)
            {
                System.out.println("ERREUR ASYNCTASK : " + ex.getMessage());
                return null;
            }
        }
    }

    //WEBSERVICE FORMATION

    private class AccesWebServicesFormation extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Début du traitement asynchrone", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try
            {
                urlCon = new URL(lienFormation);
                urlConnection = (HttpURLConnection) urlCon.openConnection();
                // Transformation du HttpUrlConnection en chaine
                InputStream in = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(in));
                String retour = rd.readLine();
                return retour;
            }
            catch (Exception ex)
            {
                System.out.println("ERREUR ASYNCTASK : " + ex.getMessage());
                return null;
            }
        }
    }
}