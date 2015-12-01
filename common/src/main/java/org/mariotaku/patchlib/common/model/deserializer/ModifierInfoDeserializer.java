package org.mariotaku.patchlib.common.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.mariotaku.patchlib.common.model.ModifierInfo;

import java.io.IOException;

/**
 * Created by mariotaku on 15/11/29.
 */
public class ModifierInfoDeserializer extends JsonDeserializer<ModifierInfo> {
    @Override
    public ModifierInfo deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        return ModifierInfo.parse(jsonParser.getValueAsString());
    }
}
