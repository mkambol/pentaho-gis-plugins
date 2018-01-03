package com.atolcd.pentaho.di.gis.io;

/*
 * #%L
 * Pentaho Data Integrator GIS Plugin
 * %%
 * Copyright (C) 2015 Atol CD
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.wololo.geojson.Crs;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import com.atolcd.pentaho.di.gis.io.features.Feature;
import com.atolcd.pentaho.di.gis.io.features.Field;
import com.atolcd.pentaho.di.gis.io.features.Field.FieldType;
import com.atolcd.pentaho.di.gis.utils.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

public class GeoJSONReader extends AbstractFileReader {

  private final LogChannel log;
  private String geoJsonFileName;
  private boolean geoJsonFileExist;
  private GeoJSON json;

  public GeoJSONReader( String fileName, String geometryFieldName, String charsetName ) throws KettleException {
    super( null, geometryFieldName, charsetName );
    this.log = new LogChannel( this );
    this.geoJsonFileExist = new File( checkFilename( fileName ).getFile() ).exists();
    this.geometryFieldName = geometryFieldName;

    if ( !this.geoJsonFileExist ) {
      throw new KettleException( "Missing " + fileName + " file" );
    } else {
      this.geoJsonFileName = checkFilename( fileName ).getFile();
    }

    try {
      this.json =
        GeoJSONFactory.create( FileUtils.readFileToString( new File( this.geoJsonFileName ), this.charset.name() ) );

    } catch ( IOException e ) {
      throw new KettleException( e );
    }
    populateFields();

  }

  private GeoJSONReader( String geoFieldName, String charsetName ) {
    super( null, geoFieldName, charsetName );
    this.log = new LogChannel( this );
  }

  public static GeoJSONReader from( String json, String geometryFieldName ) throws KettleException {
    GeoJSONReader reader = new GeoJSONReader( geometryFieldName, "UTF-8" );
    reader.geometryFieldName = geometryFieldName;
    reader.json = GeoJSONFactory.create( json );

    reader.populateFields();
    return reader;
  }

  private void populateFields() throws KettleException {
    fields.add( new Field( geometryFieldName, FieldType.GEOMETRY, null, null ) );
    if ( this.json instanceof FeatureCollection ) {
      org.wololo.geojson.Feature geoJsonfeature = ( (FeatureCollection) json ).getFeatures()[ 0 ];
      addFeature( geoJsonfeature );
    } else if ( json instanceof org.wololo.geojson.Feature ) {
      addFeature( (org.wololo.geojson.Feature) json );
    } else {
      throw new KettleException( "Error initialize reader : only Feature and FeatureCollection is supported" );
    }
  }

  private void addFeature( org.wololo.geojson.Feature geoJsonfeature ) {
    log.logDebug( "Geo feature:  " + geoJsonfeature.toString() );
    for ( Map.Entry<String, Object> entry : geoJsonfeature.getProperties().entrySet() ) {

      Field field = null;
      String fieldName = entry.getKey();
      Object value = entry.getValue();

      if ( value instanceof String ) {

        field = new Field( fieldName, FieldType.STRING, null, null );

      } else if ( value instanceof Integer ) {

        field = new Field( fieldName, FieldType.LONG, null, null );

      } else if ( value instanceof Boolean ) {

        field = new Field( fieldName, FieldType.BOOLEAN, null, null );

      } else if ( value instanceof Double ) {

        field = new Field( fieldName, FieldType.DOUBLE, null, null );

      } else if ( value instanceof Date ) {

        field = new Field( fieldName, FieldType.DATE, null, null );

      }

      this.fields.add( field );

    }
  }

  public List<Feature> getFeatures() {

    List<Feature> features = new ArrayList<Feature>();
    org.wololo.jts2geojson.GeoJSONReader geoJSONReader = new org.wololo.jts2geojson.GeoJSONReader();

    FeatureCollection featureCollection;
    if ( json instanceof org.wololo.geojson.Feature ) {
      Crs crs = new Crs( json.getType(), ( (org.wololo.geojson.Feature) json ).getProperties() );
      featureCollection =
        new FeatureCollection( new org.wololo.geojson.Feature[] { (org.wololo.geojson.Feature) json }, crs );

    } else {
      assert json instanceof FeatureCollection;
      featureCollection = (FeatureCollection) json;
    }

    Crs crs = featureCollection.getCrs();
    int srid = 0;

    if ( crs != null ) {

      if ( crs.getType().equalsIgnoreCase( "name" ) && crs.getProperties().containsKey( "name" ) ) {

        try {

          String csrName = (String) crs.getProperties().get( "name" );
          int sridIndex = csrName.lastIndexOf( ':' );
          srid = Integer.valueOf( csrName.substring( sridIndex + 1, csrName.length() ) );

        } catch ( Exception e ) {
          srid = 0;
        }
      }

    }

    // Traitement des features
    org.wololo.geojson.Feature geoJsonfeatures[] = featureCollection.getFeatures();
    if ( this.limit == 0 || this.limit > geoJsonfeatures.length || this.limit < 0 ) {
      this.limit = geoJsonfeatures.length;
    }

    for ( int i = 0; i < this.limit; i++ ) {

      org.wololo.geojson.Feature geoJsonfeature = geoJsonfeatures[ i ];
      Feature feature = new Feature();
      for ( Field field : fields ) {

        if ( field.getType().equals( FieldType.GEOMETRY ) ) {

          Geometry geometry = geoJSONReader.read( geoJsonfeature.getGeometry() );

          if ( this.forceTo2DGeometry ) {
            geometry = GeometryUtils.get2DGeometry( geometry );
          }

          if ( this.forceToMultiGeometry ) {
            geometry = GeometryUtils.getMultiGeometry( geometry );
          }

          geometry.setSRID( srid );
          feature.addValue( field, geometry );

        } else {
          feature.addValue( field, geoJsonfeature.getProperties().get( field.getName() ) );
        }

      }

      features.add( feature );

    }

    return features;

  }

}
