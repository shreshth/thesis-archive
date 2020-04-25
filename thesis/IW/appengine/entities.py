from google.appengine.ext import db

"""
Data type representing a single data point for a location
"""
class LocationPoint(db.Model):
    location = db.GeoPtProperty(required=True)                          # GPS locations
    bearing = db.FloatProperty(required=True)                           # Direction of movement
    speed = db.FloatProperty(required=True)                             # Speed of movement
    accuracy = db.FloatProperty(required=True)                          # Accuracy of GPS movement
    wifi_power_levels = db.StringProperty(required=True)                # wifi power levels (first one is the point's own power level)
    timestamp = db.IntegerProperty(required=True)                       # the timestamp (time since 1970) in ms
    signal_strength_3G = db.IntegerProperty(required=True)				# the 3G signal strength
    user_id = db.StringProperty(required=True)                          # Unique ID for each user

"""
Data type representing a single data type for an app
"""
class AppPoint(db.Model):
	appName = db.StringProperty(required=True)							# Application name
	uid = db.IntegerProperty(required=True);							# Application UID
	tcpRxBytes = db.IntegerProperty(required=True)						# TCP bytes received
	tcpTxBytes = db.IntegerProperty(required=True)						# TCP bytes sent
	udpRxBytes = db.IntegerProperty(required=True)						# UDP bytes received
	udpTxBytes = db.IntegerProperty(required=True)						# UDP bytes sent
	timestamp = db.IntegerProperty(required=True)						# Timestamp of data point
	user_id = db.StringProperty(required=True)							# Unique ID for each user

"""
Prediction for a certain location and movement obtained via machine learning
"""
class Prediction(db.Model):
    location = db.GeoPtProperty(required=True)                          # GPS locations
    bearing = db.FloatProperty(required=True)                           # Direction of movement
    wifi = db.BooleanProperty(required=True)                            # Whether wifi will be obtained eventually
    time = db.IntegerProperty(default=None, required=False)             # if wifi == true