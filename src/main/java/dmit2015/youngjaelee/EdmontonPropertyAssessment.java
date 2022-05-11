/**
 * @author Youngjae Lee
 * @version 2022-01-22
 *
 * description: Property assessment model
 */

package dmit2015.youngjaelee;

import lombok.Data;

@Data
public class EdmontonPropertyAssessment {
        public String accountNumber;
        public String suite;
        public String houseNumber;
        public String streetName;
        public Boolean garage;
        public Integer neighbourhoodId;
        public String neighbourhood;
        public String ward;
        public Integer assessedValue;
        public Double latitude;
        public Double longitude;
        public String assessmentClass1;

    public EdmontonPropertyAssessment() {
    }
}
