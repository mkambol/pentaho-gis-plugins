package com.atolcd.pentaho.di.trans.steps.gisfileinput;


import com.atolcd.pentaho.di.core.row.value.ValueMetaGeometry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowHandler;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class GisFileInputTest {

  List<RowMetaAndData> putRows = new ArrayList<RowMetaAndData>();

  private RowHandler rowHandler = new RowHandler() {

    private Object[][] objects = new Object[][] { { "{\n"
      + "  \"type\": \"Feature\",\n"
      + "  \"geometry\": {\n"
      + "    \"type\": \"Point\",\n"
      + "    \"coordinates\": [125.6, 10.1]\n"
      + "  },\n"
      + "  \"properties\": {\n"
      + "    \"name\": \"Dinagat Islands\"\n"
      + "  }\n"
      + "}" } };

    int cur = 0;

    @Override public Object[] getRow() throws KettleException {

      return cur >= objects.length ? null : objects[ cur++ ];
    }

    @Override public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      putRows.add( new RowMetaAndData( rowMeta, row ) );
    }

    @Override public void putError( RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions,
                                    String fieldNames, String errorCodes ) throws KettleStepException {

    }
  };
  ;

  @BeforeClass
  public static void init() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testFileInput() throws KettleException {
    GisFileInput input = getGisFileInput( new GisFileInputMeta() );

    GisFileInputMeta meta = (GisFileInputMeta) input.getStepMetaInterface();

    meta.setInputFileName( "/Users/mcampbell/pentaho/trans/geo/us_counties.geojson" );
    input.processRow( meta, input.getStepDataInterface() );

    assertThat( putRows.size(), equalTo( 10 ) );
    System.out.println( putRows.get( 0 ) );
  }

  @Test
  public void testJsonInput() throws KettleException {
    GisFileInputMeta meta = new GisFileInputMeta();
    GisFileInput input = getGisFileInput( meta );

    input.processRow( meta, input.getStepDataInterface() );

    assertThat( putRows.size(), equalTo( 1 ) );
    assertThat( (String) putRows.get( 0 ).getData()[ 0 ],
      equalTo( "Dinagat Islands" ) );
    System.out.println( putRows.get( 0 ) );
  }



  private GisFileInput getGisFileInput( GisFileInputMeta gisMeta ) {
    TransMeta transMeta = new TransMeta();

    Trans trans = new Trans( transMeta );

    //GisFileInputMeta gisMeta = ;
    GisFileInputData data = new GisFileInputData();
    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "name" ) );
    data.outputRowMeta.addValueMeta( new ValueMetaGeometry( "geometry" ) );

    StepMeta stepMeta = new StepMeta( "GisFileInput", "test", gisMeta );


    gisMeta.setGeometryFieldName( "geometry" );
    gisMeta.setInputFormat( "GeoJSON" );
    gisMeta.setRowLimit( 10L );
    transMeta.addStep( stepMeta );


    GisFileInput input = new GisFileInput( stepMeta, data, 0, transMeta, trans );
    input.setRowHandler( rowHandler );
    input.setStepMetaInterface( gisMeta );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "json" ) );
    input.setInputRowMeta( rowMeta );
    return input;
  }


}