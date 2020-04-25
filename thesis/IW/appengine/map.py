from google.appengine.dist import use_library
import os
use_library('django', '0.96')
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.webapp import template

import entities

class MapPage(webapp.RequestHandler):
    def get(self):
        min_power = self.request.get('min_power')
        if (not min_power):
            min_power = -1000
        max_power = self.request.get('max_power')
        if (not max_power):
            max_power = 0
        min_accuracy = self.request.get('min_accuracy')
        if (not min_accuracy):
            min_accuracy = 1000
        template_values = {
            'points': entities.LocationPoint.all(),
            'min_power' : min_power,
            'max_power' : max_power,
            'min_accuracy' : min_accuracy,
        }
        path = os.path.join(os.path.dirname(__file__), 'map.html')
        self.response.out.write(template.render(path, template_values))

application = webapp.WSGIApplication([('/.*', MapPage)], debug=True)

def main():
    use_library('django', '0.96')
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
