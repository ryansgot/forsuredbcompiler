# forsuredbcompiler
forsuredbcompiler is Annotation processor for the forsuredb project that handles code and resource generation.
As the central part of the forsuredb project, it is one of libraries necessary for forsuredb to work.

## Quickstart

### The four library dependencies for your application
1. forsuredbcompiler (compile only)
2. forsuredbapi
3. A platform integration library (see https://github.com/ryansgot/forsuredbandroid for an example)
4. An SQL library conforming to the forsuredb standards (see https://github.com/ryansgot/forsuredbsqlitelib for an example). Libraries 3 and 4 can be combined, however.

### Using forsuredb in Android
- Create a new Android project (see https://github.com/ryansgot/forsuredbandroid for a sample app)
- Set up the project build.gradle repositories and dependencies like this:
```groovy
buildscript {
    repositories {
        jcenter() // <-- all jar/aar files for forsuredb are hosted on jcenter
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.0'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.6'  // <-- forsuredbcompiler needs this plugin to generate code
        classpath 'com.fsryan:forsuredbplugin:0.3.1'
    }
}

allprojects {
    repositories {
        jcenter() // <-- all jar/aar files for forsuredb are hosted on jcenter
    }
}
```
- Amend your app build.gradle file as such:
```groovy
apply plugin: 'com.android.application'
apply plugin: 'android-apt'             // <-- enables the forsuredbcompiler annotation processor
apply plugin: 'com.fsryan.forsuredb'    // <-- provides the dbmigrate task

/*...*/

dependencies {
    compile 'com.google.guava:guava:19.0'

    compile 'com.fsryan.forsuredb:forsuredbapi:0.8.1'           // common API for your code and the supporting libraries
    compile 'com.fsryan.forsuredb:sqlitelib:0.3.0'              // the SQLite DBMS integration
    compile 'com.fsryan.forsuredb:forsuredbandroid:0.8.0@aar'   // the Android integration and useful tools

    provided 'com.fsryan.forsuredb:forsuredbcompiler:0.8.0'     // these classes are not needed at runtime--they do code generation
    apt 'com.fsryan.forsuredb:forsuredbcompiler:0.8.0'          // runs the forsuredb annotation processor at compile time
}

forsuredb {
    // should be the same as the applicationId from the android extension
    applicationPackageName = 'com.fsryan.testapp'
    // the fully-qualified class name of the parameterization of the SaveResult.
    // If you have an Android project, this should be the result parameter.
    resultParameter = "android.net.Uri"
    // The fully-qualified class name of the parameterization of the generated
    // ForSure class. It is the class that stores a record before it is 
    // deleted/inserted/updated etc.
    recordContainer = "com.fsryan.provider.FSContentValues"
    // the assets directory of your app starting at your project's base directory
    migrationDirectory = 'app/src/main/assets'
    // Your app's base directory
    appProjectDirectory = 'app'
    // (optional) this is the directory in which your META-INF/services files will go for your custom plugins. Note that this is not the same directory as your Android resources (res)
    resourcesDircectory = 'app/src/main/resources'
    // (optional) fully-qualified class name of an implementation of FSJsonAdapterFactory. You must define both resourcesDirectory and fsJsonAdapterFactoryClass in order for your doc store to perorm custom JSON serialization
    fsJsonAdapterFactoryClass = 'com.my.application.json.AdapterFactory'
}
```
- Declare an application class and the ```FSDefaultProvider``` in your app's AndroidManifest.xml file:
```xml
<application
        android:name=".App" >
  <!-- ... -->
  <provider
      android:name="com.fsryan.forsuredb.provider.FSDefaultProvider"
      android:authorities="com.fsryan.testapp.content"
      android:enabled="true"
      android:exported="false" />
</application
```
- Create the App class that you declared above
```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // creates the tables based upon your FSGetApi extensions
        // pass your custom authority in to TableGenerator.generate() if you don't want the default
        FSDBHelper.init(this, "testapp.db", TableGenerator.generate());
        // the String is your Content Provider's authority
        ForSureAndroidInfoFactory.init(this, "com.fsryan.testapp.content")
        // ForSureAndroidInfoFactory tells ForSure everything it needs to know.
        ForSure.init(ForSureAndroidInfoFactory.inst());
    }
}
```
- Define an interface that extends FSGetApi
```java
@FSTable("user")
public interface UserTable extends FSGetApi {   // <-- you must extend FSGetApi when @FSTable annotates an interface or your app won't compile
    @FSColumn("global_id") long globalId(Retriever retriever);
    @FSColumn("login_count") int loginCount(Retriever retriever);
    @FSColumn("app_rating") double appRating(Retriever retriever);
    @FSColumn("competitor_app_rating") BigDecimal competitorAppRating(Retriever retriever);
}
```
- Migrate the database by using the dbmigrate gradle task (defined by forsuredbplugin)
```
./gradlew dbmigrate
```

## Using the Doc Store feature
Introduced in forsuredbapi-0.8.0, the doc store feature allows for a doc store interface regardless of whether it is backed by a real doc store implementation or by some relational database (as in the current sqlite version). Here are the main differences:
- Your ```@FSTable``` annotated interface must extend ```FSDocStoreGetApi``` instead of ```FSGetApi```.
- This interface must be parameterized with the most basic class (could be ```Object```) that will be stored in this table.
- This interface must have a ```public Class BASE_CLASS``` field that is the ```Class``` object of the most basic class that will be stored in this table.
- Any additional columns that you add must be fields of the base class (just the base class for now). These columns will be indices for fast lookup of records as well as fast retrieval of important data.
- Starting with forsuredbapi-0.8.1, you can use a custom ```Gson``` object to serialize/deserialize objects you have persisted. Use forsuredbplugin 0.3.1 and provide the ```resourcesDirectory``` and ```fsJsonAdapterFactoryClass``` properties to the ```forsuredb``` gradle extension. Note that the value of ```fsJsonAdapterFactoryClass``` must be the fully-qualified class name of an implementation of ```FSJsonAdapterFactory```.

## Supported Migrations
- Add a table
- Add a column to a table
- Add an index to a table (single-column only)
- Add a unique index column to a table
- Make an existing column a unique index
- Add a foreign key column to a table

## Coming up
- support for inverse migrations of each of the currently supported migrations (0.10.0)
- more robust where-clause editing when doing joins (?)
- Full separation from any DBMS by means of a DBMSIntegrator plugin (0.9.0)
- More Doc store support such as adding a migration for when you refactor class names of objects that you may store in the doc store (0.8.x)
- An Android Studio plugin (?)

## Revisions

### forsuredbapi-0.8.1
- Support for custom ```Gson``` objects by means of a plugin defined by the ```FSJsonAdapterFactory``` interface. You must use forsuredbplugin 0.3.1 or greater and add the ```resourcesDirectory``` and ```fsJsonAdapterFactoryClass``` properties to the ```forsuredb``` gradle extension (or really know what you're doing writing Java plugins).

### 0.8.0  (compiler and api)
- Added doc store feature that stores objects as JSON using ```Gson```. Instead of extending ```FSGetApi``` to define your table's schema, extend ```FSDocStoreGetApi``` and give it a parameter that is the most basic possible object that will get stored in this table. Additionaly, define a ```Class``` field called ```BASE_CLASS``` with the class that you used to parameterize the ```FSDocStoreGetApi``` extension.

### 0.7.1 (compiler and api)
- ```FSColumn``` annotation now has searchable and orderable properties in order to tell the compiler whether to generate the order-by and finder methods for a column in the fluent querying api. Setting these properties to ```false``` will cause the compiler to not generate the order by and finder methods associated with the column.

### 0.7.0 (compiler and api)
- Split into two modules: compiler and api
- No functional changes, but a lot of the cruft that was getting compiled into projects will now be removed

### 0.6.4
- No longer compile lombok into the compiler in order to avoid pulling in a bunch of stuff that is not needed
