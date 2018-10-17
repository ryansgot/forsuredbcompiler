# forsuredbcompiler
forsuredbcompiler is Annotation processor for the forsuredb project that handles code and resource generation.
As the central part of the forsuredb project, it is one of libraries necessary for forsuredb to work. See the [forsuredb documentation website](http://forsuredb.org) for more details. 

## Version Compatibility

| Gradle Version | forsuredb compiler version |
| -------------- | -------------------------- |
| <= 3.x         | 0.11.0                     |
| >= 4.0         | 0.11.1 or greater          |

## Revisions

### 0.13.0
- Float and float datatype support
- BigInteger datatype support
- Removed transitive dependency in forsuredbapi on Gson
- dbinfo models split into separate library . . . pulled in transitively--you don't have to worry about it unless you're writing a plugin for forsuredb
- gson adapters for dbinfo models (forsuredbmodels-gson library): depend upon this library if you use Gson
- jackson serializer/deserializers for dbinfo models (forsuredbmodels-jackson library): depend upon this library if you use Jackson
- moshi adapters for dbinfo models (forsuredbmodels-moshi library): depend upon this library if you use Moshi
- Completely non-reflective Getter classes (note that reflection is still used to instantiate objects in document store tables--so do not obfuscate those class names)
- Completely non-reflective Setter classes (note that the class name of the serialized object is still stored as a string in the database--so do not obfuscate those class names)
- Non-Android Java integration via JDBC (forsuredbjdbc)
- forsuredbjdbc example application for manual testing
- arbitrary-depth document store indices
  * You can index any document store table by an arbitrarily deeply nested field using the `documentValueAccess` property of the `@FSColumn` annotation. See the javadocs for more info.
- Version and type-awareness for static data
  * For each version of the database, you can set static data to get inserted with the schema at that version
- Fixed SQL generation for limit/offset queries
- Started a documentation website: http://forsuredb.org
- Fixed bug where adding a unique column after adding the column for the first time kept generating new, equivalent migration json files
- Fixed bug where `@FSDefault` containing a single-quote character failed to generate the database
- Added annotation processor options prefixed with `forsuredb.`--you don't have to worry about this if you use the forsuredb gradle plugin

### 0.12.0
- Pagination of records via first/last method on the `Finder` class

### 0.11.0
- Removed dependency on Guava
- Added composite key support. Now you can create composite primary and foreign keys.

### forsuredbapi-0.10.1
- Fix issue wherein you had to join to another table in order to do a DISTINCT projection.

### 0.10.0
- You can now narrow down the columns returned by any query by calling the `Finder` methods `columns(String...)` or `distinct(String...)`. Calling `distinct(String...)` will query for distinct values. Note that if you call either of these methods, then your table API will contain methods that are invalid for the `Retriever` that gets returned when you call the `Resolver.get()` method.

### 0.9.5
- varargs argument when you want to query for different exact values of the same column. Previously, you had to store an intermediate reference to a Finder class, which, based upon the way resolvers nest, could be really difficult to find the correct type to reference.
- You can now pass multiple values to exact match methods in the case where you want to create something like:
```sql
SELECT * FROM table1 JOIN table2 ON table1._id = table2.table1_id WHERE (table1.column = 1 OR table1.column = 10 OR table1.column = 100) AND (table2.column = 2 OR table2.column = 20 OR table2.column = 200);
```

### 0.9.4
- more efficient Resolver class generation
- breaks API for your platform's use because the FSJoin class has changed.

### 0.9.3
- More robust querying API for finding/ordering by columns of joined tables.
- Generated Resolver classes are now abstract and have their own ```Base``` class declaration.

### 0.9.2
- You can call ```find()```/```orderBy()``` methods on resolvers for joined tables.
- There is no more ```RelationalResolver``` class--it was unnecessary.
- Order sometimes matters when performing a query

### 0.9.1
- You can call the join methods and ```find()```/```orderBy()``` methods to switch contexts in any order.

### 0.9.0
- Full separation from any DBMS by means of a DBMSIntegrator plugin. Use forsuredbplugin-0.4.0 in order to supply the correct class to the compiler via the forsuredb.dbmsIntegratorClass property in your build.gradle file.
- There was a pretty big problem when switching contexts between adding clauses to your query in the generated querying API. The ```andFinally()``` method is kind of misleading because it could appear several times. This has been changed to ```then()``` in an attempt to make things less confusing. If you're upgrading, you'll have to change all ```andFinally()``` methods to ```then()```

### forsuredbapi-0.8.2 and forsuredbcompiler-0.8.1
- Support for any kind of serialization you want (either to ```String``` or ```byte[]```), using whatever library you want or your own idea of what serialization should be.
- In order to take advantage of this, you should use forsuredbplugin 0.3.2 or greater

### forsuredbapi-0.8.1
- Support for custom ```Gson``` objects by means of a plugin defined by the ```FSJsonAdapterFactory``` interface. You must use forsuredbplugin 0.3.1 or greater and add the ```resourcesDirectory``` and ```fsSerializerFactoryClass``` properties to the ```forsuredb``` gradle extension (or really know what you're doing writing Java plugins).

### 0.8.0  (compiler and api)
- Added doc store feature that stores objects as JSON using ```Gson```. Instead of extending ```FSGetApi``` to define your table's schema, extend ```FSDocStoreGetApi``` and give it a parameter that is the most basic possible object that will get stored in this table. Additionaly, define a ```Class``` field called ```BASE_CLASS``` with the class that you used to parameterize the ```FSDocStoreGetApi``` extension.

### 0.7.1 (compiler and api)
- ```FSColumn``` annotation now has searchable and orderable properties in order to tell the compiler whether to generate the order-by and finder methods for a column in the fluent querying api. Setting these properties to ```false``` will cause the compiler to not generate the order by and finder methods associated with the column.

### 0.7.0 (compiler and api)
- Split into two modules: compiler and api
- No functional changes, but a lot of the cruft that was getting compiled into projects will now be removed

### 0.6.4
- No longer compile lombok into the compiler in order to avoid pulling in a bunch of stuff that is not needed
