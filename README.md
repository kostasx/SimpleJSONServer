**_What?_** A Simple CORS-enabled Web Server in Java that serves a JSON file. 

**_Why?_** To test Ajax requests via JavaScript

**_How?_**

`git clone git@github.com:kostasx/SimpleJSONServer.git` 

`javac SimpleJSONServer.java`

`java SimpleJSONServer`

Then go to: *http://localhost:8080*

Or use JavaScript: 

```
fetch("http://localhost:8080")
.then( res => res.json() )
.then( data => console.log( data ) )
.catch( error => console.error( error ) );
```

**_What?_** You can change the data served by adding custom JSON data into the data.json file
