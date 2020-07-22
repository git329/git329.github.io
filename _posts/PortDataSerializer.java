package com.nokia.cd.otc.telemetry.reporter.model.output.port;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.nokia.cd.otc.telemetry.reporter.model.PmData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * keep the field "sample-interval" and "time-stamp" only for the first item of the list.
 */
@Slf4j
public class PortDataSerializer extends StdSerializer<List<PmData>> {


    private static List<PmData> PmDatas = new ArrayList<PmData>();

    protected PortDataSerializer(Class<List<PmData>> t) {
        super(t);
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("unchecked")
    public PortDataSerializer() {
        this((Class<List<PmData>>) PmDatas.getClass());

    }

    @Override
    public void serialize(List<PmData> pmDatas, JsonGenerator paramJsonGenerator,
            SerializerProvider provider) throws IOException {
        paramJsonGenerator.writeStartArray();
        if(pmDatas.size()==0){
            paramJsonGenerator.writeEndArray();
            return;
        }
        provider.defaultSerializeValue(pmDatas.get(0), paramJsonGenerator);

        for (int i = 1; i < pmDatas.size(); i++) {

            paramJsonGenerator.writeStartObject();

            provider.defaultSerializeField("counter", pmDatas.get(i).getCounter(),
                    paramJsonGenerator);
            provider.defaultSerializeField("counter-type", pmDatas.get(i).getCounterType(),
                    paramJsonGenerator);
            provider.defaultSerializeField("name", pmDatas.get(i).getName(),
                    paramJsonGenerator);

            paramJsonGenerator.writeEndObject();

        }

        paramJsonGenerator.writeEndArray();
    }

}
