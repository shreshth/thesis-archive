from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from math import atan2

import entities

class AddDataLocation(webapp.RequestHandler):
    def get(self):
        self.post()
    def post(self):
        # Read in comma seperated values and split them into arrays
        lat = str.split(str(self.request.get('lat')),',');
        lng = str.split(str(self.request.get('lng')),',');
        bearing = str.split(str(self.request.get('bearing')),',');
        speed = str.split(str(self.request.get('speed')),',');
        accuracy = str.split(str(self.request.get('accuracy')),',');
        wifi_power_levels = str.split(str(self.request.get('wifi_power_levels')),',');
        signal_strength = str.split(str(self.request.get('sigstrength')),',');
        timestamp = str.split(str(self.request.get('timestamp')),',');
        # Read user id
        user_id = self.request.get('user_id');
        
        if ((lat == ['']) or
        	(lng == ['']) or
        	(bearing == ['']) or
        	(wifi_power_levels == ['']) or
        	(timestamp == ['']) or
        	(speed == ['']) or
        	(accuracy == ['']) or
        	(signal_strength == ['']) or
        	(user_id == '')):    
            self.error(400);
            return;
        
        # All arrays must have the same size
        if ((len(lat) != len (lng)) or 
        	(len(lat) != len (bearing)) or 
        	(len(lat) != len (wifi_power_levels)) or 
        	(len(lat) != len (timestamp)) or 
        	(len(lat) != len (speed)) or 
        	(len(lat) != len (accuracy)) or
        	(len(lat) != len (signal_strength))):
            self.error(400);  
            return;                  
            
        # Add each data point to the database
        for i in range(len(lat)):
            location = db.GeoPt(lat[i], lng[i]);
            # Store data point
            point = entities.LocationPoint(location=location, 
            							   bearing=float(bearing[i]), 
            							   speed=float(speed[i]), 
            							   accuracy=float(accuracy[i]), 
            							   wifi_power_levels=wifi_power_levels[i], 
            							   timestamp=int(timestamp[i]), 
            							   signal_strength_3G = int(signal_strength[i]),
            							   user_id = user_id);
            point.put();

application = webapp.WSGIApplication([('/.*', AddDataLocation)], debug=True)

def main():
    run_wsgi_app(application);

if __name__ == "__main__":
    main();