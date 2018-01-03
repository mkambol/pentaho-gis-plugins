package com.atolcd.pentaho.di.trans.steps.gisfileinput;

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

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.atolcd.pentaho.di.gis.io.AbstractFileReader;
import com.atolcd.pentaho.di.gis.io.DXFReader;
import com.atolcd.pentaho.di.gis.io.GeoJSONReader;
import com.atolcd.pentaho.di.gis.io.MapInfoReader;
import com.atolcd.pentaho.di.gis.io.ShapefileReader;
import com.atolcd.pentaho.di.gis.io.SpatialiteReader;
import com.atolcd.pentaho.di.gis.io.features.Feature;
import com.atolcd.pentaho.di.gis.io.features.FeatureConverter;

import static com.google.common.base.Preconditions.checkArgument;
import static org.pentaho.di.core.util.Utils.isEmpty;

public class GisFileInput extends BaseStep implements StepInterface {

  private final String sourceField;
  private GisFileInputData data;
  private GisFileInputMeta meta;
  private AbstractFileReader fileReader;

  public GisFileInput( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
    this.sourceField = "json";
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (GisFileInputMeta) smi;
    data = (GisFileInputData) sdi;

    if ( hasPrevious() ) {
      // if there's a preceding step with a field called "json", we'll assume geojsoninput.
      checkArgument( meta.getInputFormat().equalsIgnoreCase( "GEOJSON" ) );
      return processRowFromPrevStep();
    }

    if ( first ) {

      first = false;
      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this );

      if ( meta.getInputFormat().equalsIgnoreCase( "ESRI_SHP" ) ) {
        fileReader = new ShapefileReader( environmentSubstitute( meta.getInputFileName() ),
          environmentSubstitute( meta.getGeometryFieldName() ), meta.getEncoding() );
      } else if ( meta.getInputFormat().equalsIgnoreCase( "GEOJSON" ) ) {
        fileReader = new GeoJSONReader( environmentSubstitute( meta.getInputFileName() ),
          environmentSubstitute( meta.getGeometryFieldName() ), meta.getEncoding() );
      } else if ( meta.getInputFormat().equalsIgnoreCase( "MAPINFO_MIF" ) ) {
        fileReader = new MapInfoReader( environmentSubstitute( meta.getInputFileName() ),
          environmentSubstitute( meta.getGeometryFieldName() ), meta.getEncoding() );
      } else if ( meta.getInputFormat().equalsIgnoreCase( "SPATIALITE" ) ) {

        String tableName = environmentSubstitute( (String) meta.getInputParameterValue( "DB_TABLE_NAME" ) );
        fileReader =
          new SpatialiteReader( environmentSubstitute( meta.getInputFileName() ), tableName, meta.getEncoding() );

      } else if ( meta.getInputFormat().equalsIgnoreCase( "DXF" ) ) {
        String circleAsPolygon = environmentSubstitute( (String) meta.getInputParameterValue( "CIRCLE_AS_POLYGON" ) );
        String ellipseAsPolygon = environmentSubstitute( (String) meta.getInputParameterValue( "ELLIPSE_AS_POLYGON" ) );
        String lineAsPolygon = environmentSubstitute( (String) meta.getInputParameterValue( "LINE_AS_POLYGON" ) );

        fileReader = new DXFReader( environmentSubstitute( meta.getInputFileName() ),
          environmentSubstitute( meta.getGeometryFieldName() ), meta.getEncoding(),
          Boolean.parseBoolean( circleAsPolygon ), Boolean.parseBoolean( ellipseAsPolygon ),
          Boolean.parseBoolean( lineAsPolygon ) );
      }

      String forceToMultigeometry =
        environmentSubstitute( (String) meta.getInputParameterValue( "FORCE_TO_MULTIGEOMETRY" ) );
      if ( forceToMultigeometry != null ) {
        fileReader.setForceToMultiGeometry( Boolean.parseBoolean( forceToMultigeometry ) );
      }

      String forceTo2D = environmentSubstitute( (String) meta.getInputParameterValue( "FORCE_TO_2D" ) );
      if ( forceTo2D != null ) {
        fileReader.setForceTo2DGeometry( Boolean.parseBoolean( forceTo2D ) );
      }

      fileReader.setLimit( meta.getRowLimit() );
      incrementLinesInput();
      logBasic( "Initialized successfully" );

    }

    putRowsFromFeatures( fileReader.getFeatures() );

    setOutputDone();
    return false;

  }

  private void putRowsFromFeatures( List<Feature> features ) throws KettleStepException, KettleValueException {
    Iterator<Feature> featureIt = features.iterator();
    while ( featureIt.hasNext() ) {
      putRow( data.outputRowMeta, FeatureConverter.getRow( data.outputRowMeta, featureIt.next() ) );
      incrementLinesOutput();

    }
  }

  // hacked together method to allow retrieving geojson content from a previous step
  // rather than file.  Used strictly for #hackathon2018
  private boolean processRowFromPrevStep() throws KettleException {
    Object[] row = getRow();
    if ( row == null ) {
      setOutputDone();
      return false;
    }
    data.outputRowMeta = new RowMeta();
    meta.getFields( data.outputRowMeta, getStepname(), null, null, this );
    int indexOfSourceJson = getIndexOfSourceJson();
    String json = (String) row[ indexOfSourceJson ];
    putRowsFromFeatures(
      GeoJSONReader.from( json, meta.getGeometryFieldName() ).getFeatures() );
    return true;
  }

  private boolean hasPrevious() {
    return getStepMeta().getParentTransMeta().findPreviousSteps( getStepMeta() ).size() > 0;
  }
  private int getIndexOfSourceJson() {
    return getInputRowSets() != null && getInputRowSets().size() > 0
      ? getInputRowSets().get(0).getRowMeta().indexOfValue("json")
      : -1;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GisFileInputMeta) smi;
    data = (GisFileInputData) sdi;

    return super.init( smi, sdi );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GisFileInputMeta) smi;
    data = (GisFileInputData) sdi;
    super.dispose( smi, sdi );
  }

  public void run() {
    logBasic( "Starting to run..." );
    try {
      while ( processRow( meta, data ) && !isStopped() ) {
        ;
      }
    } catch ( Exception e ) {
      logError( "Unexpected error : " + e.toString() );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
    } finally {
      dispose( meta, data );
      logBasic( "Finished, processing " + getLinesRead() + " rows" );
      markStop();
    }
  }

}
