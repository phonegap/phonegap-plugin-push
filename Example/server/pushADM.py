#
# Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# To run the server, perform the following actions:
# 1. Change the value of PORT to the port you would like the server 
#    to listen on. Make sure this port is opened and accessible before proceeding.
# 2. Change the values of PROD_CLIENT_ID and PROD_CLIENT_SECRET to the ones
#    you received from ADM.
# 3. Run
#     > python server.py
#

import SimpleHTTPServer
import SocketServer
import logging
from urlparse import urlparse,parse_qs
import json
import urllib
import urllib2
import threading
import datetime
import hashlib
import base64

# Port on which your server will be listening.
PORT = 4000

# Client ID received from ADM.
PROD_CLIENT_ID = "$CLIENT_ID"

# Client secret received from ADM.
PROD_CLIENT_SECRET = "$CLIENT_SECRET"

# Oauth2.0 token endpoint. This endpoint is used to request authorization tokens.
AMAZON_TOKEN_URL = "https://api.amazon.com/auth/O2/token"

# ADM services endpoint. This endpoint is used to perform ADM requests.
AMAZON_ADM_URL = "https://api.amazon.com/messaging/registrations/"

# Data used to request authorization tokens.
ACCESS_TOKEN_REQUEST_DATA = {
        "scope" : "messaging:push",
        "grant_type" : "client_credentials",
        "client_secret" : PROD_CLIENT_SECRET,
        "client_id": PROD_CLIENT_ID
        }

# JSON used to perform EnqueueMessage requests.
JSON_MSG_REQUEST = {
        "data" : {},
        "consolidationKey" : "",
        "expiresAfter" : "",
        "md5" : ""
        }

class SampleADMWebapp:
    """
    SampleADMWebapp is a class that handles the logic for sending messages to the ADM server
    as well as the tasks involved in calling ADM services.
    """
    def __init__(self):
        """
        SampleADMWebapp constructor.
        self.devices is a dictionary of registration IDs registered with your server.
        self.token_lock is a lock which will be used to block execution on self.token_data.
        self.token_data contains the data returned with an authorization token request response.
        """
        self.devices = dict()
        self.token_lock = threading.Lock()
        self.request_token()

    def register_device(self, url_device):
        """
        Registers an instance of our app with this web application and sends it a confirmation
        message. The registration id is required to communicate with our app.
        
        Args:
            url_device: A url path containing an app's instance registration ID.

        Returns:
            {'status': 'ok'}
        """
        params =  parse_qs(url_device)
        device = params['device'][0]
        self.devices[device] = device
        print 'registering device ' + device
        self.send_message_to_device("You are registered", device, "Registration Confirmation", 3600)
        return {'status': 'ok'}

    def unregister_device(self, url_device):
        """
        Unregisters an instance of our app from this web application.
        The registration ID associated with this app instance should no longer be used.
        
        Args:
            url_device: A url path containing an app's instance registration ID.

        Returns:
            {'status': 'ok'}
        """
        params =  parse_qs(url_device)
        device = params['device'][0]
        print 'unregistering device ' + device
        if self.devices.has_key(device):
            self.devices.pop(device)
        return {'status': 'ok'}

    def query_devices(self):
        """
        Returns:
            The registration IDs registered with your server.
        """
        return self.devices

    def send_message_to_device(self, message, device, consolidationKey, expiresAfter):
        """
        Constructs and sends a request to ADM Servers to enqueue a message for delivery to a specific app instance.
        Updates registration id if a newer one is received with the ADM server response.

        Args:
            message: Message to send.
            device:  The registration ID the instance of the app to which the message is to be sent.
            consolidationKey: An arbitrary string used to indicate that multiple messages are logically the same.
            expiresAfter: The number of seconds that ADM must retain the message if the device is offline.

        Returns:
            A message string representative of the outcome of the call.
        """
        url = AMAZON_ADM_URL + device + '/messages'
        req = urllib2.Request(url)
        req.add_header('Content-Type', 'application/json')
        req.add_header('Accept', 'application/json')
        req.add_header('x-amzn-type-version', 'com.amazon.device.messaging.ADMMessage@1.0')
        req.add_header('x-amzn-accept-type', 'com.amazon.device.messaging.ADMSendResult@1.0')
        self.token_lock.acquire()
        req.add_header('Authorization', "Bearer " + self.token_data['access_token'])
        self.token_lock.release()

        timeStamp = str(datetime.datetime.now().isoformat(' '))
        JSON_MSG_REQUEST['data'] = {"message": message, "timeStamp": timeStamp}
        JSON_MSG_REQUEST['consolidationKey'] = consolidationKey
        JSON_MSG_REQUEST['expiresAfter'] = int(expiresAfter)
        JSON_MSG_REQUEST['md5'] = self.calculate_checksum(JSON_MSG_REQUEST['data'])
        print req.headers
        print req.get_full_url()
        print JSON_MSG_REQUEST
        try:
            # POST EnqueueMessage request to AMD Servers.
            response = urllib2.urlopen(req,json.dumps(JSON_MSG_REQUEST))

            # Retreiving Amazon ADM request ID. Include this with troubleshooting reports.
            X_Amzn_RequestId =  response.info().get('x-amzn-RequestId')

            # Retreiving the MD5 value computed by ADM servers. 
            MD5_from_ADM =  response.info().get('x-amzn-data-md5')
            print "ADM server md5_checksum " + MD5_from_ADM

            # Checking if the app's registration ID needs to be updated.
            response_data = json.load(response)
            canonical_reg_id = response_data['registrationID']
            if device != canonical_reg_id:
                print "Updating registration Id"
                if self.devices.has_key(device):
                    self.devices.pop(device)
                self.devices[canonical_reg_id] = canonical_reg_id 
            return 'Message sent.'
        except urllib2.HTTPError as e:
            error_reason = json.load(e)['reason']
            if e.code == 400:
                return 'Handle ' + str(e) + '. invalid input. Reason: ' + error_reason
            elif e.code == 401:
                return self.handle_invalid_token_error(e)
            elif e.code == 403:
                return 'Handle ' + str(e) + '. max rate exceeded. Reason: ' + error_reason
            elif e.code == 413:
                return 'Handle ' + str(e) + '. message greater than 6KB. Reason: ' + error_reason
            elif e.code == 500:
                return 'Handle ' + str(e) + '.  internal server error'
            elif e.code == 503:
                return self.handle_server_temporarily_unavailable_error(e)
            else:
                return 'Message was not sent', str(e)
        except urllib2.URLError as e:
            return 'Message was not sent', 'URLError: ' + str(e.reason)
        except urllib2.HTTPException as e:
            return 'Message was not sent', 'HTTPException: ' + str(e)
        except Exception as e:
            return 'Message was not sent', 'Exception: ' + str(e)

    def handle_invalid_token_error(self, error):
        """
        Handles 401 (invalid token error) raised in send_message_to_device().
        This assumes that the 401 error raised in send_message_to_device() 
        is due to an expired token. This won't help if the invalid token error
        is caused for other reasons.

        Args:
            error: HTTPError raised in send_message_to_device().

        Returns:
            'Token refreshed. Please try again.'
        """
        self.request_token()
        return 'Token refreshed. Please try again.'

    def handle_server_temporarily_unavailable_error(self, error):
        """
        Handles 503 (server temporarily unavailable) raised in send_message_to_device().
        'Retry-After' header will either contain an integer in which case 'Retry-After'
        is a delay of time in seconds or a date in HTTP format in which case 
        'Retry-After' is the date and time at which it would be suggested to try again.

        Args:
            error: HTTPError raised in send_message_to_device().

        Returns:
            A message detailing when the send_message_to_device request should be attempted again.
        """
        retry_after = error.info().get('Retry-After')
        if retry_after.isdigit():
            return 'Please retry in ' + retry_after  + ' seconds'
        else:
            return 'Please retry at the following time: ' + retry_after

    def calculate_checksum(self, data):
        """
        Computes MD5 checksum of the 'data' parameter as per the algorithm detailed
        in the ADM documentation.

        Args:
            data: a dictionary.

        Returns:
            MD5 checksum of key/value pairs within data.
        """
        md5_checksum = ""
        utf8_data = dict()
        utf8_keys = []
        
        # Retreiving the list of keys in message.
        message_keys = data.keys()
        
        # Converting data to UTF-8.
        for key in message_keys:
            utf8_keys.append(key.encode('utf-8'))
            utf8_data[key.encode('utf-8')] = data[key].encode('utf-8')
        
        # UTF-8 sorting of the keys.
        utf8_keys.sort()
        utf8_string = ""
        
        # Concatenating the series of key-value pairs.
        for key in utf8_keys:
            utf8_string = utf8_string + key
            utf8_string = utf8_string + ':'
            utf8_string = utf8_string + utf8_data[key]
            if key != utf8_keys[-1]:
                utf8_string = utf8_string + ','

        # Computing MD5 as per RFC 1321.
        md5 = hashlib.md5(utf8_string).digest()

        # Base 64 encoding.
        md5_checksum = base64.standard_b64encode(md5)

        print "App server md5_checksum " + md5_checksum
        return md5_checksum

    def request_token(self):
        """
        Requests and stores an access token from the OAuth2.0 Servers.
        We must obtain an access token prior to sending a request to enqueue a message for delivery. 
        Also, when an access token expires, a new one is requested.
        """
        print 'Requesting token'
        req = urllib2.Request(AMAZON_TOKEN_URL)
        req_data = urllib.urlencode(ACCESS_TOKEN_REQUEST_DATA)
        print req_data, str(len(req_data))
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')

        try:
            self.token_lock.acquire()
            
            # POST access token request to OAuth2.0 Servers.
            response = urllib2.urlopen(req, req_data)
            
            # Retreiving Amazon ADM request ID. Include this with troubleshooting reports.
            X_Amzn_RequestId =  response.info().get('x-amzn-RequestId')
            
            self.token_data =  json.load(response)
            self.token_lock.release()

            print 'Token acquired: ' +  self.token_data['access_token'] + ' and valid for ' + \
                    str(self.token_data['expires_in']) + ' seconds.'
            response.close()
            interval = int(self.token_data['expires_in'])
            t = threading.Timer(interval, self.request_token)
            t.daemon = True
            t.start()
        except urllib2.HTTPError as e:
            self.token_lock.release()
            error=json.load(e)
            print 'Could not acquire token ', error
            exit()

class ServerHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):
    """
    Class ServerHandler performs the minimal tasks required to process
    web requests coming from our clients, as well as outgoing responses.
    """

    def send_server_response(self, content_type, content):
        """
        Sends your server's response.
        
        Args:
            content_type: Content type of response to send (text/html or application/json).
            content: Content to send back with the response.
        """
        self.send_response(200)
        self.send_header('Content-type', content_type)
        self.send_header('Content-length', len(content))
        self.end_headers()
        self.wfile.write(content)
        self.wfile.close()

    def send_html_response(self, html):
        """
        Sends an HTML response.
        
        Args:
            html: HTML response to send.
        """
        self.send_server_response('text/html', "<html>"+html+"</html>")

    def send_json_response(self, json):
        """
        Sends a JSON response.
        
        Args:
            json: JSON response to send.
        """
        self.send_server_response('application/json', json)

    def do_GET(self):
        """
        SimpleHTTPServer do_GET() implementation.
        This method gets called when your server receives GET requests.
        """
        self.route_request()

    def route_request(self):
        """
        All the routes handled by our web application are handled here.
        GUI HTML is generated here.
        """
        query = urlparse(self.path).query
        if self.path == "/":
            server_running = '<body>Server running</body>'
            self.send_html_response(server_running)
        elif self.path.startswith("/register"):
            ret = theWebApp.register_device(query) 
            self.send_json_response(json.dumps(ret))
        elif self.path.startswith("/unregister"):
            ret = theWebApp.unregister_device(query) 
            self.send_json_response(json.dumps(ret))
        elif self.path.startswith("/show-devices"):
            devices = theWebApp.query_devices()
            html = '<body>'
            html = html + '<h1>Select A Device And Send A Message</h1>'
            if len(devices) == 0:
                html = html + '<h2>No devices registered with server</h2>'
                html = html + 'Please register a device by restarting the Amazon ADM Sample App or registering from within the app</br>'
            else:
                html = html + '<form action="sendmsg" method="get">'
                html = html + '<table border="1">'
                for device in devices:
                    html = html + '<tr>'
                    html = html + '<td><input type="radio" name="device" value="'+device+'"></td>'
                    html = html + '<td><font size="1">'+device+'</font></td>'
                    html = html + '</tr>'
                html = html + '<tr>'
                html = html + '<td><font size="1">Message:</font></td>'
                html = html + '<td><input type="text" name="msg" size="355"/></td>'
                html = html + '</tr>'
                html = html + '<tr>'
                html = html + '<td><font size="1">Consolidation key:</font></td>'
                html = html + '<td><input type="text" name="consolidationKey" value="A consolidation key" size="355"/></td>'
                html = html + '</tr>'
                html = html + '<tr>'
                html = html + '<td><font size="1">Expires after:</font></td>'
                html = html + '<td><input type="text" name="expiresAfter" value="3600" size="355"/></td>'
                html = html + '</tr>'
                html = html + '</table>'
                html = html + '<input type="submit" value="Send Message"/></form></body>'
                html = html + '</form></body>'
            self.send_html_response(html)
        elif self.path.startswith("/sendmsg"):
            device=parse_qs(query)['device'][0]
            msg = parse_qs(query)['msg'][0]
            consolidationKey = parse_qs(query)['consolidationKey'][0]
            expiresAfter = parse_qs(query)['expiresAfter'][0]
            response = theWebApp.send_message_to_device(msg, device, consolidationKey, expiresAfter)
            print response
            self.send_json_response(response)
        else:
            self.send_html_response("not found")

# Instantiate a new global SampleADMWebapp.  
theWebApp = SampleADMWebapp()

# Instantiate a new ServerHandler and listen on port PORT.
httpd = SocketServer.TCPServer(("",PORT), ServerHandler)

# Listen forever.
print "starting server in port ", PORT
httpd.serve_forever()
