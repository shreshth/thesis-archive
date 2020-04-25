var map;
var points;

function initialize() {
    var mapOptions = {
        center: new google.maps.LatLng(40.346545,-74.654425),
        zoom: 16,
        minZoom: 13,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        mapTypeControl: false,
        streetViewControl: false,
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
    addpoints();
}

function addpoints() {
    var r = 0.0001;
    for (var i = 0; i < points.length; i++) {
        var point = points[i];
        var lat = point[0];
        var lng = point[1];
        var power = point[2].split('.',1);
        var accuracy = point[3];
        var bearing = point[4];
        if ((power >= min_power) && (power <= max_power) && (accuracy < min_accuracy)) {
        	var location = new google.maps.LatLng(lat, lng);
        
        	if (lat >= 40.345 && lat <= 40.350 && lng >= -74.660 && lng <= -74.655 && bearing >= 45 && bearing <= 90) {
				new google.maps.Marker({
					position: location,
					map: map,
					icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
					zIndex: 2,
				});
        	} else {
        		new google.maps.Marker({
					position: location,
					map: map,
					zIndex: 1,
				});
			}                        
        }
    }
}
