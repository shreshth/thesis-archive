from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from math import atan2

import entities

class AddDataApp(webapp.RequestHandler):
    def get(self):
        self.post()
    def post(self):
        # Read in comma seperated values and split them into arrays
        appName = str.split(str(self.request.get('appName')),',');
        uid = str.split(str(self.request.get('uid')),',');
        tcpRxBytes = str.split(str(self.request.get('tcpRxBytes')),',');
        tcpTxBytes = str.split(str(self.request.get('tcpTxBytes')),',');
        udpRxBytes = str.split(str(self.request.get('udpRxBytes')),',');
        udpTxBytes = str.split(str(self.request.get('udpTxBytes')),',');
        timestamp = str.split(str(self.request.get('timestamp')),',');
        
        # Read user id
        user_id = self.request.get('user_id');  
        
        # All arrays must exist
        if ((appName == ['']) or
        	(uid == ['']) or
        	(tcpRxBytes == ['']) or
        	(tcpTxBytes == ['']) or
        	(udpRxBytes == ['']) or
        	(udpTxBytes == ['']) or
        	(timestamp == ['']) or
        	(user_id == '')):    
            self.error(400);
            return;

        # All arrays must have the same size
        if ((len(appName) != len(uid)) or 
        	(len(appName) != len(tcpRxBytes)) or 
        	(len(appName) != len (tcpTxBytes)) or 
        	(len(appName) != len(udpRxBytes)) or 
        	(len(appName) != len(udpTxBytes)) or 
        	(len(appName) != len(timestamp))):
            self.error(400);
            return;
            
        # Add each data point to the database
        for i in range(len(appName)):        
            # Store data point
            point = entities.AppPoint(appName=appName[i], 
            						  uid=int(uid[i]),
            						  tcpRxBytes=int(tcpRxBytes[i]),
            						  tcpTxBytes=int(tcpTxBytes[i]),
            						  udpRxBytes=int(udpRxBytes[i]),
            						  udpTxBytes=int(udpTxBytes[i]),
            						  timestamp=int(timestamp[i]),
            						  user_id=user_id);
            point.put()

application = webapp.WSGIApplication([('/.*', AddDataApp)], debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()