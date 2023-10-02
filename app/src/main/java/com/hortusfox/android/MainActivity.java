package com.hortusfox.android;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ImageView appImage;
    public SwipeRefreshLayout refresher;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    public static MainActivity instance = null;
    private SharedPreferences prefs = null;
    public static boolean refresherVisibility = true;
    public static boolean webAppLoaded = false;
    public static boolean appShutdown = false;
    private Handler swipeHandler;
    public static String lastLoadedUrl = "";
    public BottomNavigationView navigationView = null;
    public static boolean doNotDoubleLoad = false;
    public static boolean performMenuSelection = true;
    public static boolean subsOverlayVisibility = false;

    private boolean isURLReachable(String address)
    {
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            int code = connection.getResponseCode();
            return code == 200 || code == 403;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.prefs = getSharedPreferences("com.hortusfox.android", MODE_PRIVATE);

        this.webView = (WebView)findViewById(R.id.webview);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setSupportZoom(true);
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.getSettings().setGeolocationEnabled(true);
        this.webView.getSettings().setUserAgentString("com.hortusfox.android");

        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        this.webView.addJavascriptInterface(javaScriptInterface, "native");

        this.appImage = (ImageView)findViewById(R.id.imgAppIcon);

        webView.setVisibility(View.GONE);

        this.refresher = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        refresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        WebBackForwardList forwardList = MainActivity.instance.webView.copyBackForwardList();
                        if (forwardList.getCurrentIndex() == -1) {
                            MainActivity.instance.launchWebsite();
                        } else {
                            MainActivity.instance.webView.reload();
                        }
                        refresher.setRefreshing(false);
                    }
                }
        );

        navigationView = findViewById(R.id.bottomNav);
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu1) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(BuildConfig.BASE_URL + "/");
                    return true;
                } else if (item.getItemId() == R.id.menu2) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl("javascript:(function(){ document.getElementById('inpLocationId').value = 0; window.vue.bShowAddPlant = true; })();");
                    return true;
                } else if (item.getItemId() == R.id.menu3) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(BuildConfig.BASE_URL + "/tasks");
                    return true;
                } else if (item.getItemId() == R.id.menu4) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(BuildConfig.BASE_URL + "/search");
                    return true;
                } else if (item.getItemId() == R.id.menu5) {
                    if (MainActivity.doNotDoubleLoad) {
                        MainActivity.doNotDoubleLoad = false;
                        return true;
                    }
                    MainActivity.performMenuSelection = false;
                    webView.loadUrl(BuildConfig.BASE_URL + "/profile");
                    return true;
                }
                return false;
            }
        });

        this.swipeHandler = new Handler();
        final Runnable swipeRunnable = new Runnable() {
            @Override
            public void run() {
                if (MainActivity.refresherVisibility != refresher.isEnabled()) {
                    refresher.setEnabled(MainActivity.refresherVisibility);
                }

                if (MainActivity.subsOverlayVisibility) {
                    refresher.setEnabled(false);
                } else {
                    refresher.setEnabled(MainActivity.refresherVisibility);
                }

                swipeHandler.postDelayed(this, 500);
            }
        };
        this.swipeHandler.postDelayed(swipeRunnable, 1000);

        this.webView.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }

            public void onPageFinished(WebView view, String url) {
                if (MainActivity.this.webView.getVisibility() == View.GONE) {
                    MainActivity.this.webView.setVisibility(View.VISIBLE);
                    MainActivity.this.appImage.setVisibility(View.GONE);
                }

                MainActivity.lastLoadedUrl = url;

                if (!MainActivity.webAppLoaded) {
                    MainActivity.webAppLoaded = true;
                }

                view.loadUrl("javascript:(function(){ let app = document.getElementById('app'); if (app) { app.style.marginBottom = '50px'; } })();");
                view.loadUrl("javascript:(function(){ let nav = document.getElementsByTagName('nav'); if (nav[0].classList.contains('navbar')) { nav[0].style.backgroundColor = '#454545'; nav[0].classList.add('is-fixed-top'); } })();");
                view.loadUrl("javascript:(function(){ let sorting = document.getElementsByClassName('nav-sorting'); if (sorting !== null) { sorting[0].style.marginTop = '52px'; } })();");
                view.loadUrl("javascript:(function(){ let container = document.getElementsByClassName('container'); if (container) { container[0].style.marginTop = '45px'; } })();");
                view.loadUrl("javascript:(function(){ let modalCards = document.getElementsByClassName('modal-card'); if (modalCards) { for (let i = 0; i < modalCards.length; i++) { modalCards[i].style.height = '85%'; } } })();");

                if (MainActivity.performMenuSelection) {
                    if (url.equals(BuildConfig.BASE_URL + "/")) {
                        MainActivity.doNotDoubleLoad = true;
                        MainActivity.this.setOpenNavMenu(0);
                    } else if (url.equals(BuildConfig.BASE_URL + "/tasks")) {
                        MainActivity.doNotDoubleLoad = true;
                        MainActivity.this.setOpenNavMenu(2);
                    } else if (url.equals(BuildConfig.BASE_URL + "/search")) {
                        MainActivity.doNotDoubleLoad = true;
                        MainActivity.this.setOpenNavMenu(3);
                    } else if (url.equals(BuildConfig.BASE_URL + "/profile")) {
                        MainActivity.doNotDoubleLoad = true;
                        MainActivity.this.setOpenNavMenu(3);
                    }
                }

                MainActivity.performMenuSelection = true;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean shouldOverrideUrlLoading (WebView view,
                                                     WebResourceRequest request)
            {
                return false;
            }


        });

        webView.setWebChromeClient(new WebChromeClient() {

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }
        });   // End setWebChromeClient

        CookieManager.getInstance().setCookie(BuildConfig.BASE_URL + "/", "auth_token=" + BuildConfig.AUTH_TOKEN, new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean aBoolean) {
                launchWebsite();
            }
        });
    }

    public void launchWebsite()
    {
        Thread launcher = new Thread() {
            @Override
            public void run() {
                if (!isURLReachable(BuildConfig.BASE_URL + "/")) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.webAppLoaded = false;

                            try {
                                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                                dlgAlert.setMessage("Es konnte keine Verbindung zum Server aufgebaut werden. Bitte versuche es später erneut.");
                                dlgAlert.setTitle("Verbindungsfehler");
                                dlgAlert.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //dismiss the dialog
                                            }
                                        });
                                dlgAlert.create().show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(BuildConfig.BASE_URL + "/");
                        }
                    });
                }
            }
        };
        launcher.start();
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setOpenNavMenu(int menu) {
        if (menu == 0) {
            navigationView.setSelectedItemId(R.id.menu1);
        } else if (menu == 1) {
            navigationView.setSelectedItemId(R.id.menu2);
        } else if (menu == 2) {
            navigationView.setSelectedItemId(R.id.menu3);
        } else if (menu == 3) {
            navigationView.setSelectedItemId(R.id.menu4);
        } else if (menu == 4) {
            navigationView.setSelectedItemId(R.id.menu5);
        }
    }

    public View viewById(int id)
    {
        return findViewById(id);
    }

    public String getStringById(int id)
    {
        return getString(id);
    }

    public Object getSysService(String name)
    {
        return getSystemService(name);
    }

    private void downloadFromUrl(String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.allowScanningByMediaScanner();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url)));

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            Toast.makeText(MainActivity.this, "Item has been downloaded.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Oops! Something went wrong.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            if (MainActivity.lastLoadedUrl.equals(BuildConfig.BASE_URL + "/")) {
                super.onBackPressed();
                return;
            }

            this.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onDestroy()
    {
        MainActivity.appShutdown = true;
        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        MainActivity.appShutdown = true;
        super.onPause();
    }

    @Override
    public void onResume()
    {
        MainActivity.appShutdown = false;
        super.onResume();
    }
}

class JavaScriptInterface {

}