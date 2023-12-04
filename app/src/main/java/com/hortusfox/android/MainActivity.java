package com.hortusfox.android;

import static com.google.android.material.badge.BadgeDrawable.TOP_END;
import static com.google.android.material.badge.BadgeDrawable.TOP_START;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ImageView appImage;
    public SwipeRefreshLayout refresher;
    public static BadgeDrawable badgeDrawable;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_CAMERA = 100;
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
    private Uri mCapturedImageURI;
    private File photoFile = null;
    public static String currentLang = "en";
    public static boolean switchLang = false;
    private Handler langHandler;

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

    public String getStringResource(String name)
    {
        @SuppressLint("DiscouragedApi")
        int id = getResources().getIdentifier(name, "string", getPackageName());
        return getResources().getString(id);
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
        this.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

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

        int menuItemId = navigationView.getMenu().getItem(2).getItemId();
        badgeDrawable = navigationView.getOrCreateBadge(menuItemId);
        badgeDrawable.setBadgeGravity(TOP_END);
        badgeDrawable.setVisible(false);

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

        this.langHandler = new Handler();
        final Runnable langRunnable = new Runnable() {
            @Override
            public void run() {
                if (MainActivity.switchLang) {
                    if (Objects.equals(MainActivity.currentLang, "de")) {
                        MainActivity.instance.navigationView.getMenu().getItem(0).setTitle("Home");
                        MainActivity.instance.navigationView.getMenu().getItem(1).setTitle("Hinzufügen");
                        MainActivity.instance.navigationView.getMenu().getItem(2).setTitle("Aufgaben");
                        MainActivity.instance.navigationView.getMenu().getItem(3).setTitle("Suche");
                        MainActivity.instance.navigationView.getMenu().getItem(4).setTitle("Profil");
                    } else {
                        MainActivity.instance.navigationView.getMenu().getItem(0).setTitle("Home");
                        MainActivity.instance.navigationView.getMenu().getItem(1).setTitle("Add");
                        MainActivity.instance.navigationView.getMenu().getItem(2).setTitle("Tasks");
                        MainActivity.instance.navigationView.getMenu().getItem(3).setTitle("Search");
                        MainActivity.instance.navigationView.getMenu().getItem(4).setTitle("Profile");
                    }

                    MainActivity.switchLang = false;
                }

                langHandler.postDelayed(this, 2000);
            }
        };
        this.langHandler.postDelayed(langRunnable, 1000);

        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (errorResponse.getStatusCode() == 403) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage(getStringResource("errorAccessForbiddenBody_" + MainActivity.currentLang));
                    dlgAlert.setTitle(getStringResource("errorAccessForbiddenTitle_" + MainActivity.currentLang));
                    dlgAlert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    dlgAlert.create().show();
                }

                super.onReceivedHttpError(view, request, errorResponse);
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
                view.loadUrl("javascript:(function(){ let modalCards = document.getElementsByClassName('modal-card'); if (modalCards) { for (let i = 0; i < modalCards.length; i++) { modalCards[i].style.maxHeight = '85%'; } } })();");
                view.loadUrl("javascript:(function(){ window.native.setTaskCount(window.currentOpenTaskCount); })();");
                view.loadUrl("javascript:(function(){ let elapp = document.getElementById('app'); if (elapp) { let c = document.createElement('span'); c.id = 'scroller-top'; elapp.insertBefore(c, elapp.firstChild); } })();");
                view.loadUrl("javascript:(function(){ let scroll = document.getElementsByClassName('scroll-to-top'); if (scroll !== null) { scroll[0].style.bottom = '69px'; let inner = document.querySelector('.scroll-to-top-inner'); if (inner) { inner.innerHTML = '<a href=\"javascript:void(0);\" onclick=\"document.querySelector(\\'#scroller-top\\').scrollIntoView({behavior: \\'smooth\\'});\"><i class=\"fas fa-arrow-up fa-2x up-color\"></i></a>'; } } })();");
                view.loadUrl("javascript:(function(){ let radio = document.getElementsByTagName('input'); for (let i = 0; i < radio.length; i++) { if (radio[i].type === 'radio') { radio[i].style.position = 'relative'; radio[i].style.top = '4px'; } } })();");
                view.loadUrl("javascript:(function(){ let file = document.getElementsByTagName('input'); for (let i = 0; i < file.length; i++) { if (file[i].type === 'file') { file[i].accept = 'image/*;capture=camera'; } } })();");
                view.loadUrl("javascript:(function(){ window.native.setCurrentLanguage(window.currentLocale); })();");

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
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                uploadMessage = filePathCallback;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    File folder = new File(getApplicationContext().getFilesDir(), "captured");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }

                    photoFile = new File(folder, "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    mCapturedImageURI = FileProvider.getUriForFile(getApplicationContext(), "com.hortusfox.android.provider", photoFile);

                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                    Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(contentIntent, getStringResource("selectMedia_" + MainActivity.currentLang));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] { captureIntent });

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                }

                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });   // End setWebChromeClient

        launchWebsite();
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
                                dlgAlert.setMessage(getStringResource("errorNoConnectionBody_" + MainActivity.currentLang));
                                dlgAlert.setTitle(getStringResource("errorNoConnectionTitle_" + MainActivity.currentLang));
                                dlgAlert.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                dlgAlert.create().show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, getStringResource("errorNoConnectionTitle_" + MainActivity.currentLang), Toast.LENGTH_LONG).show();
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

        if (requestCode == REQUEST_SELECT_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getStringResource("errorPermissionRequestDenied_" + MainActivity.currentLang), Toast.LENGTH_LONG).show();
            }
        }
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
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == uploadMessage)
                return;

            Uri[] results = null;

            if (resultCode == RESULT_OK) {
                results = (intent == null) ? new Uri[] {mCapturedImageURI} : new Uri[] {intent.getData()};
            }

            uploadMessage.onReceiveValue(results);

            uploadMessage = null;
            mCapturedImageURI = null;
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

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.CAMERA }, REQUEST_SELECT_CAMERA);
        }
    }
}

class JavaScriptInterface {
    @JavascriptInterface
    public void setTaskCount(int count)
    {
        if (count > 0) {
            MainActivity.badgeDrawable.setNumber(count);
            MainActivity.badgeDrawable.setVisible(true);
        } else {
            if (MainActivity.badgeDrawable.isVisible()) {
                MainActivity.badgeDrawable.setVisible(false);
            }
        }
    }

    @JavascriptInterface
    public void setCurrentLanguage(String lang)
    {
        MainActivity.currentLang = lang;
        MainActivity.switchLang = true;
    }
}