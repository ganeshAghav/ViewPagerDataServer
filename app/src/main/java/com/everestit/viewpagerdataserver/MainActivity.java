package com.everestit.viewpagerdataserver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    ViewPager viewPager;
    MyCustomAdapter myCustomPagerAdapter;

    List<String> responseList;
    List<String> imagedata;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseList = new ArrayList<>();
        imagedata= new ArrayList<>();
        viewPager = (ViewPager)findViewById(R.id.viewPager);

        String devId="55:46:4F:F9:01:DB";
        backgroundTask backgroundTask=new backgroundTask(MainActivity.this);
        backgroundTask.execute(String.valueOf(devId));

    }
    public class backgroundTask extends AsyncTask<String, Void, String>
    {

        ProgressDialog myDailog=new ProgressDialog(MainActivity.this);
        Context context;

        public backgroundTask(Context cxt) {
            context = cxt;

        }

        @Override
        protected void onPreExecute()
        {

            myDailog.setMessage("Notification Loading Please wait...");
            myDailog.setIndeterminate(true);
            myDailog.setCancelable(false);
            myDailog.show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            Log.e("Notificatio", "In AsyncTask");

            String BeaconUid = params[0];
            String SOAP_ACTION = "http://tempuri.org/GetOffers";
            String OPERATION_NAME = "GetOffers";
            String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
            String SOAP_ADDRESS = "http://192.168.1.64:80/BeaconServiceNew/BeaconService.asmx";

            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE,OPERATION_NAME);

            PropertyInfo PI = new PropertyInfo();
            PI.setName("BeaconUid");
            PI.setValue(BeaconUid);
            PI.setType(String.class);
            request.addProperty(PI);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            String response = null;
            try
            {
                HttpTransportSE httpTransport = new
                        HttpTransportSE(SOAP_ADDRESS);
                httpTransport
                        .setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                httpTransport.debug = true;
                httpTransport.call(SOAP_ACTION, envelope);

                response = httpTransport.responseDump;
            }
            catch (Exception ex)
            {
                response = "<?xml version='1.0' encoding='utf-8'?>"
                        + "<root>"
                        + "<rootDetails>"
                        + "<Message>Failed</Message>"
                        + "<MessageText>The system is under maintenance or having some technical issues. We apologize for your inconvenience, please visit again in some time.</MessageText>"
                        + "</rootDetails>" + "</root>";


            }
            Log.e("Response",response.toString());
            return response;

        }

        @Override
        protected void onPostExecute(String result)
        {

            if (result != null)
            {
                xmlparser(result);
                myDailog.dismiss();
                Log.e("you are in ","onPostExecute");
            }
        }

        public void xmlparser(String xml)
        {
            Log.e("xmlParser","Xml Parser class");

            try
            {
                XMLParser parser = new XMLParser();
                Document doc = parser.getDomElement(xml);
                NodeList nl = doc.getElementsByTagName("OfferData");
                for (int i = 0; i < nl.getLength(); i++)
                {
                    Element e = (Element) nl.item(i);

                    responseList.add((parser.getValue(e, "OfferDetails")));
                    imagedata.add((parser.getValue(e, "Image")));

                }
                myCustomPagerAdapter = new MyCustomAdapter(MainActivity.this,responseList,imagedata);
                viewPager.setAdapter(myCustomPagerAdapter);

            } catch (Exception ex)
            {
                Log.e("Exception", ex.toString());
            }
        }
    }
}
