package com.example.travium.view

// Retro/Clean Travel Map Style JSON (Similar to your reference image)
const val TRAVEL_MAP_STYLE = """
[
  { "elementType": "geometry", "stylers": [ { "color": "#ebe3cd" } ] },
  { "elementType": "labels.text.fill", "stylers": [ { "color": "#523735" } ] },
  { "elementType": "labels.text.stroke", "stylers": [ { "color": "#f5f1e6" } ] },
  { "featureType": "administrative", "elementType": "geometry.stroke", "stylers": [ { "color": "#c9b2a6" } ] },
  { "featureType": "landscape.natural", "elementType": "geometry", "stylers": [ { "color": "#dfd2ae" } ] },
  { "featureType": "poi", "elementType": "geometry", "stylers": [ { "color": "#dfd2ae" } ] },
  { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#93817c" } ] },
  { "featureType": "poi.park", "elementType": "geometry.fill", "stylers": [ { "color": "#a5b076" } ] },
  { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#f5f1e6" } ] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#f8c967" } ] },
  { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [ { "color": "#e9bc62" } ] },
  { "featureType": "water", "elementType": "geometry.fill", "stylers": [ { "color": "#b9d3c2" } ] }
]
"""

// Custom Dark Map Style JSON
const val MIDNIGHT_MAP_STYLE = """
[
  { "elementType": "geometry", "stylers": [ { "color": "#212121" } ] },
  { "elementType": "labels.icon", "stylers": [ { "visibility": "off" } ] },
  { "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
  { "elementType": "labels.text.stroke", "stylers": [ { "color": "#212121" } ] },
  { "featureType": "administrative", "elementType": "geometry", "stylers": [ { "color": "#757575" } ] },
  { "featureType": "administrative.country", "elementType": "labels.text.fill", "stylers": [ { "color": "#9e9e9e" } ] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [ { "color": "#bdbdbd" } ] },
  { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
  { "featureType": "poi.park", "elementType": "geometry", "stylers": [ { "color": "#181818" } ] },
  { "featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
  { "featureType": "poi.park", "elementType": "labels.text.stroke", "stylers": [ { "color": "#1b1b1b" } ] },
  { "featureType": "road", "elementType": "geometry.fill", "stylers": [ { "color": "#2c2c2c" } ] },
  { "featureType": "road", "elementType": "labels.text.fill", "stylers": [ { "color": "#8a8a8a" } ] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#3c3c3c" } ] },
  { "featureType": "road.highway.controlled_access", "elementType": "geometry", "stylers": [ { "color": "#4e4e4e" } ] },
  { "featureType": "road.local", "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
  { "featureType": "transit", "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
  { "featureType": "water", "elementType": "geometry.fill", "stylers": [ { "color": "#000000" } ] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [ { "color": "#3d3d3d" } ] }
]
"""
