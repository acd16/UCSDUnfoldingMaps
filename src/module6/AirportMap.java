package module6;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/**
 * An applet that shows airports (and routes) on a world map.
 * 
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 *         MOOC team
 *
 */
public class AirportMap extends PApplet {

	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	private List<Marker> countryMarkers;
	private CommonMarker lastSelected;
	
	public void setup() {
		// setting up PAppler
		size(1300, 1100, OPENGL);

		// setting up map and default events
//		map = new UnfoldingMap(this, 50, 50, 750, 550, new Google.GoogleMapProvider());
		map = new UnfoldingMap(this, 0, 0, 1300, 1100, new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);

		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");

		String countryFile = "/Users/adithya/Downloads/UCSDUnfoldingMaps/data/usa.geo.json";
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		// list for markers, hashmap for quicker access when matching with
		// routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, PointFeature> airports = new HashMap<Integer, PointFeature>();
		// create markers from features
		for (PointFeature feature : features) {
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature);

		}

		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		int yel = color(255, 255, 0);
		int blue = color(0, 0, 127);
		for (ShapeFeature route : routes) {

			// get source and destination airportIds
			int source = Integer.parseInt((String) route.getProperty("source"));
			int dest = Integer.parseInt((String) route.getProperty("destination"));

			// get locations for airports on route between USA and other countries.
			if (airports.containsKey(source) && airports.containsKey(dest)
					&& isInCountry((Location) airports.get(source).getLocation(), countryMarkers.get(0))
					&& isInCountry((Location) airports.get(dest).getLocation(), countryMarkers.get(1))) {
				AirportMarker m = new AirportMarker(airports.get(source));
				AirportMarker n = new AirportMarker(airports.get(dest));
				
				m.setTitle(route.getStringProperty("sourceName"));
				n.setTitle(route.getStringProperty("destinationName"));
				m.setRadius(10);
				n.setRadius(10);
				m.setColor(yel);
				n.setColor(yel);
				airportList.add(m);
				airportList.add(n);

				route.addLocation(airports.get(source).getLocation());
				route.addLocation(airports.get(dest).getLocation());

				SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
				sl.setColor(blue);

				// System.out.println(sl.getProperties());

				// UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
				routeList.add(sl);
			}
		}

		// UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);

		map.addMarkers(airportList);

	}

	public void draw() {
		background(0);
		map.draw();

	}

	private boolean isInCountry(PointFeature airport, Marker country) {
		// getting location of feature
		Location checkLoc = airport.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use
		// isInsideByLoc
		if (country.getClass() == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker) country).getMarkers()) {

				// checking if inside
				if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {

					// return if is inside one
					return true;
				}
			}
		}

		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
			return true;
		}
		return false;
	}

	private boolean isInCountry(Location checkLoc, Marker country) {

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use
		// isInsideByLoc
		if (country.getClass() == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker) country).getMarkers()) {

				// checking if inside
				if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {

					// return if is inside one
					return true;
				}
			}
		}

		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {

			return true;
		}
		return false;
	}
	
	public void mouseMoved() {
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;

		}
		selectMarkerIfHover(airportList);
		// loop();
	}

	// If there is a marker selected
	private void selectMarkerIfHover(List<Marker> markers) {
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker) m;
			if (marker.isInside(map, mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}

}