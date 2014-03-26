In this document, "your server" refers to the server-side software 
that you must implement to use Amazon Device Messaging(ADM - https://developer.amazon.com/sdk/adm.html) services.

== Server ==

Server: A self-contained web sample application written as a Python
script. The web application simulates a range of tasks your server
could implement to send messages to client applications. The server
runs on port 80 by default.

There are two main classes in server.py:
SampleWebApp: handles the logic for interacting with ADM 
services, as well as keeping a list of all devices that have been 
registered with the server.

ServerHandler: handles the minimal tasks required to process incoming
and outgoing web requests and responses. It also generates a very
simple html GUI.

The web application exposes the following routes:
/: displays 'Server running'.
/register: accepts registration IDs from instances of your app and 
registers them with your server.
/show-devices: returns html GUI that displayes all the devices registered 
with your server and that allows you to send custom messages to them. 
/sendmsg: Sends a message to ADM Servers for relaying to an instance of 
your app.

To run the server perform the following actions: 
1. Change the value of PORT at the beginning of server.py to the port you 
   would like the server to listen on. Make sure this port is opened and
   accessible before proceeding.
2. Change the values of PROD_CLIENT_ID and PROD_CLIENT_SECRET to the ones
   you received from Amazon. These are also located at the beggining of
   server.py
3. Run from the command line:
    > python server.py
4. See it in browser: http://localhost:port
5. Register a device: http://localhost:4000/register?device=device_id
6. List registered devices: http://localhost:4000/show-devices 
