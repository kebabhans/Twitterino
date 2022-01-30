package TwitterResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class TwitterResponse {
    public TwitterResponseDataElement data;
    public TwitterResponseIncludesElement includes;

    @JsonProperty(value = "matching_rules")
    public List<TwitterStreamRule> matchedRules;
}
