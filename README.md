# forsuredbcompiler
forsuredbcompiler is Annotation processor for the forsuredb project that handles code and resource generation.
As the central part of the forsuredb project, it is one of libraries necessary for forsuredb to work.

## The three libraries you need for an application
1. forsuredbcompiler
2. A platform integration library (see https://github.com/ryansgot/forsuredbandroid for an example)
3. An SQL library conforming to the forsuredb standards (see https://github.com/ryansgot/forsuredbsqlitelib for an example). Libraries 2 and 3 can be combined, however.

## Using forsuredb in Android
- Create a new Android project (see https://github.com/ryansgot/forsuredbandroid for a sample app)
- Set up the project build.gradle repositories and dependencies like this:
```groovy
buildscript {
    repositories {
        jcenter() // <-- all jar/aar files for forsuredb are hosted on jcenter
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.2.3'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.6'  // <-- forsuredbcompiler needs this plugin to generate code
        classpath 'com.fsryan:forsuredbplugin:0.1.1'
    }
}

allprojects {
    repositories {
        jcenter() // <-- all jar/aar files for forsuredb are hosted on jcenter
        maven {
            url  "http://dl.bintray.com/ryansgot/maven"   // <-- com.fsryan.forsuredb.sqlitelib will be on jcenter soon
        }
    }
}
```
- Amend your app build.gradle file as such:
```groovy
apply plugin: 'com.android.application'
apply plugin: 'android-apt'             // <-- enables the forsuredbcompiler annotation processor
apply plugin: 'com.fsryan.forsuredb'    // <-- provides the dbmigrate task

android {
    packagingOptions {
        // the forsuredbcompiler project uses project lombok, which also has the below file
        exclude 'META-INF/services/javax.annotation.processing.Processor'
    }
}

dependencies {
    compile 'com.google.guava:guava:18.0'
    compile 'com.fsryan:forsuredbandroid:0.4.0@aar'
    
    apt 'com.fsryan:forsuredbcompiler:0.4.0'
}

forsuredb {
    // should be the same as the applicationId from the android extension
    applicationPackageName = 'com.forsuredb.testapp'
    // the fully-qualified class name of the parameterization of the SaveResult.
    // If you have an Android project, this should be the result parameter.
    resultParameter = "android.net.Uri"
    // The fully-qualified class name of the parameterization of the generated
    // ForSure class. It is the class that stores a record before it is 
    // deleted/inserted/updated etc.
    recordContainer = "com.forsuredb.provider.FSContentValues"
    // the assets directory of your app starting at your project's base directory
    migrationDirectory = 'app/src/main/assets'
    // Your app's base directory
    appProjectDirectory = 'app'
}
```
- Declare an application class and the ```FSDefaultProvider``` in your app's AndroidManifest.xml file:
```xml
<application
        android:name=".App" >
  <!-- ... -->
  <provider
      android:name="com.forsuredb.provider.FSDefaultProvider"
      android:authorities="com.forsuredb.testapp.content"
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
        ForSureAndroidInfoFactory.init(this, "com.forsuredb.testapp.content")
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
## Supported Migrations
- Add a table
- Add a column to a table
- Add a unique index column to a table
- Make an existing column a unique index
- Add a foreign key column to a table

## Coming up
- support for inverse migrations of each of the currently supported migrations
- support for more types of migrations
- an example java (non-Android) project and corresponding forsuredbjava library
- more robust where-clause editing when doing joins
- A solution for the issue adding multiple ```@ForeignKey``` annotations to the same ```FSGetApi``` extension at once
