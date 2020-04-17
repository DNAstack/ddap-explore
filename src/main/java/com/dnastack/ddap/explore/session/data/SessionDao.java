package com.dnastack.ddap.explore.session.data;

import com.dnastack.ddap.explore.session.PersistantSession;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactories;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterBeanMappers;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactories;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;


@RegisterColumnMapperFactories(
    value = @RegisterColumnMapperFactory(SessionAttributeColumnMapperFactory.class)
)
@RegisterArgumentFactories(
    value = @RegisterArgumentFactory(SessionAttributeArgumentFactory.class)
)
@RegisterBeanMappers(
    value = @RegisterBeanMapper(PersistantSession.class)
)
public interface SessionDao {


    @Transaction
    @SqlUpdate("INSERT INTO sessions(principal_id,session_id,last_accessed_time,creation_time,max_inactive_interval,attributes) VALUES(:principalId,:sessionId,:lastAccessedTime,:creationTime,:maxInactiveInterval,:attributes)")
    void createSession(@BindBean PersistantSession session);

    @SqlQuery("SELECT * FROM sessions where session_id = :sessionId")
    Optional<PersistantSession> getSession(@Bind("sessionId") String sessionId);

    @SqlQuery("SELECT * FROM sessions where principal_id = :principalId")
    Optional<PersistantSession> getSessionByPrincipalId(@Bind("principalId") String principalId);

    @Transaction
    @SqlUpdate("DELETE FROM sessions where session_id = :sessionId")
    void deleteSession(@Bind("sessionId") String id);

    @Transaction
    @SqlUpdate("DELETE FROM sessions where principal_id = :principalId")
    void deleteSessionByPrincipalId(@Bind("principalId") String principalId);

    @Transaction
    @SqlUpdate("UPDATE sessions SET session_id = :sessionId, last_accessed_time = :lastAccessedTime, attributes = :attributes  WHERE principal_id = :principalId")
    void updateSession(@BindBean PersistantSession session);

}
