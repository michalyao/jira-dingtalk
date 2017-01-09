package me.yoryor.plugin.entity;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeItem {
    private String field;
    private String fieldType;
    private String from;
    private String fromString;
    private String to;
    private String toString;

    public static ChangeItem fromJson(JsonObject jsonObject) {
        return builder().field(jsonObject.getString("field", ""))
                .fieldType(jsonObject.getString("fieldtype", ""))
                .from(jsonObject.getString("from", ""))
                .to(jsonObject.getString("to", ""))
                .fromString(jsonObject.getString("fromString", ""))
                .toString(jsonObject.getString("toString", ""))
                .build();
    }
}
