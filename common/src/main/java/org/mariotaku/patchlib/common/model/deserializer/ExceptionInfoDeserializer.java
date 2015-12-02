package org.mariotaku.patchlib.common.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.mariotaku.patchlib.common.model.ExceptionInfo;

import java.io.IOException;

/**
 * Created by mariotaku on 15/11/29.
 */
public class ExceptionInfoDeserializer extends JsonDeserializer<ExceptionInfo> {
    @Override
    public ExceptionInfo deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        return ExceptionInfo.parse(jsonParser.getValueAsString());
    }
}
