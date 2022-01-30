package TwitterResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class TwitterResponseDataElement {
    public String id;
    public String text;
    public String author_id;
    @JsonProperty(value = "created_at")
    public Date created;
}
