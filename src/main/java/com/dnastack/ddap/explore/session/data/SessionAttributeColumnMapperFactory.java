package com.dnastack.ddap.explore.session.data;

import com.dnastack.ddap.explore.session.PersistantSession;
import com.dnastack.ddap.explore.session.PersistantSession.SessionAttributes;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

public class SessionAttributeColumnMapperFactory implements ColumnMapperFactory {

    @Override
    public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
        return type != PersistantSession.SessionAttributes.class ? Optional.empty() : Optional.of((r, columnNumber, ctx) -> {
            byte[] bytes = r.getBytes(columnNumber);
            PersistantSession.SessionAttributes sessionAttributes = null;
            if (bytes != null) {
                try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                    sessionAttributes = (PersistantSession.SessionAttributes) inputStream.readObject();
                } catch (ClassNotFoundException e) {
                    sessionAttributes = new SessionAttributes();
                } catch (Exception e) {
                    throw new SQLException(e);
                }
            }
            return sessionAttributes;
        });
    }
}
