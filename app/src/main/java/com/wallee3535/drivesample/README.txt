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

-I had to make both an API key and OAuth 2.0 client ID in console.developers.google.com Credentials,
 which registers the package name


 ExternalSaver is not used in the activity and can be copy and pasted to other projects.