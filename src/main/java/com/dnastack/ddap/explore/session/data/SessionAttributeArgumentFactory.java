package com.dnastack.ddap.explore.session.data;

import com.dnastack.ddap.explore.session.PersistantSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Types;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class SessionAttributeArgumentFactory extends AbstractArgumentFactory<PersistantSession.SessionAttributes> {


    public SessionAttributeArgumentFactory() {
        super(Types.BLOB);
    }


    @Override
    protected Argument build(PersistantSession.SessionAttributes value, ConfigRegistry config) {
        return (position,statement,ctx) -> {
            try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){
                objectOutputStream.writeObject(value);
                objectOutputStream.flush();
                statement.setBytes(position,byteArrayOutputStream.toByteArray());
            }
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        };
    }
}
