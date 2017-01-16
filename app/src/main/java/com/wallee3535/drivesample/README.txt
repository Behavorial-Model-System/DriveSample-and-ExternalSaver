Unintuitive Things You Need To Get Drive Api Running:

-need this in build.gradle (module) :
	compile 'com.google.android.gms:play-services-drive:9.6.1'

-need this in AndroidManifest.xml :
        <meta-data
            android:name="com.google.android.apps.drive.APP_KEY"
            android:value= "AIzaSyDqcPrKgAv0ULsHflHhjikV7Q0Kif8jo9A"/>
        <meta-data
            android:name="com.google.android.gms:play-services-auth:9.6.1"
            android:value= "AIzaSyDqcPrKgAv0ULsHflHhjikV7Q0Kif8jo9A"/>

-You have to do this on every new computer:
    -get your SHA1 fingerprint key
    -make both an API key and OAuth 2.0 client ID in console.developers.google.com -> Credentials
    -register the package name, ie com.wallee3535.drivesample
-Still need to look into whether these steps above are necessary in the final release


 ExternalSaver is not used in the activity and can be copy and pasted to other projects.
