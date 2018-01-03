package com.atolcd.pentaho.di.gis.io;

import com.atolcd.pentaho.di.gis.io.features.Feature;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class GeoJSONReaderTest {

  @Test
  public void testFromFeatureCollection() throws Exception {

    GeoJSONReader reader = GeoJSONReader.from( "{\n"
      + "\"type\": \"FeatureCollection\",\n"
      + "\"features\": [\n"
      + "{ \"type\": \"Feature\", \"properties\": { \"NAME\": \"Lake of the Woods\", \"STATE_NAME\": \"Minnesota\", "
      + "\"STATE_FIPS\": \"27\", \"CNTY_FIPS\": \"077\", \"FIPS\": \"27077\", \"AREA\": 1784.063410, \"POP2000\": "
      + "4522, \"POP2001\": 4493, \"POP00_SQMI\": 2.500000, \"WHITE\": 4396, \"BLACK\": 13, \"AMERI_ES\": 51, "
      + "\"ASIAN\": 11, \"HAWN_PI\": 0, \"OTHER\": 5, \"MULT_RACE\": 46, \"HISPANIC\": 29, \"MALES\": 2272, "
      + "\"FEMALES\": 2250, \"AGE_UNDER5\": 188, \"AGE_5_17\": 930, \"AGE_18_21\": 149, \"AGE_22_29\": 282, "
      + "\"AGE_30_39\": 588, \"AGE_40_49\": 770, \"AGE_50_64\": 835, \"AGE_65_UP\": 780, \"MED_AGE\": 41.600000, "
      + "\"MED_AGE_M\": 41.600000, \"MED_AGE_F\": 41.600000, \"HOUSEHOLDS\": 1903, \"AVE_HH_SZ\": 2.350000, "
      + "\"HSEHLD_1_M\": 305, \"HSEHLD_1_F\": 260, \"MARHH_CHD\": 448, \"MARHH_NO_C\": 645, \"MHH_CHILD\": 46, "
      + "\"FHH_CHILD\": 62, \"FAMILIES\": 1267, \"AVE_FAM_SZ\": 2.930000, \"HSE_UNITS\": 3238, \"VACANT\": 1335, "
      + "\"OWNER_OCC\": 1623, \"RENTER_OCC\": 280, \"NO_FARMS97\": 196, \"AVG_SIZE97\": 600, \"CROP_ACR97\": 78126, "
      + "\"AVG_SALE97\": 39.890000 }, \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [ [ [ 327095.578299, "
      + "5379714.453972 ], [ 327801.383458, 5398437.795276 ], [ 345952.786216, 5398152.707476 ], [ 346507.097328, "
      + "5419762.286655 ], [ 343602.372602, 5417921.152939 ], [ 337268.430617, 5416429.627195 ], [ 332165.387632, "
      + "5419195.728694 ], [ 330511.833924, 5422485.810483 ], [ 331321.278827, 5424003.958372 ], [ 330116.090697, "
      + "5425713.724400 ], [ 330007.489668, 5427717.356615 ], [ 331015.925104, 5429293.012407 ], [ 333485.576380, "
      + "5429952.177717 ], [ 342181.866496, 5429698.595081 ], [ 343787.173553, 5471007.426879 ], [ 366893.093409, "
      + "5465845.316569 ], [ 376729.378369, 5415163.126768 ], [ 375516.506274, 5404119.520708 ], [ 384488.294100, "
      + "5396816.906217 ], [ 394756.665230, 5396293.328243 ], [ 393965.820587, 5358217.100709 ], [ 336195.305309, "
      + "5359678.605362 ], [ 336164.463903, 5379185.418466 ], [ 327095.578299, 5379714.453972 ] ] ] } }\n"
      + "]\n"
      + "}\n\n", "field" );
    assertThat( reader.getFeatures().size(), equalTo( 1 ) );
    final Feature feature = reader.getFeatures().get( 0 );
    assertThat( feature.getValue( feature.getField( "NAME" ) ).toString(),
      equalTo( "Lake of the Woods" ) );
    System.out.println( reader.getFeatures() );
  }

  @Test
  public void testFromFeature() throws Exception {

    GeoJSONReader reader = GeoJSONReader.from(
      "{ \"type\": \"Feature\", \"properties\": { \"NAME\": \"Lake of the Woods\", \"STATE_NAME\": \"Minnesota\", "
      + "\"STATE_FIPS\": \"27\", \"CNTY_FIPS\": \"077\", \"FIPS\": \"27077\", \"AREA\": 1784.063410, \"POP2000\": "
      + "4522, \"POP2001\": 4493, \"POP00_SQMI\": 2.500000, \"WHITE\": 4396, \"BLACK\": 13, \"AMERI_ES\": 51, "
      + "\"ASIAN\": 11, \"HAWN_PI\": 0, \"OTHER\": 5, \"MULT_RACE\": 46, \"HISPANIC\": 29, \"MALES\": 2272, "
      + "\"FEMALES\": 2250, \"AGE_UNDER5\": 188, \"AGE_5_17\": 930, \"AGE_18_21\": 149, \"AGE_22_29\": 282, "
      + "\"AGE_30_39\": 588, \"AGE_40_49\": 770, \"AGE_50_64\": 835, \"AGE_65_UP\": 780, \"MED_AGE\": 41.600000, "
      + "\"MED_AGE_M\": 41.600000, \"MED_AGE_F\": 41.600000, \"HOUSEHOLDS\": 1903, \"AVE_HH_SZ\": 2.350000, "
      + "\"HSEHLD_1_M\": 305, \"HSEHLD_1_F\": 260, \"MARHH_CHD\": 448, \"MARHH_NO_C\": 645, \"MHH_CHILD\": 46, "
      + "\"FHH_CHILD\": 62, \"FAMILIES\": 1267, \"AVE_FAM_SZ\": 2.930000, \"HSE_UNITS\": 3238, \"VACANT\": 1335, "
      + "\"OWNER_OCC\": 1623, \"RENTER_OCC\": 280, \"NO_FARMS97\": 196, \"AVG_SIZE97\": 600, \"CROP_ACR97\": 78126, "
      + "\"AVG_SALE97\": 39.890000 }, \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [ [ [ 327095.578299, "
      + "5379714.453972 ], [ 327801.383458, 5398437.795276 ], [ 345952.786216, 5398152.707476 ], [ 346507.097328, "
      + "5419762.286655 ], [ 343602.372602, 5417921.152939 ], [ 337268.430617, 5416429.627195 ], [ 332165.387632, "
      + "5419195.728694 ], [ 330511.833924, 5422485.810483 ], [ 331321.278827, 5424003.958372 ], [ 330116.090697, "
      + "5425713.724400 ], [ 330007.489668, 5427717.356615 ], [ 331015.925104, 5429293.012407 ], [ 333485.576380, "
      + "5429952.177717 ], [ 342181.866496, 5429698.595081 ], [ 343787.173553, 5471007.426879 ], [ 366893.093409, "
      + "5465845.316569 ], [ 376729.378369, 5415163.126768 ], [ 375516.506274, 5404119.520708 ], [ 384488.294100, "
      + "5396816.906217 ], [ 394756.665230, 5396293.328243 ], [ 393965.820587, 5358217.100709 ], [ 336195.305309, "
      + "5359678.605362 ], [ 336164.463903, 5379185.418466 ], [ 327095.578299, 5379714.453972 ] ] ] } }\n"
      + "}\n\n", "field" );
    assertThat( reader.getFeatures().size(), equalTo( 1 ) );
    final Feature feature = reader.getFeatures().get( 0 );
    assertThat( feature.getValue( feature.getField( "NAME" ) ).toString(),
      equalTo( "Lake of the Woods" ) );
    System.out.println( reader.getFeatures() );
  }



}