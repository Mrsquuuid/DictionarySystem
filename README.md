COMP90015 Assignment1
This project is a multi client one server architecture, the main highlight is as follows:
1. NIO.
2. completed a built-in thread pool.
3. encapsulate transport protocol based on TCP.
4. Singleton mode of double detection lock.
5. ConcurrentHashMap and AtomicInteger.

Client:

When the server is online, enter the correct IP address and port number of the server, and click the connect button to connect to the dictionary server, then the user can see the following six interactive buttons.
Query: query the meaning of word. If this word exists in the dictionary, you will get the correct meaning. If the word does not exist, server will return the corresponding prompt.
Add: add a new word and its meaning. If the word already exists, the server does not execute and prompts that the word exists.
Update: change the meaning of a word. If the word does not exist, the server will not execute the command and prompts the client that the word does not exist.
Remove: delete a word and its meaning. If the word does not exist, the server notifies the client.
Clear: clear words and answers in the input boxes.
Quit: disconnect from the server and exit the client.

Server:

The user can enter the appropriate port number and file path and click the start button to run the server (the program has built-in java – jar DictionaryServer.jar <port> <dictionary file> to run the server). Users can also click the terminate button to shut down the server.