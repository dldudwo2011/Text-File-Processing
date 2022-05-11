/**
 * @author Youngjae Lee
 * @version 2022-01-22
 *
 * description: Property assessment controller
 */

package dmit2015.youngjaelee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class EdmontonPropertyAssessmentManager {

   private List<EdmontonPropertyAssessment> csvDataList;
   private List<EdmontonPropertyAssessment> loadCsvData() throws IOException {
       List<EdmontonPropertyAssessment> dataList = new ArrayList<>();
       try (var reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/data/Property_Assessment_Data__Current_Calendar_Year_.csv"))) ) {
           String line;
           final var delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
           // Skip the first line as it is containing column headings
           reader.readLine();
           while ((line = reader.readLine()) != null) {
               String[] tokens = line.split(delimiter, -1);    // The -1 limit allows for any number of fields and not discard trailing empty fields
               // Column order of fields:
               // 0 - Column 1 Name
               // 1 - Column 2 Name
               var currentRowData = new EdmontonPropertyAssessment();

               currentRowData.setAccountNumber(tokens[0]);
               currentRowData.setSuite(tokens[1].trim().equals("") ? null : tokens[1]);
               currentRowData.setHouseNumber(tokens[2]);
               currentRowData.setStreetName(tokens[3]);
               currentRowData.setGarage(tokens[4].trim().equals("Y"));
               currentRowData.setNeighbourhoodId(tokens[5].trim().equals("") ? null : Integer.parseInt(tokens[5]));
               currentRowData.setNeighbourhood(tokens[6]);
               currentRowData.setWard(tokens[7]);
               currentRowData.setAssessedValue(Integer.parseInt(tokens[8]));
               currentRowData.setLatitude(Double.parseDouble(tokens[9]));
               currentRowData.setLongitude(Double.parseDouble(tokens[10]));
               currentRowData.setAssessmentClass1(tokens[15]);


               dataList.add(currentRowData);
           }
       }
       return dataList;
   }

   private static EdmontonPropertyAssessmentManager instance;
   private EdmontonPropertyAssessmentManager() throws IOException {
       csvDataList = loadCsvData();
   }
   public static EdmontonPropertyAssessmentManager getInstance() throws IOException {
       // https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples#thread-safe-singleton
       if(instance == null) {
           synchronized (EdmontonPropertyAssessmentManager.class) {
               if(instance == null){
                   instance = new EdmontonPropertyAssessmentManager();
               }
           }
       }
       return instance;
   }

    private static double getHaversineDistance(double latitude1, double longitude1, double latitude2, double longitude2){
        final int R = 6371000; // Radius of the earth in meter
        double latDistance = toRad(latitude2-latitude1);
        double lonDistance = toRad(longitude2-longitude1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(latitude1)) * Math.cos(toRad(latitude2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = R * c;

        return distance;
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    public Optional<EdmontonPropertyAssessment> findByAccountNumber(String accountNumber){
        return csvDataList.stream().filter(item -> item.accountNumber.equals(accountNumber)).findFirst();
    }

    public Optional<EdmontonPropertyAssessment> findByHouseNumberAndStreetName(String houseNumber, String streetName){
        return csvDataList.stream().filter(item -> item.houseNumber != null && item.houseNumber.equals(houseNumber) && item.streetName.equals(streetName)).findFirst();
    }

    public List<EdmontonPropertyAssessment>  findWithinDistance(double latitude, double longitude, double distanceMeters){
        return csvDataList.stream().filter(item -> getHaversineDistance(latitude,longitude, item.latitude, item.longitude) <= distanceMeters).collect(Collectors.toList());
    }

    public List<String> findDistinctAssessmentClasses(){
        return csvDataList.stream().map(EdmontonPropertyAssessment::getAssessmentClass1).distinct().sorted().collect(Collectors.toList());
    }

    public List<String> findDistinctWards(){
        return csvDataList.stream().filter(item -> !item.ward.trim().equals("")).map(EdmontonPropertyAssessment::getWard).distinct().sorted().collect(Collectors.toList());
    }

    //!! null?
    public Map<Integer, String> findDistinctNeighbourhoods(){
        return csvDataList.stream()
                .sorted(Comparator.comparing(EdmontonPropertyAssessment::getNeighbourhood)).filter(item -> item.neighbourhoodId != null && item.neighbourhood!=null)
                .collect(Collectors.toMap(EdmontonPropertyAssessment::getNeighbourhoodId, item -> item.neighbourhood,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }

    public long totalAssessedValueByAssessmentClass(String assessmentClass){
        return csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass)).mapToLong(EdmontonPropertyAssessment::getAssessedValue).reduce(0, (item1, item2) -> item1 + item2);
    }

    public long totalAssessedValueByAssessmentClassAndWard(String assessmentClass, String ward){
        return csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass) && item.ward.equals(ward)).mapToLong(EdmontonPropertyAssessment::getAssessedValue).reduce(0, (item1, item2) -> item1 + item2);
    }

    public long propertyCountByAssessmentClass(String assessmentClass){
        return csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass)).count();
    }

    public long propertyCountByAssessmentClassAndWard(String assessmentClass, String ward){
        return csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass) && item.ward.equals(ward)).count();
    }

    public long minAssessedValueByAssessmentClassAndNeighbourhood(String assessmentClass, String neighbourhood){
        return csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass) && item.neighbourhood.equals(neighbourhood)).mapToLong(EdmontonPropertyAssessment::getAssessedValue).min().orElse(0);
    }

    public long maxAssessedValueByAssessmentClassAndNeighbourhood(String assessmentClass, String neighbourhood){
        return csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass) && item.neighbourhood.equals(neighbourhood)).mapToLong(EdmontonPropertyAssessment::getAssessedValue).max().orElse(0);
    }

    public double averageAssessedValueByAssessmentClassAndNeighbourhood(String assessmentClass, String neighbourhood){
        return Math.round(csvDataList.stream().filter(item -> item.assessmentClass1.equals(assessmentClass) && item.neighbourhood.equals(neighbourhood)).collect(Collectors.averagingInt(EdmontonPropertyAssessment::getAssessedValue)));
    }

    public List<EdmontonPropertyAssessment>  findByNeighbourhood(String neighbourhood){
        return csvDataList.stream().filter(item -> item.neighbourhoodId != null && !item.neighbourhood.trim().equals("") && item.neighbourhood.equals(neighbourhood)).limit(999).sorted(Comparator.comparing(EdmontonPropertyAssessment::getStreetName)).sorted(Comparator.comparing(EdmontonPropertyAssessment::getHouseNumber)).collect(Collectors.toList());
    }

    public List<EdmontonPropertyAssessment>  findByNeighbourhoodAndAssessedValueRange(String neighbourhood, double minAssessedValue, double maxAssessedValue){
        return csvDataList.stream().filter(item -> item.neighbourhoodId != null && item.neighbourhood.equals(neighbourhood) && item.assessedValue >= minAssessedValue && item.assessedValue <= maxAssessedValue).sorted(Comparator.comparing(EdmontonPropertyAssessment::getStreetName)).sorted(Comparator.comparing(EdmontonPropertyAssessment::getHouseNumber)).limit(99).collect(Collectors.toList());
    }

}
